package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.McType

abstract class McStmtOp : McNodeOp {
    override val isExpression = false
    override val precedence = Precedence.PARENTHESES
    final override val returnType = McType.VOID
    final override val isExpensive = true
}
