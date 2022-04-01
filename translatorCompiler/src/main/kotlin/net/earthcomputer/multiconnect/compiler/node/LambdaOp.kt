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
            emitter.append(bodyParamNames.single().name)
        } else {
            emitter.append("(").append(bodyParamNames.joinToString(", ") { it.name }).append(")")
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
