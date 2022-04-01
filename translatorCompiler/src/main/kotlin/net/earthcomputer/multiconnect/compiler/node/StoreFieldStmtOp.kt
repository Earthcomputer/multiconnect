package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class StoreFieldStmtOp(ownerType: McType, private val fieldName: String, fieldType: McType) : McStmtOp() {
    override val paramTypes = listOf(ownerType, fieldType)
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, Precedence.PARENTHESES)
        emitter.append(".").append(fieldName).append(" = ")
        node.inputs[1].emit(emitter, Precedence.ASSIGNMENT)
        emitter.append(";")
    }
}
