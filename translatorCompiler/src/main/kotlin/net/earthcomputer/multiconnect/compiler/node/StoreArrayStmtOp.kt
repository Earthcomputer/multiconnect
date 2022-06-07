package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class StoreArrayStmtOp(componentType: McType) : McStmtOp() {
    override val paramTypes = listOf(componentType.arrayOf(), McType.INT, componentType)
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, Precedence.PARENTHESES)
        emitter.append("[")
        node.inputs[1].emit(emitter, Precedence.COMMA)
        emitter.append("] = ")
        node.inputs[2].emit(emitter, Precedence.ASSIGNMENT)
        emitter.append(";")
    }
}
