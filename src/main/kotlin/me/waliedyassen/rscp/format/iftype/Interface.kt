package me.waliedyassen.rscp.format.iftype

import me.waliedyassen.rscp.CodeGenerator
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.SymbolContributor
import me.waliedyassen.rscp.SymbolWithIdContributor
import me.waliedyassen.rscp.format.iftype.InterfaceType.Interface
import me.waliedyassen.rscp.symbol.*
import me.waliedyassen.rscp.util.LiteralEnum
import java.io.File

/**
 * Controls what kind of [Interface] a configuration is.
 */
@Suppress("unused")
enum class InterfaceType(val symbolType: SymbolType<out SymbolWithId>, override val literal: String) : LiteralEnum {
    Interface(SymbolType.Interface, "interface"),
    TopLevelInterface(SymbolType.TopLevelInterface, "toplevelinterface"),
    OverlayInterface(SymbolType.OverlayInterface, "overlayinterface"),
    ClientInterface(SymbolType.ClientInterface, "clientinterface")
}

/**
 * A user interface configuration, which is a collection of [Component] objects.
 */
class Interface(val type: InterfaceType, override val debugName: String, val components: List<Component>) :
    SymbolWithIdContributor<SymbolWithId>, CodeGenerator {

    override val symbolType = type.symbolType

    override fun createSymbol(id: Int) = BasicSymbol(debugName, id)

    override fun contributeSymbols(sym: SymbolTable) {
        super.contributeSymbols(sym)
        val id = sym.lookupSymbol(type.symbolType, debugName)!!.id
        val componentList = sym.lookupList(SymbolType.Component)
        val oldSym = componentList.symbols.filter { it.id shr 16 == id }.toSet()
        val newSym = components.mapIndexed { index, component ->
            component.createSymbol(id shl 16 or index) as BasicSymbol
        }.toSet()
        val deleted = oldSym - newSym
        val additions = newSym - oldSym
        deleted.forEach { componentList.remove(it) }
        additions.forEach { componentList.add(it) }
    }

    override fun generateCode(allUnits: List<CodeGenerator>, outputFolder: File, sym: SymbolTable, side: Side) {
        val interfaceDirectory = outputFolder.resolve("ifs").resolve(sym.lookupSymbol(type.symbolType, debugName)!!.id.toString())
        check(interfaceDirectory.exists() || interfaceDirectory.mkdirs())
        interfaceDirectory.listFiles()?.forEach { it.delete() }
        components.forEach {
            val symbol = sym.lookupSymbol(SymbolType.Component, it.qualifiedName)!!
            val id = symbol.id and 0xffff
            val componentFile = interfaceDirectory.resolve(id.toString())
            componentFile.writeBytes(it.encode(side, sym))
        }
    }
}