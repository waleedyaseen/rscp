package me.waliedyassen.tomlrs.parser

/**
 * Represent continuous range of data in the source code or input buffer.
 */
data class Span(val begin: Int, val end: Int) {
    companion object {
        fun empty() = Span(0, 0)
    }
}