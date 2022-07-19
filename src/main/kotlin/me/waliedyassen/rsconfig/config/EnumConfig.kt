package me.waliedyassen.rsconfig.config

import me.waliedyassen.rsconfig.Compiler
import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.parser.Reference
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.symbol.TypedSymbol

/**
 * Implementation for 'enum' type configuration.
 *
 * @author Walied K. Yassen
 */
class EnumConfig(name: String) : Config(name, SymbolType.Enum) {

    /**
     * The 'inputtype' attribute of the enum type.
     */
    private var inputType: SymbolType<*> = SymbolType.Undefined

    /**
     * The 'outputtype' attribute of the enum type.
     */
    private var outputType: SymbolType<*> = SymbolType.Undefined

    /**
     * The 'default' attribute of the enum type.
     */
    private var default: Any? = null

    /**
     * The 'val=x,y' attributes of the enum type.
     */
    private var values = LinkedHashMap<Any, Any>()

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "inputtype" -> inputType = parser.parseType() ?: return parser.skipProperty()
            "outputtype" -> outputType = parser.parseType() ?: return parser.skipProperty()
            "default" -> {
                if (outputType == SymbolType.Undefined) {
                    parser.skipProperty()
                    parser.reportPropertyError("outputtype must be specified before default")
                    return
                }
                default = parser.parseDynamic(outputType) ?: return parser.skipProperty()
            }

            "val" -> {
                if (inputType == SymbolType.Undefined) {
                    parser.reportPropertyError("inputtype must be specified before val")
                    return
                }
                if (outputType == SymbolType.Undefined) {
                    parser.reportPropertyError("outputtype must be specified before val")
                    return
                }
                val key = parser.parseDynamic(inputType) ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                val value = parser.parseDynamic(outputType) ?: return parser.skipProperty()
                values[key] = value
            }

            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (inputType == SymbolType.Undefined) {
            parser.reportConfigError("inputtype property must be specified")
            return
        }
        if (outputType == SymbolType.Undefined) {
            parser.reportConfigError("outputtype property must be specified")
            return
        }
        if (default == null) {
            parser.reportConfigError("default property must be specified")
            return
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        val transformedValues = values.map { (key, value) ->
            val transformedKey = if (key is Reference) compiler.resolveReference(key) else key
            val transformedValue = if (value is Reference) compiler.resolveReference(value) else value
            transformedKey to transformedValue
        }.toMap()
        values.clear()
        values += transformedValues
        if (default is Reference) {
            default = compiler.resolveReference(default as Reference)
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
                write4(key as Int)
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