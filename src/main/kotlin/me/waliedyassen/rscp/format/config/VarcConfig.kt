package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.TypedSymbol

class VarcConfig(override val debugName: String) : Config(SymbolType.VarClient) {

    var type: SymbolType<*> = SymbolType.Undefined
    var scope = VarLifetime.TEMPORARY

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return parser.skipProperty()
            "scope" -> scope = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "transmit" -> transmit = parser.parseBoolean()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (type == SymbolType.Undefined) {
            parser.reportUnitError("type property must be specified")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun createSymbol(id: Int) = TypedSymbol(debugName, id, type)

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val packet = BinaryEncoder(6)
        if (side == Side.Server) {
            packet.code(1) {
                write1(type.legacyChar.code)
            }
        }
        if (scope == VarLifetime.PERMANENT) {
            packet.code(2) {}
        }
        packet.terminateCode()
        return packet.toByteArray()
    }

}