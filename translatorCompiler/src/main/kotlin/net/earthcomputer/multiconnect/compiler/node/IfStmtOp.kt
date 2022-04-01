package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object IfStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.BOOLEAN, McType.VOID)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("if (")
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(") {").indent().appendNewLine()
        node.inputs[1].emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("}")
    }
}
