package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class LoadVariableOp(private val variable: VariableId, type: McType) : McNodeOp {
    override val paramTypes = listOf<McType>()
    override val returnType = type
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES
    override fun emit(node: McNode, emitter: Emitter) {
        variable.emit(emitter)
    }
}
