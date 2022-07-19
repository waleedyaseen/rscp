package me.waliedyassen.rscp.binary

/**
 * An encoder that takes primitive data types and encodes them into byte data form.
 *
 * @author Walied K. Yassen
 */
class BinaryEncoder(expectedSize: Int) {

    /**
     * An array holding the encoded data so far.
     */
    private var data = ByteArray(expectedSize)

    /**
     * The position of the data used within the array.
     */
    private var pos = 0

    /**
     * Write a termination code.
     */
    fun terminateCode() {
        code(0) { }
    }

    /**
     * Write the specified [code] to the buffer and execute the specified [block] afterwards.
     */
    fun code(code: Int, block: BinaryEncoder.() -> Unit = {}) {
        write1(code)
        block()
    }

    /**
     * Write a 1-byte integer to the buffer.
     */
    fun write1(value: Int) {
        expandIfNecessary(1)
        data[pos++] = value.toByte()
    }

    /**
     * Write a 2-byte integer to the buffer.
     */
    fun write2(value: Int) {
        expandIfNecessary(2)
        write1(value shr 8)
        write1(value)
    }

    /**
     * Write a 3-byte integer to the buffer.
     */
    fun write3(value: Int) {
        expandIfNecessary(3)
        write1(value shr 16)
        write1(value shr 8)
        write1(value)
    }

    /**
     * Write a 4-byte integer to the buffer.
     */
    fun write4(value: Int) {
        expandIfNecessary(4)
        write1(value shr 24)
        write1(value shr 16)
        write1(value shr 8)
        write1(value)
    }

    /**
     * Write the code points of the specified [value] to the buffer followed by a 0 null-terminator.
     */
    fun writeString(value: String) {
        expandIfNecessary(value.length + 1)
        value.codePoints().forEach { write1(it) }
        write1(0)
    }

    /**
     * Expands the [data] buffer if necessary. The necessity of the expansion is only true when we do not have
     * remaining byte space is less than the specified [required] amount.
     */
    private fun expandIfNecessary(required: Int) {
        val remaining = data.size - pos
        if (required > remaining) {
            val newSize = (data.size * EXPANSION_FACTOR).toInt().coerceAtLeast(pos + required)
            data = data.copyOf(newSize)
        }
    }

    /**
     * Returns a copy of the underlying encoded data.
     */
    fun toByteArray() = data.copyOf(pos)

    companion object {

        /**
         * The expansion rate to expand by when we exceed the buffer capacity.
         */
        private const val EXPANSION_FACTOR = 1.25
    }
}