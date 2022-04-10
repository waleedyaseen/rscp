package me.waliedyassen.tomlrs.util

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.symbol.SymbolType

/**
 * Convert the value of this [JsonNode] to [SymbolType] object.
 */
fun JsonNode.asSymbolType(): SymbolType {
    val literal = asText() ?: error("Type literal was expected")
    return SymbolType.lookup(literal)
}

/**
 * Convert the value of this [JsonNode] to a value that is compatible of the specified [SymbolType].
 */
fun JsonNode.asValue(type: SymbolType, context: CompilationContext) = asText().parseValue(type, context)

/**
 * Convert the value of this [JsonNode] to a configuration reference id.
 */
fun JsonNode.asReference(type: SymbolType, context: CompilationContext) = asText().parseReference(type, context)

/**
 * Parse a value that is compatible of the specified [SymbolType] from the raw value of this [String].
 */
fun String.parseValue(type: SymbolType, context: CompilationContext): Any = when (type) {
    SymbolType.INT -> toInt()
    SymbolType.BOOLEAN -> if (toBoolean()) 1 else 0
    SymbolType.STRING -> this
    SymbolType.ENUM,
    SymbolType.STRUCT,
    SymbolType.PARAM,
    SymbolType.VAR_PLAYER,
    SymbolType.VAR_BIT -> parseReference(type, context)
}

/**
 * Parse a configuration reference of type [SymbolType] from the raw value of this [String].
 */
fun String.parseReference(type: SymbolType, context: CompilationContext): Int {
    if (isNullOrBlank() || this == "null") {
        return -1
    }
    val symbol = context.sym.lookup(type)[this]
    if (symbol == null) {
        context.reportError("Unresolved ${type.literal} reference to '${this}'")
        return -1
    }
    return symbol.id
}