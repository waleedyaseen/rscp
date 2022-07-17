package me.waliedyassen.rsconfig.parser

import me.waliedyassen.rsconfig.symbol.SymbolType

/**
 * Parse a parameter key and value and store them in the specified [params] map.
 */
fun Parser.parseParam(params: MutableMap<Int, Any>) {
    val paramId = parseReference(SymbolType.Param, false)
    if (paramId == -1) {
        return
    }
    val param = context.sym.lookupList(SymbolType.Param).lookupById(paramId)!!
    parseComma()
    params[paramId] = parseDynamic(param.type)
}
