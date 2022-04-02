package net.earthcomputer.multiconnect.compiler.opto

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.Precedence
import java.util.TreeMap
import java.util.TreeSet

fun McNode.optimize(): McNode {
    val optimizer = Optimizer(this)
    optimizer.optimize()
    return optimizer.rootNode
}

internal class Optimizer(var rootNode: McNode) {
    fun optimize() {
        extractCommonNodes()
        extractStatementsInExpressions()
        addTryCatchIfNecessary()
        nameVariables(rootNode)
    }

    // parent-first
    internal inline fun forEachNode(func: (McNode) -> Unit) {
        forEachNode(rootNode, func)
    }

    // depth first is dangerous while mutating the tree because the iteration may not take into account the mutations.
    // use markChanged() whenever you mutate the tree to solve.
    internal fun forEachNodeDepthFirstUnsafe(func: DepthFirstIterator.(McNode) -> Unit) {
        val itr = DepthFirstIterator()
        val visited = mutableSetOf<McNode>()
        while (itr.internalIsChanged() && rootNode !in visited) {
            itr.internalMarkUnchanged()
            forEachNodeDepthFirstUnsafe(rootNode, func, itr, visited, mutableSetOf())
        }
    }
}

private inline fun forEachNode(fromNode: McNode, func: (McNode) -> Unit) {
    val visited = mutableSetOf<McNode>()
    var toVisit = mutableSetOf(fromNode)
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

private fun forEachNodeDepthFirstUnsafe(fromNode: McNode, func: DepthFirstIterator.(McNode) -> Unit, itr: DepthFirstIterator, visited: MutableSet<McNode>, visiting: MutableSet<McNode>) {
    visiting += fromNode
    for (input in fromNode.inputs) {
        if (input !in visited && input !in visiting) {
            forEachNodeDepthFirstUnsafe(input, func, itr, visited, visiting)
            if (itr.internalIsChanged()) {
                return
            }
        }
    }
    visited += fromNode
    itr.func(fromNode)
}

internal class DepthFirstIterator {
    private var changed = true

    internal fun markChanged() {
        changed = true
    }

    internal fun internalIsChanged(): Boolean {
        return changed
    }

    internal fun internalMarkUnchanged() {
        changed = false
    }
}

private fun nameVariables(fromNode: McNode) {
    var varIndex = 1
    forEachNode(fromNode) { node ->
        for (declaredVar in node.op.declaredVariables) {
            if (!declaredVar.isImmediate) {
                declaredVar.name = "var$varIndex"
                varIndex++
            }
        }
    }
}

internal fun McNode.debugConvertToString(): String {
    nameVariables(this)
    val emitter = Emitter("foo.Bar", TreeSet(), TreeMap(), StringBuilder())
    emit(emitter.addMember("foo")!!, Precedence.COMMA)
    return emitter.createClassText()
}
