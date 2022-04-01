package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object DoWhileStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.VOID, McType.BOOLEAN)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("do {").indent().appendNewLine()
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("} while (")
        node.inputs[1].emit(emitter, Precedence.COMMA)
        emitter.append(");")
    }
}
