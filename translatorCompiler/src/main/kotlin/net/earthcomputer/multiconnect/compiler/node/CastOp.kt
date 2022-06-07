package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class CastOp(fromType: McType, private val toType: McType) : McNodeOp {
    override val paramTypes = listOf(fromType)
    override val returnType = toType
    override val isExpensive = true
    override val precedence = Precedence.CAST
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("(")
        toType.emit(emitter)
        emitter.append(") ")
        node.inputs[0].emit(emitter, precedence)
    }
}
