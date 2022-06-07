package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class LoadFieldOp(
    private val ownerType: McType,
    private val fieldName: String,
    fieldType: McType,
    private val isStatic: Boolean = false
) : McNodeOp {
    override val paramTypes = if (isStatic) { listOf() } else { listOf(ownerType) }
    override val returnType = fieldType
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES
    override fun emit(node: McNode, emitter: Emitter) {
        if (isStatic) {
            ownerType.emit(emitter)
        } else {
            node.inputs[0].emit(emitter, Precedence.PARENTHESES)
        }
        emitter.append(".").append(fieldName)
    }
}
