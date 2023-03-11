package me.waliedyassen.rscp.symbol

import java.io.File

/**
 * Holds all the symbols relating to a single type.
 */
class SymbolList<T : Symbol> {

    /**
     * A look-up by name table for the symbols.
     */
    private val tableByName = LinkedHashMap<String, T>()

    /**
     * A look-up by id table for the symbols.
     */
    private val tableById = LinkedHashMap<Int, T>()

    /**
     * A collection of all the symbols in the list.
     */
    val symbols: Collection<T>
        get() = tableByName.values

    /**
     * Wheter the symbol list was modified.
     */
    var modified = false

    /**
     * Adds the specified [symbol] to the list.
     */
    fun add(symbol: T) {
        if (tableByName.containsKey(symbol.name)) {
            error("Another symbol with the same name of \"${symbol.name}\" exists in the table")
        }
        if (tableById.containsKey(symbol.id)) {
            error("Another symbol with the same id of \"${symbol.id}\" exists in the table")
        }
        tableByName[symbol.name] = symbol
        tableById[symbol.id] = symbol
        modified = true
    }

    /**
     * Removes the specified [symbol] from the list.
     */
    fun remove(symbol: T) {
        tableByName -= symbol.name
        tableById -= symbol.id
        modified = true
    }

    /**
     * Returns the symbol of type [T] with name matching the specified [name].
     */
    fun lookupByName(name: String): T? = tableByName[name]

    /**
     * Returns the symbol of type [T] with id matching the specified [id].
     */
    fun lookupById(id: Int): T? = tableById[id]

}

/**
 * A table of all the symbols used by the compiler.
 */
class SymbolTable {

    /**
     * A map of all the symbol lists in this table.
     */
    private val lists = mutableMapOf<SymbolType<*>, SymbolList<*>>()


    /**
     * Read the symbol list of the specified [type] from the specified [file].
     */
    fun <T> read(type: SymbolType<T>, file: File) where T : Symbol {
        @Suppress("UNCHECKED_CAST")
        val list = lists.getOrPut(type) { SymbolList<T>() } as SymbolList<T>
        file.readLines().forEach { line ->
            if (line.isBlank()) {
                return@forEach
            }
            val symbol = type.serializer.deserialize(line)
            list.add(symbol)
        }
        list.modified = false
    }

    /**
     * Write the symbol list of the specified [type] to the specified [file].
     */
    fun <T> write(type: SymbolType<T>, file: File) where T : Symbol {
        @Suppress("UNCHECKED_CAST")
        val list = lists.getOrPut(type) { SymbolList<T>() } as SymbolList<T>
        file.bufferedWriter().use { writer ->
            list.symbols.sortedBy { it.id }.forEach { symbol: T ->
                writer.write(type.serializer.serialize(symbol))
                writer.newLine()
            }
        }
    }

    /**
     * Add the specified [symbol] of type [T] the appropriate symbol list.
     */
    fun <T> add(type: SymbolType<T>, symbol: T) where T : Symbol {
        val list = lookupList(type)
        list.add(symbol)
    }


    /**
     * Returns the [SymbolList] for the specified [type].
     */
    fun <T> lookupList(type: SymbolType<T>): SymbolList<T> where T : Symbol {
        @Suppress("UNCHECKED_CAST")
        return lists.getOrPut(type) { SymbolList<T>() } as SymbolList<T>
    }


    /**
     * Returns the [Symbol] that matches the specified [name] and [type].
     */
    fun <T> lookupSymbol(type: SymbolType<T>, name: String): T? where T : Symbol {
        return lookupList(type).lookupByName(name)
    }

    /**
     * Returns the next free id for the specified symbol [type].
     */
    fun generateId(type: SymbolType<*>): Int {
        val list = lookupList(type)
        val highestId = list.symbols.maxOfOrNull { it.id } ?: -1
        return highestId + 1
    }

    /**
     * Clear all the stored symbols in this table.
     */
    fun clear() {
        lists.clear()
    }
}