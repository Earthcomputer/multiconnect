package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

object StmtListOp : McStmtOp() {
    override val paramTypes = emptyList<McType>() // special cased

    override fun emit(node: McNode, emitter: Emitter) {
        val inExpression = node.usages.any {
            val index = it.inputs.indexOf(node)
            it.op !is StmtListOp && it.op !is LambdaOp && (it.op !is SwitchOp<*> || index == 0) && it.op.paramTypes.getOrNull(index) != McType.VOID
        }
        if (inExpression) {
            emitter.append("<unextracted-block> {").indent().appendNewLine()
        }
        // length-based newline insertion, which properly handles empty inner nodes
        var currentLength = emitter.length
        for (input in node.inputs) {
            if (emitter.length != currentLength) {
                emitter.appendNewLine()
                currentLength = emitter.length
            }
            input.emit(emitter, Precedence.COMMA)
        }
        if (inExpression) {
            emitter.dedent().appendNewLine().append("}")
        }
    }
}
