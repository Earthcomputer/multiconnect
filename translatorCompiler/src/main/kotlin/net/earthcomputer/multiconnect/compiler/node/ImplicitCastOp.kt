package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class ImplicitCastOp(fromType: McType, toType: McType) : McNodeOp {
    override val paramTypes = listOf(fromType)
    override val returnType = toType
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES
    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, Precedence.COMMA)
    }
}
