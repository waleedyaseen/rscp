package me.waliedyassen.tomlrs.parser

import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.config.Config
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.LiteralEnum

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
 * A higher level of parsing, it takes the output of [Lexer] as input and makes sure the tokens follow
 * a certain set of rules.
 */
class Parser(
    private val type: SymbolType,
    val context: CompilationContext,
    input: String,
    /**
     * Whether we should track the semantic information of the parser
     */
    private var trackSemanticInformation: Boolean
) {

    /**
     * The underlying lexer we use for parsing tokens.
     */
    private val lexer = Lexer(input.toCharArray(), context)

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
     * Quickly run through the source code and collect the names of all defined configurations.
     */
    fun peekConfigs(): List<String> {
        val names = mutableListOf<String>()
        while (true) {
            lexer.skipWhitespace()
            if (lexer.isLBracket() || lexer.isEof()) {
                break
            }
            val sig = parseSignature()
            if (sig != null) {
                names += sig.name
            }
            while (!lexer.isLBracket() && !lexer.isEof()) {
                lexer.skipLine()
            }
        }
        return names
    }

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
        val config = type.supplier(name)
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
        if (propertyNameToken is Token.Dummy) {
            lexer.skipLine()
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
        val left = parseLBracket()
        val name = parseIdentifier()
        if (name is Token.Dummy) {
            return null
        }
        val nameId = name as Token.Identifier
        storeSemInfo(name.span, "name")
        val right = parseRBracket()
        return SyntaxSignature(left.span + right.span, nameId.text)
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.LBracket] token.
     */
    private fun parseLBracket(): Token {
        lexer.skipWhitespace()
        return lexer.lexLBracket()
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.RBracket] token.
     */
    private fun parseRBracket(): Token {
        lexer.skipWhitespace()
        return lexer.lexRBracket()
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.Equals] token.
     */
    private fun parseEquals(): Token {
        lexer.skipWhitespace()
        return lexer.lexEquals()
    }

    /**
     * Skip all the whitespace and attempt to parse a [Token.Comma] token.
     */
    fun parseComma(): Token {
        lexer.skipWhitespace()
        return lexer.lexComma()
    }

    /**
     * Attempt to parse an [Token.Identifier] and falls back to [Token.Dummy] if something occurs.
     * This will eliminate any preceding whitespace to the token.
     */
    fun parseIdentifier(): Token {
        lexer.skipWhitespace()
        return lexer.lexIdentifier()
    }

    /**
     * Attempt to parse a valid numerical value and return 0 if it fails.
     */
    fun parseInteger(): Int {
        val token = lexer.lexInteger()
        if (token is Token.Dummy) {
            return 0
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
                reportError("Unrecognized boolean literal '${identifier.text}'")
                false
            }
        }
    }

    /**
     * Attempt to parse a valid text value and return 0 if it fails.
     */
    private fun parseString(): String {
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
    fun parseType(): SymbolType? {
        val token = parseIdentifier()
        if (token is Token.Dummy) {
            return null
        }
        storeSemInfo(token.span, "type")
        val identifier = token as Token.Identifier
        val type = SymbolType.lookupOrNull(identifier.text)
        if (type == null) {
            reportError("Unrecognized type name '${identifier.text}'")
            return null
        }
        return type
    }

    /**
     * Attempt to parse a valid configuration symbol name and return the associated id for that symbol.
     * If no valid configuration is found or can be parsed, a -1 will be returned instead.
     */
    fun parseReference(type: SymbolType, permitNulls: Boolean = true): Int {
        val literal = parseIdentifier()
        if (literal is Token.Dummy) {
            return -1
        }
        storeSemInfo(literal.span, "reference")
        val identifier = literal as Token.Identifier
        if (identifier.text == "null") {
            if (!permitNulls) {
                reportError("Null values are not permitted in here")
            }
            return -1
        }
        val symbol = context.sym.lookup(type)[identifier.text]
        if (symbol == null) {
            reportError("Unresolved reference to symbol '${identifier.text}' of type '${type.literal}'")
            return -1
        }
        return symbol.id
    }

    /**
     * Attempt to parse a valid identifier then match it to a valid enum constant in [T].
     *
     * The [errorValue] exists in-case we could not parse a valid identifier or if we could not match
     * the parsed identifier to valid enum constant, in that case, the [errorValue] is returned.
     */
    inline fun <reified T> parseEnumLiteral(errorValue: T): T where T : Enum<T>, T : LiteralEnum {
        val literal = parseIdentifier()
        if (literal is Token.Dummy) {
            return errorValue
        }
        storeSemInfo(literal.span, "literal")
        val identifier = literal as Token.Identifier
        val values = enumValues<T>()
        val value = values.find { it.literal == identifier.text }
        if (value == null) {
            val validValuesMsg = values.joinToString(", ") { "'$it'" }
            reportError("Unrecognised value. Valid values are ($validValuesMsg)")
            return errorValue
        }
        return value
    }

    /**
     * Attempt to parse a dynamic value based on the given [SymbolType]
     */
    fun parseDynamic(outputType: SymbolType): Any {
        if (outputType.isReference()) {
            return parseReference(outputType, true)
        }
        return when (outputType) {
            SymbolType.STRING -> parseString()
            SymbolType.INT -> parseInteger()
            SymbolType.BOOLEAN -> parseBoolean()
            else -> error("Unexpected symbol type: $outputType")
        }
    }

    /**
     * Report an error message to the compilation context.
     */
    fun reportError(message: String) {
        context.reportError(parsingPropertySpan ?: Span.empty(), message)
    }

    /**
     * Report an error indicating the property that is being currently parsed is unknown to the configuration.
     */
    fun unknownProperty() {
        reportError("Unknown property '${parsingPropertyName!!}'")
    }

    fun storeSemInfo(span: Span, name: String) {
        if (!trackSemanticInformation) return
        semInfo += SemanticInfo(name, span)
    }
}