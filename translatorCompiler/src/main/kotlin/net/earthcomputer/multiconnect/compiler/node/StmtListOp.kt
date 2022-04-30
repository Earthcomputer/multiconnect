package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object StmtListOp : McStmtOp() {
    override val paramTypes = emptyList<McType>() // special cased

    override fun emit(node: McNode, emitter: Emitter) {
        // length-based newline insertion, which properly handles empty inner nodes
        var currentLength = emitter.length
        for (input in node.inputs) {
            if (emitter.length != currentLength) {
                emitter.appendNewLine()
                currentLength = emitter.length
            }
            input.emit(emitter, Precedence.COMMA)
        }
    }
}
