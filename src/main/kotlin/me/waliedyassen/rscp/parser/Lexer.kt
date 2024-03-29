package me.waliedyassen.rscp.parser

/**
 * Returns `true` if [this] character is an ascii letter.
 */
private fun Char.isAsciiLetter() = this in 'A'..'Z' || this in 'a'..'z'

/**
 * Returns `true` if [this] character is an ascii digit.
 */
private fun Char.isAsciiDigit() = this in '0'..'9'

/**
 * Returns `true` if [this] character is an ascii digit.
 */
private fun Char.isAsciiHexDigit() = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

/**
 * Returns `true` if [this] character is either an ascii digit or an ascii letter.
 */
private fun Char.isAsciiLetterOrDigit() = isAsciiLetter() || isAsciiDigit()

/**
 * Returns `true` if [this] character is suitable to be part of an identifier.
 */
private fun Char.isIdentifierPart() = isAsciiLetterOrDigit() || this == '_' || this == '+' || this == ':'

/**
 * Returns `true` if [this] character is a line feed character.
 */
private fun Char.isLineDelimiter() = this == '\r' || this == '\n'

/**
 * Returns `true` if [this] character is a string quote.
 */
private fun Char.isQuote() = this == '\"'

/**
 * The lowest level of parsing, it takes an array of characters as input and transforms them into
 * meaningful [Token] objects while validating using specific rules.
 */
class Lexer(private val input: CharArray, var errorReportHandler: ErrorReportHandler) {

    /**
     * The current index of source code in the buffer.
     */
    var index = 0

    /**
     * Returns `true` if a [Token.LBracket] can be parsed next.
     */
    fun isLBracket() = peek() == '['

