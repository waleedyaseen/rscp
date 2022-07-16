package me.waliedyassen.tomlrs.parser

import me.waliedyassen.tomlrs.symbol.SymbolType

/**
 * Parse a parameter key and value and store them in the specified [params] map.
 */
fun Parser.parseParam(params: MutableMap<Int, Any>) {
    val paramId = parseReference(SymbolType.PARAM, false)
    if (paramId == -1) {
        return
    }
    val param = context.sym.lookup(SymbolType.PARAM).lookupById(paramId)!!
    parseComma()
    params[paramId] = parseDynamic(param.content!!)
}
