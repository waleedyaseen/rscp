package me.waliedyassen.rscp.format.config
 
import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.parser.Token
import me.waliedyassen.rscp.symbol.SymbolType
import kotlin.reflect.KMutableProperty0
 
fun parsePropertyMultiConfig(
    parser: Parser,
    multiDefaultProperty: KMutableProperty0<Any?>,
    multiProperty: KMutableProperty0<MutableMap<Int, Any>?>,
    type: SymbolType<*>,
) {
    val id = parser.peekIdentifier()
    if (id != null && id is Token.Identifier && id.text == "default") {
        // guaranteed to pass anyway since we used peekIdentifier()
        val defaultKeyword = parser.parseIdentifier()!!
        parser.storeSemInfo(defaultKeyword.span, "literal")
        parser.parseComma() ?: return parser.skipProperty()
        multiDefaultProperty.set(parser.parseReference(type) ?: return parser.skipProperty())
    } else {
        val state = parser.parseInteger() ?: return parser.skipProperty()
        parser.parseComma() ?: return parser.skipProperty()
        val ref = parser.parseReference(type) ?: return parser.skipProperty()
        var multi = multiProperty()
        if (multi == null) {
            multi = LinkedHashMap()
            multiProperty.set(multi)
        }
        multi[state] = ref
    }
}
 
fun resolveReferencesMultiConfig(
    compiler: Compiler,
    multiProperty: KMutableProperty0<MutableMap<Int, Any>?>,
    multiDefaultProperty: KMutableProperty0<Any?>,
    multiVarRefProperty: KMutableProperty0<Reference?>,
    multiVarProperty: KMutableProperty0<Int>,
    multiVarbitProperty: KMutableProperty0<Int>
) {
    multiProperty.set(multiProperty()
        ?.mapValues { (_, value) -> compiler.resolveReferenceId(value as Reference) }
        ?.toMutableMap())
    if (multiDefaultProperty() != null) {
        compiler.resolveReference(multiDefaultProperty)
    }
    val multiVarRef = multiVarRefProperty()
    if (multiVarRef != null) {
        val multiVar = compiler.sym.lookupSymbol(SymbolType.VarPlayer, multiVarRef.name)?.id ?: -1
        val multiVarbit = compiler.sym.lookupSymbol(SymbolType.VarBit, multiVarRef.name)?.id ?: -1
        if (multiVar == -1 && multiVarbit == -1) {
            val message = "Unresolved reference to '${multiVarRef.name}' of type 'var/bit'"
            compiler.addError(multiVarRef.span, message)
        }
        multiVarProperty.set(multiVar)
        multiVarbitProperty.set(multiVarbit)
    }
 
}
