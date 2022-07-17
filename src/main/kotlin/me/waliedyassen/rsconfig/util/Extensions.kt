package me.waliedyassen.rsconfig.util

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.rsconfig.Compiler
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
fun JsonNode.asValue(type: SymbolType<*>, compiler: Compiler) = asText().parseValue(type, compiler)

/**
 * Convert the value of this [JsonNode] to a configuration reference id.
 */
fun JsonNode.asReference(type: SymbolType<*>, compiler: Compiler) = asText().parseReference(type, compiler)

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
fun String.parseValue(type: SymbolType<*>, compiler: Compiler): Any {
    if (type.isReference()) {
        return parseReference(type, compiler);
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
fun String.parseReference(type: SymbolType<*>, compiler: Compiler): Int {
    if (isNullOrBlank() || this == "null") {
        return -1
    }
    val symbol = compiler.sym.lookupList(type).lookupByName(this)
    if (symbol == null) {
        compiler.addError(Span.empty(), "Unresolved ${type.literal} reference to '${this}'")
        return -1
    }
    return symbol.id
}