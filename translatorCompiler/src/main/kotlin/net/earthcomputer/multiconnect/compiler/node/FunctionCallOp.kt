package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class FunctionCallOp(
    private val owner: String,
    private val name: String,
    override val paramTypes: List<McType>,
    override val returnType: McType,
    override val hasSideEffects: Boolean,
    private val isStatic: Boolean = true,
    override val throwsException: Boolean = false,
) : McNodeOp {
    override val isExpensive = true
    override val precedence = Precedence.PARENTHESES

    override fun emit(node: McNode, emitter: Emitter) {
        if (isStatic) {
            if (owner != emitter.currentClass) {
                emitter.appendClassName(owner).append(".")
            }
        } else {
            node.inputs[0].emit(emitter, Precedence.PARENTHESES)
            emitter.append(".")
        }
        emitter.append(name).append("(")
        for ((index, arg) in node.inputs.asSequence().drop(if (isStatic) 0 else 1).withIndex()) {
            if (index != 0) {
                emitter.append(", ")
            }
            arg.emit(emitter, Precedence.COMMA)
        }
        emitter.append(")")
    }
}
