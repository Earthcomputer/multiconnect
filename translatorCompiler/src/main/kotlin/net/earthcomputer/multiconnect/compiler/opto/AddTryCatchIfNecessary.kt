package net.earthcomputer.multiconnect.compiler.opto

import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.TryCatchStmtOp

internal fun Optimizer.addTryCatchIfNecessary() {
    var needsTryCatch = false
    forEachNode {
        if (it.op.throwsException) {
            needsTryCatch = true
        }
    }
    if (needsTryCatch) {
        rootNode = McNode(TryCatchStmtOp, rootNode)
    }
}
