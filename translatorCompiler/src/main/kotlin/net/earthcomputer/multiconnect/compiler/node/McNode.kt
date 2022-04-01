package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.PrimitiveTypeKind

class McNode(op: McNodeOp, inputs: MutableList<McNode>) {
    constructor(op: McNodeOp, vararg inputs: McNode) : this(op, mutableListOf(*inputs))

    private var opVal = op
    var op: McNodeOp
        get() = opVal
        set(value) {
            opVal = value
            onChange()
        }

    private val mutableInputs = inputs
    val inputs: List<McNode> get() = mutableInputs
    private val mutableUsages = mutableListOf<McNode>()
    val usages: List<McNode> get() = mutableUsages

    init {
        for (input in inputs) {
            input.mutableUsages += this
        }
        onChange()
    }

    private fun onChange(depths: MutableMap<McNode, Int> = mutableMapOf(this to 0)) {
        hasSideEffectsCache = null
        isExpensiveCache = null
        val depth = depths[this]!!
        for (usage in usages) {
            val usageDepth = depths[usage]
            if (usageDepth == null || depth + 1 < usageDepth) {
                depths[usage] = depth + 1
                usage.onChange(depths)
            }
        }
        if (depth >= 2) return

        if (op != StmtListOp) {
            if (op.paramTypes.size != inputs.size || !op.paramTypes.zip(inputs).all { (paramType, arg) ->
                    paramType == arg.op.returnType || paramType == McType.Any || arg.op == StmtListOp
                }) {
                throw CompileException("Argument types (${inputs.joinToString(", ") { it.op.returnType.toString() }}) do not match parameter types (${op.paramTypes.joinToString(", ")})")
            }
            if (!inputs.all { it.op.isExpression || it.op == StmtListOp }) {
                throw CompileException("All inputs must be expressions")
            }
        } else {
            if (inputs.any { it.op.isExpression }) {
                throw CompileException("All inputs must be statements")
            }
        }
    }

    fun setInput(index: Int, value: McNode) {
        val oldValue = mutableInputs.set(index, value)
        oldValue.mutableUsages.remove(this)
        value.mutableUsages += this
        onChange()
    }

    fun insertInput(index: Int, value: McNode) {
        mutableInputs.add(index, value)
        value.mutableUsages += this
        onChange()
    }

    fun removeInput(index: Int) {
        val oldValue = mutableInputs.removeAt(index)
        oldValue.mutableUsages.remove(this)
        onChange()
    }

    fun replace(other: McNode) {
        val exceptions = mutableSetOf<McNode>()
        fun addExceptions(n: McNode) {
            exceptions += n
            for (input in n.inputs) {
                if (input != this && input !in exceptions) {
                    addExceptions(input)
                }
            }
        }
        addExceptions(other)

        for (usage in usages.toList()) {
            if (usage in exceptions) continue
            for ((index, input) in usage.inputs.withIndex()) {
                if (input == this) {
                    usage.setInput(index, other)
                }
            }
        }
    }

    fun emit(emitter: Emitter, parentPrecedence: Precedence) {
        if (parentPrecedence < op.precedence) {
            emitter.append("(")
        }
        op.emit(this, emitter)
        if (parentPrecedence < op.precedence) {
            emitter.append(")")
        }
    }

    fun castIfNecessary(targetType: McType): McNode {
        val currentType = op.returnType
        if (targetType == currentType) {
            return this
        }
        if (currentType is McType.PrimitiveType && targetType is McType.PrimitiveType) {
            if (currentType.kind == PrimitiveTypeKind.FLOAT && targetType.kind == PrimitiveTypeKind.DOUBLE) {
                return McNode(ImplicitCastOp(currentType, targetType), this)
            }
            if (targetType.kind == PrimitiveTypeKind.LONG) {
                return McNode(ImplicitCastOp(currentType, targetType), this)
            }
            if (targetType.kind == PrimitiveTypeKind.INT && currentType.kind != PrimitiveTypeKind.LONG) {
                return McNode(ImplicitCastOp(currentType, targetType), this)
            }
        }
        return McNode(CastOp(currentType, targetType), this)
    }

    private var hasSideEffectsCache: Boolean? = null
    fun hasSideEffects(): Boolean = hasSideEffectsCache
        ?: (op.hasSideEffects || inputs.any { it.hasSideEffects() }).also { hasSideEffectsCache = it }
    private var isExpensiveCache: Boolean? = null
    fun isExpensive(): Boolean = isExpensiveCache
        ?: (op.isExpensive || inputs.any { it.isExpensive() }).also { isExpensiveCache = it }
}
