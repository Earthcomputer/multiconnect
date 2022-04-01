package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

interface McNodeOp {
    val paramTypes: List<McType>
    val returnType: McType
    val isExpensive: Boolean
    val isExpression: Boolean get() = true
    val precedence: Precedence
    val hasSideEffects: Boolean get() = false
    val declaredVariables: List<VariableId> get() = emptyList()
    val throwsException: Boolean get() = false
    fun emit(node: McNode, emitter: Emitter)
}
