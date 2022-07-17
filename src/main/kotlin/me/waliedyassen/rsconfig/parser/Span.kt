package me.waliedyassen.rsconfig.parser

import kotlin.math.max
import kotlin.math.min

/**
 * Represent continuous range of data in the source code or input buffer.
 */
data class Span(val begin: Int, val end: Int) {

    operator fun plus(other: Span) = Span(min(begin, other.begin), max(end, other.end))

    companion object {
        fun empty() = Span(0, 0)
    }
}