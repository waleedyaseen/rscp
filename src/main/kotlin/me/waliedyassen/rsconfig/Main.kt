package me.waliedyassen.rsconfig

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.michaelbull.logging.InlineLogger
import me.waliedyassen.rsconfig.config.Config
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.parser.SemanticInfo
import me.waliedyassen.rsconfig.symbol.Symbol
import me.waliedyassen.rsconfig.symbol.SymbolList
import me.waliedyassen.rsconfig.symbol.SymbolTable
import me.waliedyassen.rsconfig.symbol.SymbolType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

data class ParsingConfig(val names: List<String>, val type: SymbolType<*>, val input: String)

/**
 * The kind of information that we want to extract from the compiler.
 */
enum class ExtractMode {
    Errors,
    SemInfo
}

object PackTool : CliktCommand() {

    private val logger = InlineLogger()

    /**
     * The directory which contains all the symbols.
     */
    private val symbolDirectory by option(help = "The symbol directory which contains the symbol table files")
        .file()
        .default(File("symbols"))

    /**
     * If present, the compiler will attempt to compile all the files within the directory.
     */
    private val inputDirectory by option(help = "The input directory which contains all the source files")
        .file()

    /**
     * If present the compiler will attempt to compile this file only.
     */
    private val inputFile by option(help = "The input file which contains the source code")
        .file()

    /**
     * The directory to place the output binaries into.
     */
    private val outputDirectory by option(help = "The output directory which the binary files will be written to")
        .file()
        .default(File("output"))

    /**
     * When present, the compiler will output JSON string with the information needed. This option has an argument
     * that tells what kind of information is needed.
     */
    private val extract by option(
        "-e",
        "--extract",
        help = "Tells the packer to output the errors in a json format"
    ).enum<ExtractMode>()

    /**
     * A flag option which indicates to turn off any logging output.
     */
    private val silent by option("-s", "--silent", help = "Run in silent mode, prevent any logging output").flag()

    override fun run() {
        if (silent) {
            val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
            root.level = Level.OFF
        }
        val time = measureTimeMillis {
            val table = readSymbolTable()
            val context = CompilerContext(table)
            val configs = mutableListOf<Pair<String, Config>>()
            var semInfo = mutableListOf<SemanticInfo>()
            if (inputDirectory != null) {
                logger.info { "Parsing configs from $inputDirectory" }
                val parsingConfigs = parseRsConfigs(inputDirectory!!)
                assignPreParseSymbol(parsingConfigs, table)
                parsingConfigs.forEach {
                    val parser = Parser(it.type, context, it.input, extract == ExtractMode.SemInfo)
                    configs += parser.parseConfigs().map { config -> config.name to config }
                    semInfo += parser.semInfo
                }
            } else if (inputFile != null) {
                val parsingConfig = parseRsConfig(inputFile!!)
                if (parsingConfig != null) {
                    val parser =
                        Parser(parsingConfig.type, context, parsingConfig.input, extract == ExtractMode.SemInfo)
                    configs += parser.parseConfigs().map { config -> config.name to config }
                    semInfo += parser.semInfo
                }
            }
            if (extract != null) {
                val output = when (extract) {
                    ExtractMode.Errors -> context.diagnostics
                    ExtractMode.SemInfo -> semInfo
                    else -> error("Unhandled extract mode: $extract")
                }
                print(jsonMapper { }.writeValueAsString(output))
                exitProcess(0)
            }
            if (context.diagnostics.isNotEmpty()) {
                context.diagnostics.forEach { logger.info { it } }
                return@measureTimeMillis
            }
            // TODO(Walied): This need to be done while we are still parsing
            assignPostParseSymbol(configs.map { it.second }.toList(), table)
            if (configs.isNotEmpty()) {
                check(outputDirectory.exists() || outputDirectory.mkdirs()) { "Failed to create the output directory '$outputDirectory'" }
                logger.info { "Writing ${configs.size} configs to $outputDirectory" }
                configs.forEach {
                    val name = it.first
                    val config = it.second
                    val type = config.symbolType
                    val directory = outputDirectory.resolve(type.literal)
                    check(directory.exists() || directory.mkdirs()) { "Failed to create the output directory '$directory'" }
                    val file = directory.resolve("${table.lookupSymbol(type, name)!!.id}")
                    file.writeBytes(config.encode())
                }
            }
            writeSymbolTable(table)
        }
        logger.info { "Finished. Took $time ms" }
    }

    private fun assignPreParseSymbol(parsingConfigs: List<ParsingConfig>, table: SymbolTable) {
//        parsingConfigs.forEach { config ->
//            val type = config.type
//            config.names.forEach { name ->
//                if (table.lookupOrNull(type, name) == null) {
//                    val symbol = BasicSymbol(name, table.generateUniqueId(type))
//                    table.add(type, symbol)
//                }
//            }
//        }
    }

    private fun assignPostParseSymbol(configs: List<Config>, table: SymbolTable) {
        configs.forEach { config ->
            val type = config.symbolType
            val name = config.name
            val old = table.lookupSymbol(type, name)
            val id = old?.id ?: table.generateId(type)
            val new = config.createSymbol(id)
            if (old != new) {
                val list = table.lookupList(type) as SymbolList<Symbol>
                if (old != null) {
                    list.remove(old)
                }
                list.add(new)
            }
        }
    }

    private fun parseRsConfigs(folder: File): List<ParsingConfig> {
        return folder.walkTopDown().map { parseRsConfig(it) }.filterNotNull().toList()
    }

    private fun parseRsConfig(file: File): ParsingConfig? {
        val extension = file.extension
        val type = SymbolType.lookupOrNull(extension) ?: return null
        val input = file.reader().use { it.readText() }
        val dummyContext = CompilerContext(SymbolTable())
        val parser = Parser(type, dummyContext, input, false)
        val names = parser.peekConfigs()
        return ParsingConfig(names, type, input)
    }

    private fun readSymbolTable(): SymbolTable {
        check(symbolDirectory.exists()) { "The specified symbols directory does not exist" }
        val table = SymbolTable()
        val regex = Regex("(\\w+)\\.sym")
        symbolDirectory.listFiles()?.forEach { file ->
            val result = regex.matchEntire(file.name) ?: return@forEach
            val literal = result.groupValues[1]
            val type = SymbolType.lookup(literal)
            table.read(type, file)
            logger.info { "Parsed a total of ${table.lookupList(type).symbols.size} '$literal' symbol entries" }
        }
        return table
    }

    private fun writeSymbolTable(table: SymbolTable) {
        for (type in SymbolType.values) {
            val list = table.lookupList(type)
            if (list.modified) {
                logger.info { "Writing symbol table changes for '${type.literal}'" }
                table.write(type, symbolDirectory.resolve("${type.literal}.sym"))
            }
        }
    }
}

fun main(args: Array<String>) {
    PackTool.main(args)
}