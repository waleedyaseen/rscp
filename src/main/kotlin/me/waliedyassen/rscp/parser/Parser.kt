package me.waliedyassen.rscp.parser

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.config.Config
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.util.LiteralEnum

/**
 * Semantic information is essentially a span of text that holds a special meaning.
 */
data class SemanticInfo(
    val name: String,
    val span: Span
)

data class SyntaxSignature(val span: Span, val name: String)
data class SyntaxConfig(val span: Span, val config: Config)

/**
 * A functional interface for handling error report call backs.
 */
typealias ErrorReportHandler = (Span, String) -> Unit

/**
 * A higher level of parsing, it takes the output of [Lexer] as input and makes sure the tokens follow
 * a certain set of rules.
 */
class Parser(
    private val type: SymbolType<*>,
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
    private var parsingSignatureSpan: Span? = null

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
    fun parseConfigs(): List<Config> {
        val configs = mutableListOf<Config>()
        while (!lexer.isEof()) {
            if (lexer.skipWhitespace()) {
                continue
            }
            val config = parseConfig() ?: continue
            storeSemInfo(config.span, "definition")
            configs += config.config
        }
        return configs
    }

    /**
     * Attempt to parse a single [Config] unit.
     */
    private fun parseConfig(): SyntaxConfig? {
        val (span, name) = parseSignature() ?: return null
        val config = type.constructor(name)
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
        return SyntaxConfig(span + Span(begin, end), config)
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
    private fun parseSignature(): SyntaxSignature? {
        val left = parseLBracket() ?: return null
        val name = parseIdentifier() ?: return null
        if (name is Token.Dummy) {
            return null
        }
        val nameId = name as Token.Identifier
        storeSemInfo(name.span, "name")
        val right = parseRBracket() ?: return null
        val signature = SyntaxSignature(left.span + right.span, nameId.text)
        parsingSignatureSpan = signature.span
        return signature
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
     * Skip all the whitespace and attempt to parse a [Token.Equals] token.
     */
    private fun parseEquals(): Token? {
        lexer.skipWhitespace()
        val equals = lexer.lexEquals()
        if (equals is Token.Dummy) {
            return null
        }
        return equals
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
        val parser = Parser(SymbolType.Undefined, compiler, constant.value, extractSemInfo)
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
    fun reportConfigError(message: String) {
        reportError(parsingSignatureSpan!!, message)
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

    fun storeSemInfo(span: Span, name: String) {
        if (!extractSemInfo) return
        semInfo += SemanticInfo(name, span)
    }
}