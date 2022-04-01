package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class ReturnAsVariableBlockOp(
    private val blockName: VariableId?,
    private val returnedVariableName: VariableId,
    private val returnedVariableType: McType
) : McStmtOp() {
    override val paramTypes = listOf(McType.VOID)
    override val declaredVariables = listOfNotNull(blockName, returnedVariableName)

    override fun emit(node: McNode, emitter: Emitter) {
        returnedVariableType.emit(emitter)
        emitter.append(" ").append(returnedVariableName.name).append(";").appendNewLine()
        if (blockName != null) {
            emitter.append(blockName.name).append(": {").indent().appendNewLine()
        }
        emitter.pushReturnHandler { func ->
            emitter.append(returnedVariableName.name).append(" = ")
            func()
            emitter.append(";")
            if (blockName != null) {
                emitter.appendNewLine().append("break ").append(blockName.name).append(";")
            }
        }
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.popReturnHandler()
        if (blockName != null) {
            emitter.dedent().appendNewLine().append("}")
        }
    }
}
