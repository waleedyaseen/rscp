package me.waliedyassen.rscp

import me.waliedyassen.rscp.symbol.SymbolTable
import java.io.File

/**
 * A binary code generator, when implemented on units, the compiler will automatically call [generateCode]
 * when the parsing is complete.
 */
interface CodeGenerator {

    /**
     * Generate byte-code data and writes them to the specified [outputFolder]. The file the generated
     * data will be written to is determined by the implementation.
     */
    fun generateCode(outputFolder: File, sym: SymbolTable, side: Side)
}