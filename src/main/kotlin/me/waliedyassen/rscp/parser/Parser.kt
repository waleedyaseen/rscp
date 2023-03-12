package me.waliedyassen.rscp.parser

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.format.value.Constant
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.util.LiteralEnum

/**
 * Semantic information is essentially a span of text that holds a special meaning.
 */
data class SemanticInfo(
    val name: String,
    val span: Span
)

/**
 * A functional interface for handling error report call backs.
 */
typealias ErrorReportHandler = (Span, String) -> Unit

/**
 * A higher level of parsing, it takes the output of [Lexer] as input and makes sure the tokens follow
 * a certain set of rules.
 */
class Parser(
    val compiler: Compiler,
    input: String,
    private var extractSemInfo: Boolean
) {

    /**
     * The error report handler function, by default it is equivalent to [Compiler.addError]
     */
    var errorReportHandler: ErrorReportHandler = compiler::addError
        set(value) {
            field = value
            lexer.errorReportHandler = value
        }

    /**
     * The underlying lexer we use for parsing tokens.
     */
    private val lexer = Lexer(input.toCharArray(), errorReportHandler)

    /**
     * The config signature span we are currently parsing
     */
    private var parsingSignatureSpan: Span = Span(lexer.position(), lexer.position())

    /**
     * The property span we are currently parsing
     */
    private var parsingPropertySpan: Span? = null

    /**
     * The property name we are currently parsing
     */
    private var parsingPropertyName: String? = null

    /**
     * A list containing all the semantic information that we tracked.
     */
    var semInfo = mutableListOf<SemanticInfo>()

    /**
     * Parse a list of all the valid [Config] in the file.
     */
    fun parseConfigs(type: SymbolType<*>): List<Syntax.Config> {
        val configs = mutableListOf<Syntax.Config>()
        while (!lexer.isEof()) {
            if (lexer.skipWhitespace()) {
                continue
            }
            val config = parseConfig(type) ?: continue
            storeSemInfo(config.span, "definition")
            configs += config
        }
        return configs
    }

    /**
     * Parse a list of all the valid [Config] in the file.
     */
    fun parseConstants(): List<Constant> {
        val constants = mutableListOf<Constant>()
        while (!lexer.isEof()) {
            if (lexer.skipWhitespace()) {
                continue
            }
            val constant = parseConstantDeclaration() ?: continue
            storeSemInfo(constant.span, "constant")
            constants += Constant(constant.name, constant.value)
        }
        return constants
    }

    /**
     * Attempt to parse a single [Config] unit.
     */
    private fun parseConfig(type: SymbolType<*>): Syntax.Config? {
        val (span, name) = parseSignature() ?: return null
        val config = type.constructor!!(name)
        val begin = lexer.position()
        var end = lexer.position()
        while (true) {
            lexer.skipWhitespace()
            if (lexer.isLBracket() || lexer.isEof()) {
                break
            }
            parseProperty(config)
            end = lexer.position()
        }
        config.verifyProperties(this)
        return Syntax.Config(span + Span(begin, end), config)
    }

    /**
     * Attempt to parse a single property or skip the entire line if it is erroneous.
     */
    private fun parseProperty(config: Config): Span? {
        val propertyNameToken = parseIdentifier()
        if (propertyNameToken == null) {
            skipProperty()
            return null
        }
        storeSemInfo(propertyNameToken.span, "property")
        parseEquals()
        parsingPropertyName = (propertyNameToken as Token.Identifier).text
        parsingPropertySpan = propertyNameToken.span

        val valueBegin = lexer.position()
        config.parseProperty(parsingPropertyName!!, this)
        val valueEnd = lexer.position()
        return propertyNameToken.span + Span(valueBegin, valueEnd)
    }

    /**
     * Attempt to parse a configuration signature and return the parsed name if it is valid otherwise null.
     */
    private fun parseSignature(): Syntax.Signature? {
        val left = parseLBracket() ?: return null
        val name = parseIdentifier() ?: return null
        if (name is Token.Dummy) {
            return null
        }
        val nameId = name as Token.Identifier
        storeSemInfo(name.span, "name")
        val right = parseRBracket() ?: return null
        val signature = Syntax.Signature(left.span + right.span, nameId.text)
        parsingSignatureSpan = signature.span
        return signature
    }

    /**
     * Attempt to parse a single constant declaration.
     */
    private fun parseConstantDeclaration(): Syntax.Constant? {
        val caret = parseCaret() ?: return skipConstantDeclaration()
        val identifier = parseIdentifier() ?: return skipConstantDeclaration()
        val equals = parseEquals() ?: return skipConstantDeclaration()
        val value = parseString()
        return Syntax.Constant(collectSpans(caret, identifier), (identifier as Token.Identifier).text, value)
    }

    /**
     * Skip the remaining of the current constant declaration.
     */
    private fun skipConstantDeclaration(): Syntax.Constant? {
        lexer.skipLine()
        return null
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.LBracket] token.
     */
    private fun parseLBracket(): Token? {
        lexer.skipWhitespace()
        val bracket = lexer.lexLBracket()
        if (bracket is Token.Dummy) {
            return null
        }
        return bracket
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.RBracket] token.
     */
    private fun parseRBracket(): Token? {
        lexer.skipWhitespace()
        val bracket = lexer.lexRBracket()
        if (bracket is Token.Dummy) {
            return null
        }
        return bracket
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.LParen] token.
     */
    fun parseLParen(): Token? {
        lexer.skipWhitespace()
        val paren = lexer.lexLParen()
        if (paren is Token.Dummy) {
            return null
        }
        return paren
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.RParen] token.
     */
    fun parseRParen(): Token? {
        lexer.skipWhitespace()
        val paren = lexer.lexRParen()
        if (paren is Token.Dummy) {
            return null
        }
        return paren
    }

    /**
     * Checks whether the next letter can be parsed as a [Token.Quote].
     */
    fun isLBrace(): Boolean {
        // TODO(Walied): We need to check for leading whitespace.
        return lexer.isLBrace()
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.LBrace] token.
     */
    fun parseLBrace(): Token? {
        lexer.skipWhitespace()
        val brace = lexer.lexLBrace()
        if (brace is Token.Dummy) {
            return null
        }
        return brace
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.RBrace] token.
     */
    fun parseRBrace(): Token? {
        lexer.skipWhitespace()
        val brace = lexer.lexRBrace()
        if (brace is Token.Dummy) {
            return null
        }
        return brace
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.Equals] token.
     */
    fun parseEquals(): Token? {
        lexer.skipWhitespace()
        val equals = lexer.lexEquals()
        if (equals is Token.Dummy) {
            return null
        }
        return equals
    }

    /**
     * Checks whether the next letter can be parsed as a [Token.Comma].
     */
    fun isComma(): Boolean {
        // TODO(Walied): We need to check for leading whitespace.
        return lexer.isComma()
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.Comma] token.
     */
    fun parseComma(): Token? {
        lexer.skipWhitespace()
        val comma = lexer.lexComma()
        if (comma is Token.Dummy) {
            return null
        }
        return comma
    }

    /**
     * Checks whether the next letter can be parsed as a [Token.Quote].
     */
    fun isQuote(): Boolean {
        // TODO(Walied): We need to check for leading whitespace.
        return lexer.isQuote()
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.Quote] token.
     */
    fun parseQuote(): Token? {
        lexer.skipWhitespace()
        val quote = lexer.lexQuote()
        if (quote is Token.Dummy) {
            return null
        }
        return quote
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.Caret] token.
     */
    private fun parseCaret(): Token? {
        lexer.skipWhitespace()
        val comma = lexer.lexCaret()
        if (comma is Token.Dummy) {
            return null
        }
        return comma
    }

    /**
     * Attempt to parse an [Token.Identifier] and falls back to [Token.Dummy] if something occurs.
     * This will eliminate any preceding whitespace to the token.
     */
    fun parseIdentifier(): Token? {
        lexer.skipWhitespace()
        val identifier = lexer.lexIdentifier()
        if (identifier is Token.Dummy) {
            return null
        }
        return identifier
    }

    /**
     * Same as [parseIdentifier] except it does not change the current lexer position or report errors.
     */
    fun peekIdentifier(): Token? {
        val errorReporter = lexer.errorReportHandler
        val position = lexer.position()
        lexer.errorReportHandler = { _, _ -> }
        lexer.skipWhitespace()
        val identifier = lexer.lexIdentifier()
        lexer.index = position
        lexer.errorReportHandler = errorReporter
        if (identifier is Token.Dummy) {
            return null
        }
        return identifier
    }

    /**
     * Attempt to parse a constant expression (^identifier) then try to convert the value of that expression
     * to an acceptable value, The function responsible for converting is provided as [block].
     */
    private fun <T> parseConstantReference(block: (Parser) -> T?): T? {
        // TODO(Walied): Handle circular references (for example ^a = ^b and ^b = ^a)
        val caret = parseCaret() ?: return null
        val name = parseIdentifier() ?: return null
        val nameText = (name as Token.Identifier).text
        storeSemInfo(caret.span + name.span, "constant")
        val constant = compiler.sym.lookupSymbol(SymbolType.Constant, nameText)
        if (constant == null) {
            reportError(name.span, "Could not resolve '${nameText}' to a constant")
            return null
        }
        val messages = mutableListOf<String>()
        val parser = Parser(compiler, constant.value, extractSemInfo)
        parser.errorReportHandler = { _, message -> messages += message }
        parser.lexer.errorReportHandler = { _, message -> messages += message }
        val result = block(parser)
        if (messages.isNotEmpty()) {
            val reasons = messages.joinToString("\n") { "    ^ $it" }
            val message = "Inconvertible value '${constant.value}' of constant '${nameText}'.\n$reasons"
            reportError(name.span, message)
        }
        return result
    }

    /**
     * Attempt to parse a valid numerical value and return 0 if it fails.
     */
    fun parseInteger(): Int? {
        if (lexer.isCaret()) {
            return parseConstantReference(Parser::parseInteger)
        }
        val token = lexer.lexInteger()
        if (token is Token.Dummy) {
            return null
        }
        storeSemInfo(token.span, "number")
        val integer = token as Token.Number
        return integer.value
    }

    /**
     * Attempt to parse a valid boolean value and return false if it fails.
     */
    fun parseBoolean(): Boolean {
        val token = lexer.lexIdentifier()
        if (token is Token.Dummy) {
            return false
        }
        storeSemInfo(token.span, "boolean")
        val identifier = token as Token.Identifier
        return when (identifier.text) {
            "true", "yes" -> true
            "false", "no" -> false
            else -> {
                reportError(identifier.span, "Unrecognized boolean literal '${identifier.text}'")
                false
            }
        }
    }

    /**
     * Attempt to parse a valid graphic value and null if it fails.
     */
    fun parseGraphic(): Reference? {
        val token = if (lexer.isQuotedString()) lexer.lexQuotedString() else parseIdentifier()
        if (token == null || token is Token.Dummy) {
            return null
        }
        storeSemInfo(token.span, "reference")
        val text = when(token) {
            is Token.Text -> token.text
            is Token.Identifier -> token.text
            else -> error("Unrecognized type: ${token::class}")
        }
        return Reference(SymbolType.Graphic, token.span, text)
    }

    /**
     * Attempt to parse a valid coord grid value and null if it fails.
     */
    private fun parseCoordGrid(): Int? {
        val token = lexer.lexCoordGrid()
        if (token is Token.Dummy) {
            return null
        }
        token as Token.CoordGrid
        storeSemInfo(token.span, "number")
        val parts = token.value.split("_").map { it.toInt() }
        val level = parts[0]
        val squareX = parts[1] shl 6 or parts[3]
        val squareY = parts[2] shl 6 or parts[4]
        return (level shl 28) or (squareX shl 14) or (squareY)
    }

    /**
     * Attempt to parse a valid text value and return 0 if it fails.
     */
    fun parseString(): String {
        // TODO(Walied): Not every place allows non quoted string
        if (lexer.isQuotedString()) {
            val token = lexer.lexQuotedString()
            if (token is Token.Dummy) {
                return ""
            }
            storeSemInfo(token.span, "string")
            val text = token as Token.Text
            return text.text
        } else {
            val token = lexer.lexLine()
            if (token is Token.Dummy) {
                return ""
            }
            storeSemInfo(token.span, "string")
            val text = token as Token.Text
            return text.text
        }
    }

    /**
     * Attempt to parse a type name literal.
     */
    fun parseType(): SymbolType<*>? {
        val token = parseIdentifier() ?: return null
        if (token is Token.Dummy) {
            return null
        }
        storeSemInfo(token.span, "type")
        val identifier = token as Token.Identifier
        val type = SymbolType.lookupOrNull(identifier.text)
        if (type == null) {
            reportError(identifier.span, "Unrecognized type name '${identifier.text}'")
            return null
        }
        return type
    }

    /**
     * Attempt to parse a type name literal.
     */
    fun parseTypeOrAutoInt(): SymbolType<*>? {
        val token = parseIdentifier() ?: return null
        if (token is Token.Dummy) {
            return null
        }
        storeSemInfo(token.span, "type")
        val identifier = token as Token.Identifier
        if (identifier.text == "autoint") {
            return SymbolType.AutoInt
        }
        val type = SymbolType.lookupOrNull(identifier.text)
        if (type == null) {
            reportError(identifier.span, "Unrecognized type name '${identifier.text}'")
            return null
        }
        return type
    }

    /**
     * Attempt to parse a valid configuration symbol name and return the associated id for that symbol.
     * If no valid configuration is found or can be parsed, a -1 will be returned instead.
     */
    fun parseReference(type: SymbolType<*>): Reference? {
        val literal = parseIdentifier() ?: return null
        storeSemInfo(literal.span, "reference")
        val identifier = literal as Token.Identifier
        return Reference(type, identifier.span, identifier.text)
    }

    /**
     * Attempt to parse a valid identifier then match it to a valid enum constant in [T].
     */
    inline fun <reified T> isEnumLiteral(): Boolean where T : Enum<T>, T : LiteralEnum {
        val literal = peekIdentifier() ?: return false
        val identifier = literal as Token.Identifier
        val values = enumValues<T>()
        return values.find { it.literal == identifier.text } != null
    }

    /**
     * Attempt to parse a valid identifier then match it to a valid enum constant in [T].
     */
    inline fun <reified T> parseEnumLiteral(): T? where T : Enum<T>, T : LiteralEnum {
        val literal = parseIdentifier() ?: return null
        storeSemInfo(literal.span, "literal")
        val identifier = literal as Token.Identifier
        val values = enumValues<T>()
        val value = values.find { it.literal == identifier.text }
        if (value == null) {
            val validValuesMsg = values.joinToString(", ") { "'${it.literal}'" }
            reportError(identifier.span, "Unrecognised value. Acceptable values are ($validValuesMsg)")
            return null
        }
        return value
    }


    /**
     * Attempt to parse a list of the given enum literal type [T]. The list will at-least contain one element,
     * if the parser fails to parse any element, a null will be returned instead.
     */
    inline fun <reified T> parseEnumLiteralList(): List<T>? where T : Enum<T>, T : LiteralEnum {
        val values = enumValues<T>()
        val result = mutableListOf<T>()
        var erroneous = false
        while(true) {
            val literal = parseIdentifier() ?: return null
            storeSemInfo(literal.span, "literal")
            val identifier = literal as Token.Identifier
            val value = values.find { it.literal == identifier.text }
            if (value == null) {
                val validValuesMsg = values.joinToString(", ") { "'${it.literal}'" }
                reportError(identifier.span, "Unrecognised value. Acceptable values are ($validValuesMsg)")
                erroneous = true
            } else {
                result += value
            }
            if (!isComma()) {
                break
            }
            parseComma()
        }
        if (erroneous) {
            return null
        }
        return result
    }

    /**
     * Attempt to parse a dynamic value based on the given [SymbolType]
     */
    fun parseDynamic(outputType: SymbolType<*>): Any? {
        if (outputType.isReference()) {
            return parseReference(outputType)
        }
        return when (outputType) {
            SymbolType.String -> parseString()
            SymbolType.Int -> parseInteger()
            SymbolType.Boolean -> parseBoolean()
            SymbolType.Graphic -> parseGraphic()
            SymbolType.CoordGrid -> parseCoordGrid()
            else -> error("Unexpected symbol type: $outputType")
        }
    }

    /**
     * Report an error message with span of the current property.
     */
    fun reportPropertyError(message: String) {
        reportError(parsingPropertySpan ?: Span.empty(), message)
    }

    /**
     * Report an error message with span of the current config signature.
     */
    fun reportUnitError(message: String) {
        reportError(parsingSignatureSpan, message)
    }

    /**
     * Report an error diagnostic to the compiler.
     */
    fun reportError(span: Span, message: String) {
        errorReportHandler(span, message)
    }

    /**
     * Report an error indicating the property that is being currently parsed is unknown to the configuration.
     */
    fun unknownProperty() {
        reportPropertyError("Unknown property '${parsingPropertyName!!}'")
        skipProperty()
    }

    /**
     * Skip the remaining of the current property.
     */
    fun skipProperty() {
        lexer.skipLine()
    }

    /**
     * Collect and merge the spans of the given [tokens] into one single [Span] that covers all the soruce
     * code range of the [tokens].
     */
    private fun collectSpans(vararg tokens: Token) = tokens.map { it.span }.reduce { a, b -> a + b }

    fun storeSemInfo(span: Span, name: String) {
        if (!extractSemInfo) return
        semInfo += SemanticInfo(name, span)
    }

}