package net.earthcomputer.multiconnect.compiler.opto

import net.earthcomputer.multiconnect.compiler.node.McNode

internal fun Optimizer.removeUnusedNodes() {
    val isUnusedCache = mutableMapOf<McNode, Boolean>()
    forEachNode { node ->
        for ((index, usage) in node.usages.withIndex().reversed()) {
            if (isUnused(usage, isUnusedCache)) {
                node.removeUsageDangerously(index)
            }
        }
    }
}

internal fun Optimizer.isUnused(node: McNode, isUnusedCache: MutableMap<McNode, Boolean>): Boolean {
    if (node == rootNode) {
        return false
    }
    if (node.usages.isEmpty()) {
        return true
    }
    isUnusedCache[node]?.let { return it }
    // avoid infinite recursion by setting this node to unused (a self-referential node can still be unused)
    isUnusedCache[node] = true
    return node.usages.all { isUnused(it, isUnusedCache) }.also { isUnusedCache[node] = it }
}
