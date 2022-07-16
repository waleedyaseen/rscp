package me.waliedyassen.tomlrs.symbol

import java.io.File

class SymbolList {

    private val internalSymbols = LinkedHashMap<String, Symbol>()
    val symbols get(): Map<String, Symbol> = internalSymbols
    var modified = false

    operator fun get(name: String) = internalSymbols[name]

    operator fun set(name: String, id: Int) {
        this += Symbol(name, id)
    }

    operator fun plusAssign(symbol: Symbol) {
        internalSymbols[symbol.name] = symbol
        modified = true
    }

    fun lookupById(id: Int) = symbols.values.find { it.id == id }
}

class SymbolTable {

    private val lists = mutableMapOf<SymbolType, SymbolList>()

    fun read(type: SymbolType, file: File) {
        val regex = Regex(":")
        val modified = lists[type]?.modified ?: false
        file.readLines().forEach {
            if (it.isBlank()) return@forEach
            val split = it.split(regex, 3)
            val name = split[0].lowercase()
            val id = split[1].toInt()
            val content = if (split.size > 2) SymbolType.lookup(split[2]) else null
            val list = lists.getOrPut(type) { SymbolList() }
            list += Symbol(name, id, content)
        }
        lists[type]?.modified = modified
    }

    fun write(type: SymbolType, file: File) {
        val list = lists.getOrPut(type) { SymbolList() }
        file.bufferedWriter().use {
            list.symbols.entries.sortedBy { it.value.id }.forEach { (_, symbol) ->
                it.write("${symbol.name}:${symbol.id}")
                if (symbol.content != null) {
                    it.write(":${symbol.content!!.literal}")
                }
                it.newLine()
            }
        }
    }

    fun insert(type: SymbolType, name: String, id: Int) {
        val list = lists.getOrPut(type) { SymbolList() }
        list += Symbol(name, id)
    }

    fun lookup(type: SymbolType) =
        lookupOrNull(type) ?: error("Failed to find me.waliedyassen.tomlrs.symbol list for $type")

    fun lookupOrNull(type: SymbolType, name: String) = lookupOrNull(type)?.get(name);

    fun lookupOrNull(type: SymbolType) = lists[type]

    fun generateUniqueId(type: SymbolType): Int {
        val list = lists.getOrPut(type) { SymbolList() }
        val highestId = list.symbols.maxOfOrNull { it.value.id } ?: -1
        return highestId + 1
    }
}