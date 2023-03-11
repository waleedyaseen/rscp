package me.waliedyassen.rscp.format.dbtable

import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.binary.encodeParamValue
import java.io.File

class DbTableIndex(tupleCount: Int = 1, types: IntArray = intArrayOf(0)) {

    private var tuples = Array(tupleCount) { DbTupleIndex(types[it]) }

    fun writeTo(file: File) {
        file.writeBytes(encode())
    }

    private fun encode(): ByteArray {
        val encoder = BinaryEncoder(256)
        encoder.writeVarInt(tuples.size)
        for (tuple in tuples) {
            encoder.write1(tuple.baseType)
            encoder.writeVarInt(tuple.rowsByValue.size)
            for ((value, rows) in tuple.rowsByValue) {
                encoder.encodeParamValue(value)
                encoder.writeVarInt(rows.size)
                for (dbrow in rows) {
                    encoder.writeVarInt(dbrow)
                }
            }
        }
        return encoder.toByteArray()
    }

    fun add(tupleIndex: Int, value: Any, dbRowId: Int) {
        val processedValue = when (value) {
            is Boolean -> if (value) 1 else 0
            is Int, is String -> value
            else -> error("Unexpected value: $value")
        }
        val tuple = tuples[tupleIndex]
        var dbRows = tuple.rowsByValue[processedValue]
        if (dbRows == null) {
            dbRows = mutableListOf()
            tuple.rowsByValue[processedValue] = dbRows
        }
        dbRows.add(dbRowId)
    }

    data class DbTupleIndex(val baseType: Int, val rowsByValue: MutableMap<Any, MutableList<Int>> = HashMap())
}