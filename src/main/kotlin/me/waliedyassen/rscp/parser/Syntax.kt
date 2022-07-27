package me.waliedyassen.rscp.parser

/**
 * The base class for all the syntax-tree like structs.
 */
sealed class Syntax {

    /**
     * The source code span of the syntax.
     */
    abstract val span: Span

    /**
     * The base class for all the top level units that could be parsed.
     */
    sealed class Unit : Syntax()

    /**
     * A configuration configuration signature.
     */
    data class Signature(override val span: Span, val name: String) : Syntax()

    /**
     * A single configuration entity.
     */
    data class Config(override val span: Span, val config: me.waliedyassen.rscp.config.Config) : Unit()

    /**
     *  A single constant declaration entity.
     */
    data class Constant(override val span: Span, val name: String, val value: String) : Unit()
}
