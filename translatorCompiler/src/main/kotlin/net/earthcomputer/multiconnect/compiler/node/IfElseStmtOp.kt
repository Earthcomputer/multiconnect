package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object IfElseStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.BOOLEAN, McType.VOID, McType.VOID)

    override fun emit(node: McNode, emitter: Emitter) {
        IfStmtOp.emit(node, emitter)
        emitter.append(" else ")
        if (node.inputs[2].op is IfStmtOp || node.inputs[2].op is IfElseStmtOp) {
            // give special formatting to else-if (same line, no extra braces)
            node.inputs[2].emit(emitter, Precedence.COMMA)
        } else {
            emitter.append("{").indent().appendNewLine()
            node.inputs[2].emit(emitter, Precedence.COMMA)
            emitter.dedent().appendNewLine().append("}")
        }
    }
}
