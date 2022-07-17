package me.waliedyassen.rsconfig.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.rsconfig.CompilationContext
import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.symbol.TypedSymbol
import me.waliedyassen.rsconfig.util.asSymbolType
import me.waliedyassen.rsconfig.util.asValue
import me.waliedyassen.rsconfig.util.parseValue

/**
 * Implementation for 'enum' type configuration.
 *
 * @author Walied K. Yassen
 */
class EnumConfig(name: String) : Config(name, SymbolType.Enum) {

    /**
     * The 'inputtype' attribute of the enum type.
     */
    private lateinit var inputType: SymbolType<*>

    /**
     * The 'outputtype' attribute of the enum type.
     */
    private lateinit var outputType: SymbolType<*>

    /**
     * The 'default' attribute of the enum type.
     */
    private lateinit var default: Any

    /**
     * The 'val=x,y' attributes of the enum type.
     */
    private var values = LinkedHashMap<Int, Any>()

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        inputType = node["inputtype"].asSymbolType()
        outputType = node["outputtype"].asSymbolType()
        default = node["default"].asValue(outputType, context)
        node["values"]?.fields()?.forEach { (key, value) ->
            val parsedKey = key.toString().parseValue(inputType, context) as Int
            val parsedValue = value.asValue(outputType, context)
            values[parsedKey] = parsedValue
        }
    }

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "inputtype" -> inputType = parser.parseType() ?: return
            "outputtype" -> outputType = parser.parseType() ?: return
            "default" -> {
                if (!this::outputType.isInitialized) {
                    parser.reportError("outputtype must be specified before default")
                    return
                }
                default = parser.parseDynamic(outputType)
            }

            "val" -> {
                if (!this::inputType.isInitialized) {
                    parser.reportError("inputtype must be specified before val")
                    return
                }
                if (!this::outputType.isInitialized) {
                    parser.reportError("outputtype must be specified before val")
                    return
                }
                val key = parser.parseDynamic(inputType) as Int
                parser.parseComma()
                val value = parser.parseDynamic(outputType)
                values[key] = value
            }

            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (!this::inputType.isInitialized) {
            parser.reportError("inputtype property must be specified")
            return
        }
        if (!this::outputType.isInitialized) {
            parser.reportError("outputtype property must be specified")
            return
        }
        if (!this::default.isInitialized) {
            parser.reportError("default property must be specified")
            return
        }
    }

    override fun createSymbol(id: Int) = TypedSymbol(name, id, outputType)

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(32)
        packet.code(1) {
            write1(inputType.legacyChar.code)
        }
        packet.code(2) {
            write1(outputType.legacyChar.code)
        }
        when (default) {
            is String -> packet.code(3) {
                writeString(default as String)
            }

            is Int -> packet.code(4) {
                write4(default as Int)
            }
        }
        val stringValues = outputType == SymbolType.String
        packet.code(if (stringValues) 5 else 6) {
            write2(values.size)
            values.forEach { (key, value) ->
                write4(key)
                if (stringValues) {
                    writeString(value as String)
                } else {
                    write4(value as Int)
                }
            }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}