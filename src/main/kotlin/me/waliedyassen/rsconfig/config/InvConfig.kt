package me.waliedyassen.rsconfig.config

import me.waliedyassen.rsconfig.Compiler
import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.util.LiteralEnum

enum class InvScope(val id: Int, override val literal: String) : LiteralEnum {
    TEMPORARY(0, "temp"),
    PERMANENT(1, "perm"),
}

/**
 * Implementation for 'inv' type configuration.
 *
 * @author Walied K. Yassen
 */
class InvConfig(name: String) : Config(name, SymbolType.Inv) {

    var size = 0
    var scope = InvScope.TEMPORARY

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "size" -> size = parser.parseInteger()
            "scope" -> scope = parser.parseEnumLiteral(InvScope.TEMPORARY)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(1 + if (size != 0) 3 else 0)
        if (scope != InvScope.TEMPORARY) {
            packet.code(1) {
                write1(scope.id)
            }
        }
        if (size != 0) {
            packet.code(2) {
                write2(size)
            }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}