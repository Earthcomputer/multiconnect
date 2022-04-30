package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object ReturnVoidStmtOp : McStmtOp() {
    override val paramTypes = emptyList<McType>()

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("return;")
    }
}
