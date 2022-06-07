package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object PopStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.Any)

    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(";")
    }
}
