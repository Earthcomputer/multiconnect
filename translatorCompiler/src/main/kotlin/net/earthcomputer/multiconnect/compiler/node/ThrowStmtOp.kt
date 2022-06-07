package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object ThrowStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.Any)
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("throw ")
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(";")
    }
}
