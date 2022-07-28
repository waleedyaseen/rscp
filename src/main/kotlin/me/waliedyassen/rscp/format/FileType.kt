package me.waliedyassen.rscp.format.handler

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.SymbolContributor
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.format.value.Constant
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolType
import java.io.File

/**
 * The value returned when doing a parse call.
 */
data class ParseResult<T : SymbolContributor>(val type: FileType<T>, val units: List<T>)

/**
 * Contains all the n eces
 */
sealed class FileType<T : SymbolContributor> {

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
    abstract fun createParser(compiler: Compiler, file: File, extractSemInfo: Boolean): Parser

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
        return ParseResult(this, parser.parseConfigs(type))
    }

    override fun validate(compiler: Compiler, result: ParseResult<Config>) {
        result.units.forEach { it.resolveReferences(compiler) }
    }

    override fun createParser(compiler: Compiler, file: File, extractSemInfo: Boolean): Parser {
        return Parser(compiler, file.readText(), extractSemInfo)
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

    override fun createParser(compiler: Compiler, file: File, extractSemInfo: Boolean): Parser {
        return Parser(compiler, file.readText(), extractSemInfo)
    }
}