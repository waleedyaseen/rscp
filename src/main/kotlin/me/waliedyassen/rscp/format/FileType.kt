package me.waliedyassen.rscp.format

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.SymbolContributor
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.format.iftype.Component
import me.waliedyassen.rscp.format.iftype.Interface
import me.waliedyassen.rscp.format.iftype.InterfaceType
import me.waliedyassen.rscp.format.value.Constant
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Span
import me.waliedyassen.rscp.parser.Token
import me.waliedyassen.rscp.symbol.SymbolType
import java.io.File

/**
 * The value returned when doing a parse call.
 */
data class ParseResult<T : SymbolContributor<*>>(val type: FileType<T>, val units: List<T>)

/**
 * Contains all the n eces
 */
sealed class FileType<T : SymbolContributor<*>> {

    /**
     * Run the specified [parser], which will essentially parse all the units that this
     * file contains. Returns a non-null [ParseResult].
     */
    abstract fun parse(parser: Parser): ParseResult<T>

    /**
     * Run any validation required by this file type. This is called after all the requested file(s) are parsed
     * by the compiler.
     */
    abstract fun validate(compiler: Compiler, result: ParseResult<T>)

    /**
     * Create a parser for the specified [file].
     */
    fun createParser(compiler: Compiler, file: File, extractSemInfo: Boolean): Parser {
        return createParser(compiler, file.readText(), extractSemInfo);
    }

    /**
     * Create a parser for the specified [text].
     */
    abstract fun createParser(compiler: Compiler, text: String, extractSemInfo: Boolean): Parser

    companion object {

        /**
         * Returns the [FileType] for the specified [extension] or null if there is no matching file type.
         */
        fun find(extension: String): FileType<*>? {
            val type = SymbolType.lookupByExtensionOrNull(extension)
            if (type?.constructor != null) {
                return ConfigFileType(type)
            }
            return when (extension) {
                "constant" -> return ConstantFileType
                "if3" -> return InterfaceFileType
                else -> null
            }
        }
    }
}

/**
 * [FileType] implementation that handles all the configuration file types.
 */
class ConfigFileType(private val type: SymbolType<*>) : FileType<Config>() {

    override fun parse(parser: Parser): ParseResult<Config> {
        return ParseResult(this, parser.parseConfigs(type).map { it.config })
    }

    override fun validate(compiler: Compiler, result: ParseResult<Config>) {
        result.units.forEach { it.resolveReferences(compiler) }
    }

    override fun createParser(compiler: Compiler, text: String, extractSemInfo: Boolean): Parser {
        return Parser(compiler, text, extractSemInfo)
    }
}

/**
 * [FileType] implementation that only handles the constant file type.
 */
object ConstantFileType : FileType<Constant>() {

    override fun parse(parser: Parser): ParseResult<Constant> {
        return ParseResult(this, parser.parseConstants())
    }

    override fun validate(compiler: Compiler, result: ParseResult<Constant>) {
        // Do nothing.
    }

    override fun createParser(compiler: Compiler, text: String, extractSemInfo: Boolean): Parser {
        return Parser(compiler, text, extractSemInfo)
    }
}

/**
 * [FileType] implementation that only handles the constant file type.
 */
object InterfaceFileType : FileType<Interface>() {

    override fun parse(parser: Parser): ParseResult<Interface> {
        var name: String? = null
        var type: InterfaceType? = null
        while (true) {
            // TODO(Walied): Find a better way around this, we do not need to use peekIdentifier()
            //  perhaps implement isIdentifier() function to avoid this.
            parser.peekIdentifier() ?: break
            val identifier = parser.parseIdentifier()!!
            parser.storeSemInfo(identifier.span, "property")
            if (parser.parseEquals() == null) {
                parser.skipProperty()
                continue
            }
            when (val propertyName = (identifier as Token.Identifier).text) {
                "name" -> {
                    name = (parser.parseIdentifier() as? Token.Identifier)?.text
                }

                "type" -> {
                    type = parser.parseEnumLiteral()
                }

                else -> {
                    parser.reportPropertyError("Unknown interface property '${propertyName}'")
                    parser.skipProperty()
                }
            }
        }
        if (name == null) {
            parser.reportError(Span(0, 0), "Missing interface 'name' property")
        }
        if (type == null) {
            parser.reportError(Span(0, 0), "Missing interface 'type' property")
        }
        val components = parser.parseConfigs(SymbolType.Component)
        if (name != null) {
            for (component in components) {
                (component.config as Component).interfaceName = name
            }
        }
        if (name == null || type == null) {
            return ParseResult(this, emptyList())
        }
        components.groupBy { it.config.debugName }.filterValues { it.size > 1 }.forEach { (name, configs) ->
            configs.forEach {
                parser.reportError(it.span, "Duplicate component name '$name'")
            }
        }
        val config = Interface(type, name, components.map { it.config as Component })
        return ParseResult(this, listOf(config))
    }

    override fun validate(compiler: Compiler, result: ParseResult<Interface>) {
        result.units.forEach { inter ->
            val prefix = "${inter.debugName}:"
            inter.components.forEach {
                if (it.debugName.indexOf(':') != -1 && !it.debugName.startsWith(prefix)) {
                    compiler.addError(Span(0, 0), "The component name must start with '$prefix'")
                }
                it.resolveReferences(compiler)
            }
        }
    }

    override fun createParser(compiler: Compiler, text: String, extractSemInfo: Boolean): Parser {
        return Parser(compiler, text, extractSemInfo)
    }
}