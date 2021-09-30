package net.earthcomputer.multiconnect.compiler

fun McNode.optimize(): McNode {
    Optimizer(this).optimize()
    return this
}

private class Optimizer(private val rootNode: McNode) {
    fun optimize() {
        extractCommonNodes()
        extractStatementsInExpressions()
        nameVariables()
    }

    private fun extractCommonNodes() {
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
                node.replace(McNode(LoadVariableOp(variable, node.op.returnType), mutableListOf()))
                val extractionNode = findExtractionNode(node)
                val stmtListUsage = extractionNode.usages.singleOrNull()?.takeIf { it.op == StmtListOp }
                val storeVariable = McNode(StoreVariableStmtOp(variable, node.op.returnType, true), mutableListOf(node))
                if (stmtListUsage != null) {
                    val index = stmtListUsage.inputs.indexOf(extractionNode)
                    stmtListUsage.insertInput(index, storeVariable)
                } else {
                    extractionNode.replace(McNode(StmtListOp, mutableListOf(
                        storeVariable,
                        extractionNode
                    )))
                }
                changed = true
            }
        }
    }

    // Finds the node that the given node can be extracted before such that a variable can be declared without
    // duplicates. Computes all ancestors of the node as a side effect, which the caller may optionally capture.
    private fun findExtractionNode(node: McNode, allAncestors: MutableSet<McNode> = mutableSetOf()): McNode {
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

    private fun extractStatementsInExpressions() {
        var changed = true
        while (changed) {
            changed = false
            forEachNode { node ->
                // search for statements within expressions
                if (node == rootNode || node.op.isExpression) {
                    return@forEachNode
                }
                val usage = node.usages.single()
                if (!usage.op.isExpression) {
                    return@forEachNode
                }
                val variable = VariableId.create()
                val varType = usage.op.paramTypes[usage.inputs.indexOf(node)]
                node.replace(McNode(LoadVariableOp(variable, varType), mutableListOf()))
                val parentStmt = generateSequence(usage) { it.usages.firstOrNull() }.firstOrNull { !it.op.isExpression } ?: return@forEachNode
                parentStmt.replace(McNode(StmtListOp, mutableListOf(
                    convertReturnToVariableDeclaration(node, variable, varType),
                    parentStmt
                )))
                changed = true
            }
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

        return if (hasEarlyReturnStatements) {
            McNode(ReturnAsVariableBlockOp(VariableId.create(), variable, varType), mutableListOf(stmtList))
        } else {
            McNode(ReturnAsVariableBlockOp(null, variable, varType), mutableListOf(stmtList))
        }
    }

    private fun nameVariables() {
        var varIndex = 1
        forEachNode { node ->
            for (declaredVar in node.op.declaredVariables) {
                if (!declaredVar.isImmediate) {
                    declaredVar.name = "var$varIndex"
                    varIndex++
                }
            }
        }
    }

    // parent-first
    private inline fun forEachNode(func: (McNode) -> Unit) {
        val visited = mutableSetOf<McNode>()
        var toVisit = mutableSetOf(rootNode)
        var visiting = mutableSetOf<McNode>()
        while (toVisit.isNotEmpty()) {
            visited += toVisit
            val temp = toVisit
            toVisit = visiting
            visiting = temp
            toVisit.clear()
            for (node in visiting) {
                func(node)
                for (input in node.inputs) {
                    if (input !in visited) {
                        toVisit += input
                    }
                }
            }
        }
    }
}
