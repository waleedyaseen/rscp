package me.waliedyassen.tomlrs.parser

import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.config.Config
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.LiteralEnum

/**
 * A higher level of parsing, it takes the output of [Lexer] as input and makes sure the tokens follow
 * a certain set of rules.
 */
class Parser(
    private val type: SymbolType,
    val context: CompilationContext,
    input: String,
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
     * Quickly run through the source code and collect the names of all defined configurations.
     */
    fun peekConfigs(): List<String> {
        val names = mutableListOf<String>()
        while (!lexer.isEof()) {
            if (lexer.skipWhitespace()) {
                continue
            }
            val name = parseSignature()
            if (name != null) {
                names += name
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
    fun parseConfigs(): List<Pair<String, Config>> {
        val configs = mutableListOf<Pair<String, Config>>()
        while (!lexer.isEof()) {
            if (lexer.skipWhitespace()) {
                // Skip whitespace and try parsing again in-case we hit an eof.
                continue
            }
            val (name, config) = parseConfig()
            if (name == null || config == null) {
                continue
            }
            configs += name to config
        }
        return configs
    }

    /**
     * Attempt to parse a single [Config] unit.
     */
    private fun parseConfig(): Pair<String?, Config?> {
        val name = parseSignature() ?: return null to null
        val config = type.supplier(name)
        // LBracket means we reached another configuration beginning, eof means halt.
        while (true) {
            lexer.skipWhitespace()
            if (lexer.isLBracket() || lexer.isEof()) {
                break
            }
            parseProperty(config)
        }
        config.verifyProperties(this)
        return name to config
    }

    /**
     * Attempt to parse a single property or skip the entire line if it is erroneous.
     */
    private fun parseProperty(config: Config) {
        val propertyNameToken = parseIdentifier()
        if (propertyNameToken is Token.Dummy) {
            lexer.skipLine()
            return
        }
        parseEquals()
        parsingPropertyName = (propertyNameToken as Token.Identifier).text
        parsingPropertySpan = propertyNameToken.span
        config.parseProperty(parsingPropertyName!!, this)
    }

    /**
     * Attempt to parse a configuration signature and return the parsed name if it is valid otherwise null.
     */
    private fun parseSignature(): String? {
        parseLBracket()
        val name = parseIdentifier()
        if (name is Token.Dummy) {
            return null
        }
        parseRBracket()
        return (name as Token.Identifier).text
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
            val text = token as Token.Text
            return text.text
        } else {
            val token = lexer.lexLine()
            if (token is Token.Dummy) {
                return ""
            }
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
        context.reportError(message)
    }

    /**
     * Report an error indicating the property that is being currently parsed is unknown to the configuration.
     */
    fun unknownProperty() {
        reportError("Unknown property '${parsingPropertyName!!}'")
    }
}