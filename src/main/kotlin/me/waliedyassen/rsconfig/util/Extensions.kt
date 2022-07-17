package me.waliedyassen.rsconfig.util

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.rsconfig.CompilerContext
import me.waliedyassen.rsconfig.parser.Span
import me.waliedyassen.rsconfig.symbol.SymbolType

/**
 * Convert the value of this [JsonNode] to [SymbolType] object.
 */
fun JsonNode.asSymbolType(): SymbolType<*> {
    val literal = asText() ?: error("Type literal was expected")
    return SymbolType.lookup(literal)
}

/**
 * Convert the value of this [JsonNode] to a value that is compatible of the specified [SymbolType].
 */
fun JsonNode.asValue(type: SymbolType<*>, context: CompilerContext) = asText().parseValue(type, context)

/**
 * Convert the value of this [JsonNode] to a configuration reference id.
 */
fun JsonNode.asReference(type: SymbolType<*>, context: CompilerContext) = asText().parseReference(type, context)

/**
 * Convert the value of this [JsonNode] to a custom [LiteralEnum] constant reference.
 */
inline fun <reified T> JsonNode.asEnumLiteral(defaultValue: T? = null): T where T : Enum<T>, T : LiteralEnum {
    val values = enumValues<T>()
    val literal = asText() ?: error("A text literal is required")
    return values.find { it.literal == literal }
        ?: defaultValue
        ?: error("Could not find enum constant for literal: $literal for type: ${T::class.java.simpleName}")
}

/**
 * Parse a value that is compatible of the specified [SymbolType] from the raw value of this [String].
 */
fun String.parseValue(type: SymbolType<*>, context: CompilerContext): Any {
    if (type.isReference()) {
        return parseReference(type, context);
    }
    return when (type) {
        SymbolType.Int -> toInt()
        SymbolType.Boolean -> if (toBoolean()) 1 else 0
        SymbolType.String -> this
        else -> error("Unrecognized type: $type")
    }
}

/**
 * Parse a configuration reference of type [SymbolType] from the raw value of this [String].
 */
fun String.parseReference(type: SymbolType<*>, context: CompilerContext): Int {
    if (isNullOrBlank() || this == "null") {
        return -1
    }
    val symbol = context.sym.lookupList(type).lookupByName(this)
    if (symbol == null) {
        context.addError(Span.empty(), "Unresolved ${type.literal} reference to '${this}'")
        return -1
    }
    return symbol.id
}