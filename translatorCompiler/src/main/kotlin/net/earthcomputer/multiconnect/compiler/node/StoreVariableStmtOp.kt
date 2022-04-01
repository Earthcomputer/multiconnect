package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class StoreVariableStmtOp(
    private val variable: VariableId,
    private val type: McType,
    private val declare: Boolean,
    private val operator: String = "="
) : McStmtOp() {
    init {
        if (declare && operator != "=") {
            throw CompileException("Operator type for declare assignment must be =")
        }
    }

    override val paramTypes by lazy {
        when (operator) {
            "+=" -> if (type == McType.STRING) listOf(McType.Any) else listOf(type)
            "<<=", ">>=", ">>>=" -> listOf(McType.INT)
            else -> listOf(type)
        }
    }
    override val hasSideEffects = true
    override val declaredVariables = if (declare) listOf(variable) else emptyList()

    override fun emit(node: McNode, emitter: Emitter) {
        if (declare) {
            type.emit(emitter)
            emitter.append(" ")
        }
        emitter.append(variable.name).append(" ").append(operator).append(" ")
        node.inputs[0].emit(emitter, Precedence.ASSIGNMENT)
        emitter.append(";")
    }
}
