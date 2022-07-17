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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

/**
 * The kind of information that we want to extract from the compiler.
 */
enum class ExtractMode {
    None,
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
    private val extract by option("--extract")
        .enum<ExtractMode>()
        .default(ExtractMode.None)

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
            val compiler = Compiler(extract)
            compiler.readSymbols(symbolDirectory)
            val configs = if (inputDirectory != null) {
                compiler.compileDirectory(inputDirectory!!)
            } else if (inputFile != null) {
                compiler.compileFile(inputFile!!)
            } else {
                println("No input files provided")
                exitProcess(1)
            }
            if (extract != ExtractMode.None) {
                val output = when (extract) {
                    ExtractMode.Errors -> compiler.diagnostics
                    ExtractMode.SemInfo -> compiler.semanticInfo
                    else -> error("Unhandled extract mode: $extract")
                }
                val mapper = jsonMapper { }
                print(mapper.writeValueAsString(output))
                exitProcess(0)
            }
            if (compiler.diagnostics.isNotEmpty()) {
                compiler.diagnostics.forEach { logger.info { it } }
                return@measureTimeMillis
            }
            compiler.generateSymbols(configs)
            compiler.generateCode(configs, outputDirectory)
            compiler.writeSymbols(symbolDirectory)
        }
        logger.info { "Finished. Took $time ms" }
    }
}

fun main(args: Array<String>) {
    PackTool.main(args)
}