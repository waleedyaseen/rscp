package me.waliedyassen.rscp.symbol

/**
 * Handles serialization operations for a symbol of type [T].
 */
abstract class SymbolSerializer<T : Symbol> {

    /**
     * Deserializes a [T] object from the given [String].
     */
    abstract fun deserialize(line: String): T

    /**
     * Serializes the given [T] object to a [String]
     */
    abstract fun serialize(symbol: T): String
}

/**
 * A [SymbolSerializer] implementation for [BasicSymbol] type.
 */
object BasicSymbolSerializer : SymbolSerializer<BasicSymbol>() {

    override fun deserialize(line: String): BasicSymbol {
        val parts = line.split("!")
        val name = parts[0]
        val id = parts[1].toInt()
        return BasicSymbol(name, id)
    }

    override fun serialize(symbol: BasicSymbol) = "${symbol.name}!${symbol.id}"
}

/**
 * A [SymbolSerializer] implementation for [TypedSymbol] type.
 */
object TypedSymbolSerializer : SymbolSerializer<TypedSymbol>() {

    override fun deserialize(line: String): TypedSymbol {
        val parts = line.split("!")
        val name = parts[0]
        val id = parts[1].toInt()
        val type = SymbolType.lookup(parts[2])
        return TypedSymbol(name, id, type)
    }

    override fun serialize(symbol: TypedSymbol) = "${symbol.name}!${symbol.id}!${symbol.type.literal}"
}

/**
 * A [SymbolSerializer] implementation for [ConstantSymbol] type.
 */
object ConstantSymbolSerializer : SymbolSerializer<ConstantSymbol>() {

    override fun deserialize(line: String): ConstantSymbol {
        val parts = line.split("!", limit = 3)
        val name = parts[0]
        val id = parts[1].toInt()
        val value = parts[2]
        return ConstantSymbol(name, id, value)
    }

    override fun serialize(symbol: ConstantSymbol) = "${symbol.name}!${symbol.id}!${symbol.value}"
}

/**
 * A [SymbolSerializer] implementation for [ClientScriptSymbol] type.
 */
object ClientScriptSymbolSerializer : SymbolSerializer<ClientScriptSymbol>() {

    override fun deserialize(line: String): ClientScriptSymbol {
        val parts = line.split("!", limit = 3)
        val name = parts[0]
        val id = parts[1].toInt()
        val arguments = if(parts[2].isBlank()) emptyList() else parts[2].split(",").map { SymbolType.lookup(it) }.toList()
        return ClientScriptSymbol(name, id, arguments)
    }

    override fun serialize(symbol: ClientScriptSymbol) =
        "${symbol.name}!${symbol.id}!${symbol.arguments.joinToString(",") { it.literal }}"
}