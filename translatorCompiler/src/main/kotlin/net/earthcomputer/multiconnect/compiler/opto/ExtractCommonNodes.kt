package net.earthcomputer.multiconnect.compiler.opto

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId

internal fun Optimizer.extractCommonNodes() {
    var changed = true
    while (changed) {
        changed = false
        forEachNode { node ->
            val usages = node.usages
            if (node.hasSideEffects()) {
                if (node.usages.size > 1) {
                    throw CompileException("Node with side effects has multiple usages")
                }
                return@forEachNode
            }
            if (usages.size <= 1 || !node.isExpensive()) {
                return@forEachNode
            }
            val variable = VariableId.create()
            val extractionNode = findExtractionNode(node)
            node.replace(McNode(LoadVariableOp(variable, node.op.returnType)))
            val stmtListUsage = extractionNode.usages.singleOrNull()?.takeIf { it.op == StmtListOp }
            val storeVariable = McNode(StoreVariableStmtOp(variable, node.op.returnType, true), node)
            if (stmtListUsage != null) {
                val index = stmtListUsage.inputs.indexOf(extractionNode)
                stmtListUsage.insertInput(index, storeVariable)
            } else {
                val returnNode = when {
                    !extractionNode.op.isExpression -> extractionNode
                    extractionNode.op.returnType == McType.VOID -> McNode(PopStmtOp, extractionNode)
                    else -> McNode(ReturnStmtOp(extractionNode.op.returnType), extractionNode)
                }
                extractionNode.replace(McNode(StmtListOp,
                    storeVariable,
                    returnNode
                ))
            }
            changed = true
        }
    }
}

// Finds the node that the given node can be extracted before such that a variable can be declared without
// duplicates. Computes all ancestors of the node as a side effect, which the caller may optionally capture.
private fun Optimizer.findExtractionNode(node: McNode, allAncestors: MutableSet<McNode> = mutableSetOf()): McNode {
    fun getAllAncestors(n: McNode) {
        allAncestors += n
        for (parent in n.usages) {
            if (parent !in allAncestors) {
                getAllAncestors(parent)
            }
        }
    }
    getAllAncestors(node)

    var extractionNode = rootNode
    while (true) {
        extractionNode = extractionNode.inputs.singleOrNull { it in allAncestors } ?: return extractionNode.let { result ->
            if (result.op == StmtListOp) {
                result.inputs.first { it in allAncestors }
            } else {
                result
            }
        }
    }
}
