package me.waliedyassen.rscp.daemon

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jsonMapper
import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.ExtractMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Writer

object DaemonProcess {

    private val JSON_MAPPER = jsonMapper {
        configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
        serializationInclusion(JsonInclude.Include.NON_NULL)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        rootLogger.level = Level.OFF;

        val reader = System.`in`.reader()
        val writer = System.out.writer()
        var compiler: Compiler? = null
        val jsonReader = JSON_MAPPER.reader()
        while (true) {
            for (command in jsonReader.readValues(JSON_MAPPER.createParser(reader), DaemonCommand::class.java)) {
                val id = command.id
                when (command.command) {
                    "init" -> {
                        val extractMode = ExtractMode.valueOf(command.params["extractMode"] as String)
                        val graphicsDirectory = File(command.params["graphicsDirectory"] as String)
                        compiler = Compiler(extractMode, graphicsDirectory)
                        writer.writeJson(DaemonMessage(id, "successful"))
                    }

                    "reload-symbols" -> {
                        compiler ?: error("Must send a successful init command first")
                        val symbolsDirectory = File(command.params["symbolsDirectory"] as String)
                        compiler.clearSymbols()
                        compiler.readSymbols(symbolsDirectory)
                        writer.writeJson(DaemonMessage(id, "successful"))
                    }

                    "compile-text" -> {
                        compiler ?: error("Must send a successful init command first")
                        val extension = command.params["extension"] as String
                        val text = command.params["text"] as String
                        val extractMode = ExtractMode.valueOf(command.params["extractMode"] as String)
                        compiler.extractMode = extractMode
                        compiler.clearRound()
                        compiler.compileText(text, extension)
                        if (extractMode == ExtractMode.SemInfo) {
                            writer.writeJson(DaemonMessage(id, compiler.semanticInfo))
                        } else if (extractMode == ExtractMode.Errors) {
                            writer.writeJson(DaemonMessage(id, compiler.diagnostics))
                        } else {
                            writer.writeJson(DaemonMessage(id, "successful"))
                        }
                    }

                    "compile-file" -> {
                        compiler ?: error("Must send a successful init command first")
                        val file = File(command.params["file"] as String)
                        val extractMode = ExtractMode.valueOf(command.params["extractMode"] as String)
                        compiler.extractMode = extractMode;
                        compiler.compileFile(file)
                    }

                    "compile-directory" -> {
                        compiler ?: error("Must send a successful init command first")
                        val directory = File(command.params["directory"] as String)
                        val extractMode = ExtractMode.valueOf(command.params["extractMode"] as String)
                        compiler.extractMode = extractMode;
                        compiler.compileDirectory(directory)
                    }
                }
            }
        }
    }

    private fun Writer.writeJson(value: Any) {
        write(JSON_MAPPER.writeValueAsString(value))
        write('\n'.code)
        flush()
    }

    data class DaemonCommand(
        @JsonProperty("id") val id: Int,
        @JsonProperty("command") val command: String,
        @JsonProperty("params") val params: Map<String, Any>
    )

    data class DaemonMessage(
        @JsonProperty("id") val id: Int,
        @JsonProperty("result") val result: Any? = null,
        @JsonProperty("error") val error: String? = null
    )
}