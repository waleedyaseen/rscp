package me.waliedyassen.rscp.parser

/**
 * Used for distinguishing between different diagnostics.
 */
enum class DiagnosticKind {
    Error
}

/**
 * Hold all the information for a single diagnostic message.
 */
data class Diagnostic(val kind: DiagnosticKind, val span: Span, val message: String)