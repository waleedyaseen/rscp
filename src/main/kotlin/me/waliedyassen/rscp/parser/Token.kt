package me.waliedyassen.rscp.parser


/**
 * The base class for all of the [Token]s produced by the [Lexer].
 */
abstract class Token {

    /**
     * The span of the token in the source code or input buffer.
     */
    abstract val span: Span

    /**
     * A dummy token, returned when there is an error in lexing.
     */
    data class Dummy(override val span: Span) : Token()

    /**
     * A token representing the '[' character.
     */
    data class LBracket(override val span: Span) : Token()

    /**
     * A token representing the ']' character.
     */
    data class RBracket(override val span: Span) : Token()

    /**
     * A token representing the '(' character.
     */
    data class LParen(override val span: Span) : Token()

    /**
     * A token representing the ')' character.
     */
    data class RParen(override val span: Span) : Token()

    /**
     * A token representing the '{' character.
     */
    data class LBrace(override val span: Span) : Token()

    /**
     * A token representing the '}' character.
     */
    data class RBrace(override val span: Span) : Token()

    /**
     * A token representing the '=' character.
     */
    data class Equals(override val span: Span) : Token()

    /**
     * A token representing the '=' character.
     */
    data class Comma(override val span: Span) : Token()

    /**
     * A token representing the '"' character.
     */
    data class Quote(override val span: Span) : Token()

    /**
     * A token representing the '^' character.
     */
    data class Caret(override val span: Span) : Token()

    /**
     * A token representing a valid string.
     */
    data class Text(override val span: Span, val text: String) : Token()

    /**
     * A token representing a valid number.
     */
    data class Number(override val span: Span, val value: Int) : Token()

    /**
     * A token representing a coord grid literal.
     */
    data class CoordGrid(override val span: Span, val value: String) : Token()

    /**
     * A token representing a valid identifier.
     */
    data class Identifier(override val span: Span, val text: String) : Token()
}