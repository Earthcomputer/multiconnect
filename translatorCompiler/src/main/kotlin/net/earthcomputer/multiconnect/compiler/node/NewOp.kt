package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class NewOp(private val typeName: String, override val paramTypes: List<McType>) : McNodeOp {
    override val returnType = McType.DeclaredType(typeName)
    override val isExpensive = true
    override val precedence = Precedence.CAST
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("new ").appendClassName(typeName).append("(")
        for ((index, input) in node.inputs.withIndex()) {
            if (index != 0) {
                emitter.append(", ")
            }
            input.emit(emitter, Precedence.COMMA)
        }
        emitter.append(")")
    }
}
