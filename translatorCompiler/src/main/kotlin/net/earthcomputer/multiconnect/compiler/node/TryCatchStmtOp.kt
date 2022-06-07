package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CommonClassNames
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

internal object TryCatchStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.VOID)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("try {").indent().appendNewLine()
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine()
        emitter.append("} catch (").appendClassName(CommonClassNames.THROWABLE).append(" ex) {").indent().appendNewLine()
        emitter.append("throw ").appendClassName(CommonClassNames.PACKET_INTRINSICS).append(".sneakyThrow(ex);").dedent().appendNewLine()
        emitter.append("}")
    }
}
