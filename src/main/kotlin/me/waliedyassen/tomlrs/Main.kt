package me.waliedyassen.tomlrs

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.michaelbull.logging.InlineLogger
import me.waliedyassen.tomlrs.config.Config
import me.waliedyassen.tomlrs.config.ParamConfig
import me.waliedyassen.tomlrs.parser.Parser
import me.waliedyassen.tomlrs.parser.Span
import me.waliedyassen.tomlrs.symbol.SymbolTable
import me.waliedyassen.tomlrs.symbol.SymbolType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

abstract class ParsingConfig {
    abstract val names: List<String>
    abstract val type: SymbolType
}

data class ParsingTomlConfig(override val names: List<String>, override val type: SymbolType, val node: JsonNode) :
    ParsingConfig()

data class ParsingRsConfig(override val names: List<String>, override val type: SymbolType, val input: String) :
    ParsingConfig()

data class Error(val span: Span, val message: String)

data class CompilationContext(val sym: SymbolTable) {

    val errors = mutableListOf<Error>()

    fun reportError(span: Span, message: String) {
        errors += Error(span, message)
    }
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
     * A flag option which indicates we are looking to extract the errors only.
     */
    private val extractErrors by option(
        "-e",
        "--extract-errors",
        help = "Tells the packer to output the errors in a json format"
    ).flag()

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
            val context = CompilationContext(table)
            val configs = mutableListOf<Pair<String, Config>>()
            if (inputDirectory != null) {
                logger.info { "Parsing configs from $inputDirectory" }
                val parsingConfigs = parseTomlConfigs(inputDirectory!!) + parseRsConfigs(inputDirectory!!)
                generateConfigId(parsingConfigs, table)
                parsingConfigs.forEach {
                    if (it is ParsingTomlConfig) {
                        val config = it.type.supplier(it.names[0])
                        config.parseToml(it.node, context)
                        configs += it.names[0] to config
                    } else if (it is ParsingRsConfig) {
                        val parser = Parser(it.type, context, it.input)
                        configs += parser.parseConfigs()
                    }
                }
            } else if (inputFile != null) {
                val parsingConfig = parseRsConfig(inputFile!!)
                if (parsingConfig != null) {
                    val parser = Parser(parsingConfig.type, context, parsingConfig.input)
                    configs += parser.parseConfigs()
                }
            }
            if (extractErrors) {
                print(jsonMapper { }.writeValueAsString(context.errors))
                exitProcess(0)
            }
            if (context.errors.isNotEmpty()) {
                context.errors.forEach { logger.info { it } }
                return@measureTimeMillis
            }
            // TODO(Walied): This need to be done while we are still parsing
            assignContentType(configs.map { it.second }.toList(), table)
            if (configs.isNotEmpty()) {
                check(outputDirectory.exists() || outputDirectory.mkdirs()) { "Failed to create the output directory '$outputDirectory'" }
                logger.info { "Writing ${configs.size} configs to $outputDirectory" }
                configs.forEach {
                    val name = it.first
                    val config = it.second
                    val type = config.symbolType
                    val directory = outputDirectory.resolve(type.literal)
                    check(directory.exists() || directory.mkdirs()) { "Failed to create the output directory '$directory'" }
                    val file = directory.resolve("${table.lookupOrNull(type, name)!!.id}")
                    file.writeBytes(config.encode())
                }
            }
            writeSymbolTable(table)
        }
        logger.info { "Finished. Took $time ms" }
    }

    private fun generateConfigId(parsingConfigs: List<ParsingConfig>, table: SymbolTable) {
        parsingConfigs.forEach { config ->
            val type = config.type
            config.names.forEach { name ->
                if (table.lookupOrNull(type, name) == null) {
                    table.insert(type, name, table.generateUniqueId(type))
                }
            }
        }
    }

    private fun assignContentType(configs: List<Config>, table: SymbolTable) {
        configs.forEach { config ->
            val type = config.symbolType
            val name = config.name
            val sym = table.lookupOrNull(type, name)!!
            val content = when (config) {
                is ParamConfig -> config.type
                else -> null
            }
            if (sym.content != content) {
                sym.content = content
                table.lookup(type).modified = true
            }

        }
    }

    private fun parseTomlConfigs(folder: File): List<ParsingTomlConfig> {
        val regex = Regex("(?:.+\\.)?([^.]+)\\.toml")
        val mapper = createMapper()
        val configs = mutableListOf<ParsingTomlConfig>()
        folder.walkTopDown().forEach { file ->
            val result = regex.matchEntire(file.name) ?: return@forEach
            val literal = result.groupValues[1]
            val type = SymbolType.lookup(literal)
            val tree: JsonNode
            file.reader().use { tree = mapper.readTree(it) }
            tree.fields().asSequence().forEach {
                configs += ParsingTomlConfig(listOf(it.key), type, it.value)
            }
        }
        return configs
    }

    private fun parseRsConfigs(folder: File): List<ParsingRsConfig> {
        return folder.walkTopDown().map { parseRsConfig(it) }.filterNotNull().toList()
    }

    private fun parseRsConfig(file: File): ParsingRsConfig? {
        val extension = file.extension
        val type = SymbolType.lookupOrNull(extension) ?: return null
        val input = file.reader().use { it.readText() }
        val dummyContext = CompilationContext(SymbolTable())
        val parser = Parser(type, dummyContext, input)
        val names = parser.peekConfigs()
        return ParsingRsConfig(names, type, input)
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
            logger.info { "Parsed a total of ${table.lookup(type).symbols.size} '$literal' symbol entries" }
        }
        return table
    }

    private fun writeSymbolTable(table: SymbolTable) {
        for (type in SymbolType.values()) {
            val list = table.lookupOrNull(type) ?: continue
            if (list.modified) {
                logger.info { "Writing symbol table changes for '${type.literal}'" }
                table.write(type, symbolDirectory.resolve("${type.literal}.sym"))
            }
        }
    }

    private fun createMapper(): TomlMapper {
        val mapper = TomlMapper()
        mapper.registerModule(kotlinModule {
            configure(KotlinFeature.NullToEmptyCollection, false)
            configure(KotlinFeature.NullToEmptyMap, false)
            configure(KotlinFeature.NullIsSameAsDefault, false)
            configure(KotlinFeature.SingletonSupport, false)
            configure(KotlinFeature.StrictNullChecks, false)
        })
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
        return mapper
    }
}

fun main(args: Array<String>) {
    PackTool.main(args)
}