    /**
     * Attempts to parse a [Token.LBracket] and returns [Token.Dummy] if it fails.
     */
    fun lexLBracket(): Token {
        if (!isLBracket()) {
            return unexpectedCharacter("[")
        }
        val start = index
        advance()
        return Token.LBracket(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.RBracket] can be parsed next.
     */
    private fun isRBracket() = peek() == ']'

    /**
     * Attempts to parse a [Token.RBracket] and returns [Token.Dummy] if it fails.
     */
    fun lexRBracket(): Token {
        if (!isRBracket()) {
            return unexpectedCharacter("]")
        }
        val start = index
        advance()
        return Token.RBracket(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.LParen] can be parsed next.
     */
    fun isLParen() = peek() == '('

    /**
     * Attempts to parse a [Token.LParen] and returns [Token.Dummy] if it fails.
     */
    fun lexLParen(): Token {
        if (!isLParen()) {
            return unexpectedCharacter("(")
        }
        val start = index
        advance()
        return Token.LParen(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.RParen] can be parsed next.
     */
    private fun isRParen() = peek() == ')'

    /**
     * Attempts to parse a [Token.RParen] and returns [Token.Dummy] if it fails.
     */
    fun lexRParen(): Token {
        if (!isRParen()) {
            return unexpectedCharacter(")")
        }
        val start = index
        advance()
        return Token.RParen(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.LBrace] can be parsed next.
     */
    fun isLBrace() = peek() == '{'

    /**
     * Attempts to parse a [Token.LBrace] and returns [Token.Dummy] if it fails.
     */
    fun lexLBrace(): Token {
        if (!isLBrace()) {
            return unexpectedCharacter("{")
        }
        val start = index
        advance()
        return Token.LBrace(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.RBrace] can be parsed next.
     */
    private fun isRBrace() = peek() == '}'

    /**
     * Attempts to parse a [Token.RBrace] and returns [Token.Dummy] if it fails.
     */
    fun lexRBrace(): Token {
        if (!isRBrace()) {
            return unexpectedCharacter("}")
        }
        val start = index
        advance()
        return Token.RBrace(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.Equals] can be parsed next.
     */
    private fun isEquals() = peek() == '='

    /**
     * Attempts to parse a [Token.Equals] and returns [Token.Dummy] if it fails.
     */
    fun lexEquals(): Token {
        if (!isEquals()) {
            return unexpectedCharacter("=")
        }
        val start = index
        advance()
        return Token.Equals(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.Comma] can be parsed next.
     */
    fun isComma() = peek() == ','

    /**
     * Attempts to parse a [Token.Comma] and returns [Token.Dummy] if it fails.
     */
    fun lexComma(): Token {
        if (!isComma()) {
            return unexpectedCharacter(",")
        }
        val start = index
        advance()
        return Token.Comma(Span(start, index))
    }


    /**
     * Returns `true` if a [Token.Quote] can be parsed next.
     */
    fun isQuote() = peek() == '"'

    /**
     * Attempts to parse a [Token.Quote] and returns [Token.Dummy] if it fails.
     */
    fun lexQuote(): Token {
        if (!isQuote()) {
            return unexpectedCharacter("")
        }
        val start = index
        advance()
        return Token.Quote(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.Comma] can be parsed next.
     */
    fun isCaret() = peek() == '^'

    /**
     * Attempts to parse a [Token.Comma] and returns [Token.Dummy] if it fails.
     */
    fun lexCaret(): Token {
        if (!isCaret()) {
            return unexpectedCharacter("^")
        }
        val start = index
        advance()
        return Token.Caret(Span(start, index))
    }

    /**
     * Returns `true` if a [Token.Modulo] can be parsed next.
     */
    fun isModulo() = peek() == '%'

    /**
     * Attempts to parse a [Token.Modulo] and returns [Token.Dummy] if it fails.
     */
    fun lexModulo(): Token {
        if (!isModulo()) {
            return unexpectedCharacter("%")
        }
        val start = index
        advance()
        return Token.Modulo(Span(start, index))
    }

    /**
     * Attempts to parse a [Token.Identifier] and returns [Token.Dummy] if it fails.
     */
    fun lexIdentifier(): Token {
        if (!peek().isIdentifierPart()) {
            return unexpectedCharacter("identifier")
        }
        val builder = StringBuilder()
        val start = index
        while (peek().isIdentifierPart()) {
            builder.append(peek())
            advance()
        }
        return Token.Identifier(Span(start, index), builder.toString())
    }

    /**
     * Attempts to parse unquoted string, the lexer will continue appending to the string until it meets
     * the end of the line or the end of the file,
     *
     * A [Token.Text] will be returned if the parsing is successful otherwise a [Token.Dummy] will be returned.
     */
    fun lexLine(): Token {
        if (isEof()) {
            return unexpectedCharacter("string")
        }
        val builder = StringBuilder()
        val start = index
        while (!isEof() && !peek().isLineDelimiter()) {
            builder.append(peek())
            advance()
        }
        skipLine()
        return Token.Text(Span(start, index), builder.toString())
    }

    /**
     * Returns `true` if a quoted string can be lexxed next.
     */
    fun isQuotedString() = peek().isQuote()

    /**
     * Attempts to parse a quoted string, a quote is expected at the beginning and ending of the string.
     *
     * A [Token.Text] will be returned if the parsing is successful otherwise a [Token.Dummy] will be returned.
     */
    fun lexQuotedString(): Token {
        if (!peek().isQuote()) {
            return unexpectedCharacter("string")
        }
        val start = index
        advance()
        val builder = StringBuilder()
        while (true) {
            if (isEof() || peek().isLineDelimiter() || peek().isQuote()) {
                break
            }
            builder.append(peek())
            advance()
        }
        if (!peek().isQuote()) {
            val span = Span(start, index)
            reportError(span, "String is not closed properly")
            return Token.Dummy(span)
        }
        advance()
        return Token.Text(Span(start, index), builder.toString())
    }

    /**
     * Attempts to parse a 32-bit signed number.
     *
     * A [Token.Number] will be returned if the parsing is successful otherwise a [Token.Dummy] will be returned.
     */
    fun lexInteger(): Token {
        val builder = StringBuilder()
        val start = index
        var sign = 1
        val radix: Int = if (peek() == '0' && peek(1) == 'x') {
            advance()
            advance()
            builder.append("0x")
            16
        } else {
            if (peek() == '+') {
                advance()
            } else if (peek() == '-') {
                advance()
                sign = -1
            }
            10
        }
        if (!if (radix == 10) peek().isAsciiDigit() else peek().isAsciiHexDigit()) {
            reportError(Span(index, index), "Expected a digit but received '${peek()}'")
            return Token.Dummy(Span(index, index))
        }
        while (if (radix == 10) peek().isAsciiDigit() else peek().isAsciiHexDigit()) {
            builder.append(peek())
            advance()
        }
        val integer = (builder.substring(if (radix == 16) 2 else 0)).toUIntOrNull(radix)
        if (integer == null) {
            reportError(Span(index, index), "Could not convert '${builder}' to a valid 32-bit number")
            return Token.Dummy(Span(index, index))
        }
        return Token.Number(Span(start, index), integer.toInt() * sign)
    }

    fun lexCoordGrid(): Token {
        val builder = StringBuilder()
        val start = index
        if (!peek().isAsciiDigit()) {
            reportError(Span(index, index), "Expected a digit but received '${peek()}'")
            return Token.Dummy(Span(index, index))
        }
        while (peek() == '_' || peek().isAsciiDigit()) {
            builder.append(peek())
            advance()
        }
        val text = builder.toString()
        if (!text.matches(COORDGRID_REGEX)) {
            reportError(Span(index, index), "Could not convert '${text}' to a valid coordgrid")
            return Token.Dummy(Span(index, index))
        }
        return Token.CoordGrid(Span(start, index), text)
    }

    /**
     * Skip all the whitespace characters which are: line delimiters, tab characters and space characters.
     */
    fun skipWhitespace() = skipWhile { it.isLineDelimiter() || it == '\t' || it == ' ' }

    /**
     * Skip the current line and all the following line delimiter characters.
     */
    fun skipLine() {
        skipWhile { !it.isLineDelimiter() }
        skipWhile { it.isLineDelimiter() }
    }

    /**
     * Skip the next sequential characters that match the specified [predicate].
     */
    private fun skipWhile(predicate: (Char) -> (Boolean)): Boolean {
        var skipped = 0
        while (!isEof() && predicate(peek())) {
            skipped++
            advance()
        }
        return skipped > 0
    }

    /**
     * Returns `true` if the next character is an end-of-file character.
     */
    fun isEof() = peek() == 0.toChar()

    /**
     * Returns the current character in the source code without advancing.
     */
    private fun peek(offset: Int = 0): Char {
        return if (index + offset >= input.size) 0.toChar() else input[index + offset]
    }

    /**
     * Advance the character position in the source code by one.
     */
    private fun advance() {
        if (index == input.size) {
            return
        }
        index++
    }

    /**
     * Report an error to the compilation compiler.
     */
    private fun reportError(span: Span, text: String) {
        errorReportHandler(span, text)
    }

    /**
     * Creates a span with the range of [begin] and [end]. The default values will point at the current character
     * in the source buffer.
     */
    private fun span(begin: Int = index, end: Int = index) = Span(begin, end)

    /**
     * Returns the current position in the input source code.
     */
    fun position() = index


    private fun unexpectedCharacter(expected: String): Token {
        val start = index
        val found = if (isEof()) "EOF" else peek()
        advance()
        val token = Token.Dummy(Span(start, index))
        reportError(token.span, "Unexpected character '${found}'. Expected '$expected'")
        return token
    }

    companion object {
        private val COORDGRID_REGEX = Regex("\\d+_\\d+_\\d+_\\d+_\\d+")
    }
}