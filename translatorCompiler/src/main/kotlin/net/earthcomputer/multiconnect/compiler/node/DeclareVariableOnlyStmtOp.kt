package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class DeclareVariableOnlyStmtOp(private val variable: VariableId, private val type: McType) : McStmtOp() {
    override val declaredVariables = listOf(variable)
    override val paramTypes = emptyList<McType>()
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        type.emit(emitter)
        emitter.append(" ").append(variable.name).append(";")
    }

}
