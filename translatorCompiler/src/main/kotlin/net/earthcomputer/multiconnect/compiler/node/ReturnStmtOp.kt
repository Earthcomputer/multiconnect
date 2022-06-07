package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class ReturnStmtOp(retType: McType) : McStmtOp() {
    override val paramTypes = listOf(retType)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.appendReturn { node.inputs[0].emit(emitter, Precedence.COMMA) }
    }
}
