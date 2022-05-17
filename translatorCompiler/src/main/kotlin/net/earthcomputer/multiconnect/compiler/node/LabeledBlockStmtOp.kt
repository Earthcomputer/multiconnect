package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class LabeledBlockStmtOp(
    private val blockName: VariableId,
) : McStmtOp() {
    override val paramTypes = listOf(McType.VOID)
    override val declaredVariables = listOf(blockName)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(blockName.name).append(": {").indent().appendNewLine()
        emitter.pushReturnHandler {
            emitter.append("break ").append(blockName.name).append(";")
        }
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.popReturnHandler()
        emitter.dedent().appendNewLine().append("}")
    }
}
