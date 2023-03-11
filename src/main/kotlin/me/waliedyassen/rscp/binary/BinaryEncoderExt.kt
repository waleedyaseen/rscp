package me.waliedyassen.rscp.binary

import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * Encode the specified [params] map and write it to the buffer.
 */
fun BinaryEncoder.codeParams(side: Side, sym: SymbolTable, params: Map<Int, Any>) {
    code(249) {
        val filteredParams = if (side == Side.Server) {
            params
        } else {
            params.filter { (id, _) ->
                sym.lookupList(SymbolType.Param).lookupById(id)!!.transmit
            }
        }
        write1(filteredParams.size)
        filteredParams.forEach { (key, value) ->
            val stringValue = value is String
            write1(if (stringValue) 1 else 0)
            write3(key)
            encodeParamValue(value)
        }
    }
}

fun BinaryEncoder.encodeParamValue(value: Any) {
    if (value is String) {
        writeString(value)
    } else {
        val intValue = when (value) {
            is Boolean -> if (value) 1 else 0
            else -> value as Int
        }
        write4(intValue)
    }
}

fun BinaryEncoder.writeDbColumn(types: List<SymbolType<*>>, values: List<Any>) {
    val tupleCount = values.size / types.size
    write1or2(tupleCount)
    values.forEach { encodeParamValue(it) }
}