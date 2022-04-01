package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class LoadArrayOp(componentType: McType) : McNodeOp {
    override val paramTypes = listOf(componentType.arrayOf(), McType.INT)
    override val returnType = componentType
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES
    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, Precedence.PARENTHESES)
        emitter.append("[")
        node.inputs[1].emit(emitter, Precedence.COMMA)
        emitter.append("]")
    }
}
