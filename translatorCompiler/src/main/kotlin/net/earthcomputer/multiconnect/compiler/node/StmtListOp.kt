package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object StmtListOp : McStmtOp() {
    override val paramTypes = emptyList<McType>() // special cased

    override fun emit(node: McNode, emitter: Emitter) {
        for ((index, input) in node.inputs.withIndex()) {
            if (index != 0) {
                emitter.appendNewLine()
            }
            input.emit(emitter, Precedence.COMMA)
        }
    }
}
