package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.TypedSymbol

class VarcConfig(name: String) : Config(name, SymbolType.VarClient) {

    var type: SymbolType<*> = SymbolType.Undefined
    var scope = VarLifetime.TEMPORARY

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return parser.skipProperty()
            "scope" -> scope = parser.parseEnumLiteral() ?: return parser.skipProperty()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (type == SymbolType.Undefined) {
            parser.reportUnitError("type property must be specified")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun createSymbol(id: Int) = TypedSymbol(name, id, type)

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(6)
        packet.code(1) {
            write1(type.legacyChar.code)
        }
        if (scope == VarLifetime.PERMANENT) {
            packet.code(2) {}
        }
        packet.terminateCode()
        return packet.toByteArray()
    }

}