package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.isGeneric
import net.earthcomputer.multiconnect.compiler.rawType

class NewArrayOp(private val componentType: McType) : McNodeOp {
    override val returnType = componentType.arrayOf()
    override val isExpensive = true
    override val precedence = Precedence.CAST
    override val hasSideEffects = true
    override val paramTypes = listOf(McType.INT)

    override fun emit(node: McNode, emitter: Emitter) {
        if (componentType.isGeneric) {
            emitter.append("(")
            componentType.arrayOf().emit(emitter)
            emitter.append(") ")
        }
        emitter.append("new ")
        var nonArrayType = componentType
        var extraDims = 0
        while (nonArrayType is McType.ArrayType) {
            nonArrayType = nonArrayType.elementType
            extraDims++
        }
        nonArrayType.rawType.emit(emitter)
        emitter.append("[")
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append("]")
        emitter.append("[]".repeat(extraDims))
    }
}
