package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class LambdaOp(
    override val returnType: McType,
    val bodyReturnType: McType,
    val bodyParamTypes: List<McType>,
    val bodyParamNames: List<VariableId>
) : McNodeOp {
    init {
        if (bodyParamTypes.size != bodyParamNames.size) {
            throw IllegalArgumentException("bodyParamTypes.size != bodyParamNames.size")
        }
    }

    override val declaredVariables = bodyParamNames
    override val paramTypes = listOf(bodyReturnType)
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES

    override fun emit(node: McNode, emitter: Emitter) {
        if (bodyParamTypes.size == 1) {
            bodyParamNames.single().emit(emitter)
        } else {
            emitter.append("(")
            for ((index, paramName) in bodyParamNames.withIndex()) {
                if (index != 0) {
                    emitter.append(", ")
                }
                paramName.emit(emitter)
            }
            emitter.append(")")
        }
        emitter.append(" -> ")
        if (node.inputs[0].op == StmtListOp) {
            emitter.append("{").indent().appendNewLine()
            emitter.pushReturnHandler { func ->
                emitter.append("return ")
                func()
                emitter.append(";")
            }
            node.inputs[0].emit(emitter, Precedence.COMMA)
            emitter.popReturnHandler()
            emitter.dedent().appendNewLine().append("}")
        } else {
            node.inputs[0].emit(emitter, Precedence.COMMA)
        }
    }
}
