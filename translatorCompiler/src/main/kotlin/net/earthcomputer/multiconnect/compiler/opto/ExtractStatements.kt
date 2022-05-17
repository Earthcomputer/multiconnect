package net.earthcomputer.multiconnect.compiler.opto

import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.node.DeclareVariableOnlyStmtOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.IfStmtOp
import net.earthcomputer.multiconnect.compiler.node.LambdaOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.LabeledBlockStmtOp
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.ReturnVoidStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.VariableId

internal fun Optimizer.extractStatementsInExpressions() {
    forEachNodeDepthFirstUnsafe { node ->
        // search for statements within expressions
        if (node == rootNode || node.op.isExpression) {
            return@forEachNodeDepthFirstUnsafe
        }
        val usage = node.usages.single()
        if (usage.op is StmtListOp || usage.op is LambdaOp || (usage.op is SwitchOp<*> && node != usage.inputs[0])) {
            return@forEachNodeDepthFirstUnsafe
        }
        if (usage.op is PopStmtOp) {
            usage.replace(node)
            markChanged()
            return@forEachNodeDepthFirstUnsafe
        }
        val varType = usage.op.paramTypes[usage.inputs.indexOf(node)]
        if (varType == McType.VOID) {
            return@forEachNodeDepthFirstUnsafe
        }
        val variable = VariableId.create()
        node.replace(McNode(LoadVariableOp(variable, varType)))
        val parentStmt = generateSequence(usage) { it.usages.firstOrNull() }.firstOrNull { !it.op.isExpression } ?: return@forEachNodeDepthFirstUnsafe
        parentStmt.replace(
            McNode(StmtListOp,
                convertReturnToVariableDeclaration(node, variable, varType),
                parentStmt
            )
        )
        markChanged()
    }
}

private fun convertReturnToVariableDeclaration(stmtList: McNode, variable: VariableId, varType: McType): McNode {
    var hasEarlyReturnStatements = false
    fun findEarlyReturnStatements(n: McNode, isEarly: Boolean) {
        if (hasEarlyReturnStatements) return
        if (isEarly && n.op is ReturnStmtOp) {
            hasEarlyReturnStatements = true
            return
        }
        for ((index, input) in n.inputs.withIndex()) {
            when (n.op) {
                is SwitchOp<*> -> if (index != 0) break
                is LabeledBlockStmtOp, is LambdaOp -> break
            }

            val childIsEarly = isEarly || when (n.op) {
                is StmtListOp -> index != n.inputs.lastIndex
                is IfStmtOp, is IfElseStmtOp -> index == 0
                is ReturnStmtOp -> false
                else -> true
            }
            findEarlyReturnStatements(input, childIsEarly)
        }
    }

    findEarlyReturnStatements(stmtList, false)

    val returnStatements = mutableListOf<McNode>()
    stmtList.forEachNode { node ->
        val parent = node.usages.singleOrNull()
        if (parent?.op is SwitchOp<*> && node != parent.inputs[0]) {
            skipChildren()
            return@forEachNode
        }
        when (node.op) {
            is LabeledBlockStmtOp, is LambdaOp -> skipChildren()
            is ReturnStmtOp -> {
                val newReturnStmt = McNode(StoreVariableStmtOp(variable, varType, false),
                    node.inputs[0]
                )
                node.replace(newReturnStmt)
                returnStatements += newReturnStmt
            }
        }
    }

    val singleReturnStmt = returnStatements.singleOrNull()

    return if (singleReturnStmt != null) {
        singleReturnStmt.replace(McNode(StoreVariableStmtOp(variable, varType, true),
            singleReturnStmt.inputs[0]
        ))
        stmtList
    } else {
        for (returnStmt in returnStatements) {
            returnStmt.replace(McNode(StmtListOp,
                returnStmt,
                McNode(ReturnVoidStmtOp)
            ))
        }
        McNode(StmtListOp,
            McNode(DeclareVariableOnlyStmtOp(variable, varType)),
            McNode(LabeledBlockStmtOp(VariableId.create()),
                stmtList
            )
        )
    }
}
