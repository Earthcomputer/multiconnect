package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class InstanceOfOp(
    inputType: McType,
    private val castType: McType
) : McNodeOp {
    override val paramTypes = listOf(inputType)
    override val returnType = McType.BOOLEAN
    override val isExpensive = false
    override val precedence = Precedence.RELATIONAL

    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, precedence)
        emitter.append(" instanceof ")
        castType.emit(emitter)
    }
}
