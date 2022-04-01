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
        nameVariables()
    }

    internal fun nameVariables() {
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
    internal inline fun forEachNode(func: (McNode) -> Unit) {
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

    internal fun McNode.debugConvertToString(): String {
        val emitter = Emitter("foo.Bar", TreeSet(), TreeMap(), StringBuilder())
        emit(emitter.addMember("foo")!!, Precedence.COMMA)
        return emitter.createClassText()
    }
}
