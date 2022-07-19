package me.waliedyassen.rscp.binary

/**
 * Encode the specified [params] map and write it to the buffer.
 */
fun BinaryEncoder.codeParams(params: Map<Int, Any>) {
    code(249) {
        write1(params.size)
        params.forEach { (key, value) ->
            val stringValue = value is String
            write1(if (stringValue) 1 else 0)
            write3(key)
            if (stringValue) {
                writeString(value as String)
            } else {
                val intValue = when (value) {
                    is Boolean -> if (value) 1 else 0
                    else -> value as Int
                }
                write4(intValue)
            }
        }
    }
}
