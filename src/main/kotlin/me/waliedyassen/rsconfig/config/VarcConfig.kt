package me.waliedyassen.rsconfig.config

import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.symbol.TypedSymbol

class VarcConfig(name: String) : Config(name, SymbolType.VarClient) {

    lateinit var type: SymbolType<*>
    var scope = VarLifetime.TEMPORARY

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return
            "scope" -> scope = parser.parseEnumLiteral(VarLifetime.TEMPORARY)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (!this::type.isInitialized) {
            parser.reportError("type property must be specified")
        }
    }

    override fun createSymbol(id: Int) = TypedSymbol(name, id, type)

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(6)
        packet.code(1) {
            write1(type.legacyChar.code)
        }
        if (scope == VarLifetime.PERMANENT) {
            packet.code(2) {}
        }
        packet.terminateCode()
        return packet.toByteArray()
    }

}