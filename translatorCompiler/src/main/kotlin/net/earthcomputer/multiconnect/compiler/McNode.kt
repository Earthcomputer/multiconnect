package net.earthcomputer.multiconnect.compiler

import java.util.Collections
import java.util.SortedSet

class McNode(op: McNodeOp, inputs: MutableList<McNode>) {
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

        for (usage in usages) {
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

    private var hasSideEffectsCache: Boolean? = null
    fun hasSideEffects(): Boolean = hasSideEffectsCache
        ?: (op.hasSideEffects || inputs.any { it.hasSideEffects() }).also { hasSideEffectsCache = it }
    private var isExpensiveCache: Boolean? = null
    fun isExpensive(): Boolean = isExpensiveCache
        ?: (op.isExpensive || inputs.any { it.isExpensive() }).also { isExpensiveCache = it }
}

enum class Precedence {
    PARENTHESES,
    POSTFIX,
    UNARY,
    CAST,
    MULTIPLICATIVE,
    ADDITIVE,
    SHIFT,
    RELATIONAL,
    EQUALITY,
    BITWISE,
    LOGICAL,
    TERNARY,
    ASSIGNMENT,
    COMMA
}

class VariableId private constructor(var isImmediate: Boolean) {
    lateinit var name: String

    override fun hashCode() = if (isImmediate) {
        name.hashCode()
    } else {
        System.identityHashCode(this)
    }

    override fun equals(other: Any?) = if (isImmediate) {
        other is VariableId && other.isImmediate && name == other.name
    } else {
        this === other
    }

    companion object {
        fun immediate(name: String): VariableId {
            val result = VariableId(true)
            result.name = name
            return result
        }

        fun create(): VariableId {
            return VariableId(false)
        }
    }
}

interface McNodeOp {
    val paramTypes: List<McType>
    val returnType: McType
    val isExpensive: Boolean
    val isExpression: Boolean get() = true
    val precedence: Precedence
    val hasSideEffects: Boolean get() = false
    val declaredVariables: List<VariableId> get() = emptyList()
    fun emit(node: McNode, emitter: Emitter)
}

class CastOp(fromType: McType, private val toType: McType) : McNodeOp {
    override val paramTypes = listOf(fromType)
    override val returnType = toType
    override val isExpensive = true
    override val precedence = Precedence.CAST
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("(")
        toType.emit(emitter)
        emitter.append(") ")
        node.inputs[0].emit(emitter, precedence)
    }
}

abstract class CstOp : McNodeOp {
    override val paramTypes = listOf<McType>()
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES
}

class CstBoolOp(private val value: Boolean) : CstOp() {
    override val returnType = McType.BOOLEAN
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(value.toString())
    }
}

class CstIntOp(private val value: Int) : CstOp() {
    override val returnType = McType.INT
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(value.toString())
    }
}

class CstLongOp(private val value: Long) : CstOp() {
    override val returnType = McType.LONG
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(value.toString()).append("L")
    }
}

class CstDoubleOp(private val value: Double) : CstOp() {
    override val returnType = McType.DOUBLE
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(value.toString())
    }
}

class CstStringOp(private val value: String) : CstOp() {
    override val returnType = McType.STRING
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("\"${escape(value)}\"")
    }
    private fun escape(str: String): String {
        return str.asSequence()
            .flatMap {
                when (it) {
                    '\\' -> "\\\\".asSequence()
                    '\n' -> "\\n".asSequence()
                    '\r' -> "\\r".asSequence()
                    '\t' -> "\\t".asSequence()
                    '"' -> "\\\"".asSequence()
                    in '\u0000'..'\u001f', in '\u007f'..'\u009f', in '\u0100'..'\uffff' -> "\\u${String.format("%04x", it.code)}".asSequence()
                    else -> sequenceOf(it)
                }
            }
            .joinToString("")
    }
}

fun createCstOp(value: Any): CstOp {
    return when (value) {
        is Boolean -> CstBoolOp(value)
        is Int -> CstIntOp(value)
        is Long -> CstLongOp(value)
        is Double -> CstDoubleOp(value)
        is String -> CstStringOp(value)
        else -> throw CompileException("Invalid constant $value")
    }
}

class LoadVariableOp(val variable: VariableId, type: McType) : McNodeOp {
    constructor(variable: String, type: McType) : this(VariableId.immediate(variable), type)

    override val paramTypes = listOf<McType>()
    override val returnType = type
    override val isExpensive = false
    override val precedence = Precedence.PARENTHESES
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(variable.name)
    }
}

class BinaryExpressionOp(private val operator: String, leftType: McType, rightType: McType) : McNodeOp {
    init {
        if (leftType == McType.STRING) {
            if (operator != "+") {
                throw CompileException("Strings can only support the + operator")
            }
        } else if (operator == "<<" || operator == ">>" || operator == ">>>") {
            if (rightType != McType.INT) {
                throw CompileException("Can only shift by integers")
            }
        } else {
            if (leftType != rightType) {
                throw CompileException("Incompatible operand types $leftType and $rightType")
            }
        }
    }

    override val paramTypes = listOf(leftType, rightType)
    override val returnType by lazy {
        when (operator) {
            "==", "!=", "<", ">", "<=", ">=" -> McType.BOOLEAN
            else -> leftType
        }
    }
    override val isExpensive = true
    override val precedence by lazy {
        when (operator) {
            "*", "/", "%" -> Precedence.MULTIPLICATIVE
            "+", "-" -> Precedence.ADDITIVE
            "<<", ">>", ">>>" -> Precedence.SHIFT
            "<", "<=", ">", ">=" -> Precedence.RELATIONAL
            "==", "!=" -> Precedence.EQUALITY
            "&", "^", "|", "&&", "||" -> Precedence.BITWISE
            else -> throw CompileException("Unknown binary operator $operator")
        }
    }

    private val isAssociative by lazy {
        when (operator) {
            "/", "%", "-", "<<", ">>", ">>>", "<", "<=", ">", ">=" -> false
            else -> true
        }
    }

    override fun emit(node: McNode, emitter: Emitter) {
        // always insert parentheses for bitwise, equality and relational operations
        node.inputs[0].emit(emitter, if (precedence == Precedence.RELATIONAL || precedence == Precedence.EQUALITY || precedence == Precedence.BITWISE) Precedence.SHIFT else precedence)
        emitter.append(" ").append(operator).append(" ")
        // if the right-hand side must come first but has the same precedence, make sure we're associative or else
        // up our precedence to make sure parentheses are inserted
        node.inputs[1].emit(emitter, if (isAssociative) precedence else Precedence.values()[precedence.ordinal - 1])
    }
}

class FunctionCallOp(
    private val owner: String,
    private val name: String,
    override val paramTypes: List<McType>,
    override val returnType: McType,
    override val hasSideEffects: Boolean,
    private val isStatic: Boolean = true
) : McNodeOp {
    override val isExpensive = true
    override val precedence = Precedence.PARENTHESES

    override fun emit(node: McNode, emitter: Emitter) {
        if (isStatic) {
            if (owner != emitter.currentClass) {
                emitter.appendClassName(owner).append(".")
            }
        } else {
            node.inputs[0].emit(emitter, Precedence.PARENTHESES)
            emitter.append(".")
        }
        emitter.append(name).append("(")
        for ((index, arg) in node.inputs.asSequence().drop(if (isStatic) 0 else 1).withIndex()) {
            if (index != 0) {
                emitter.append(", ")
            }
            arg.emit(emitter, Precedence.COMMA)
        }
        emitter.append(")")
    }
}

class NewOp(private val typeName: String, override val paramTypes: List<McType>) : McNodeOp {
    override val returnType = McType.DeclaredType(typeName)
    override val isExpensive = true
    override val precedence = Precedence.CAST
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("new ").appendClassName(typeName).append("(")
        for ((index, input) in node.inputs.withIndex()) {
            if (index != 0) {
                emitter.append(", ")
            }
            input.emit(emitter, Precedence.COMMA)
        }
        emitter.append(")")
    }
}

class SwitchOp<T: Any>(
    private val cases: SortedSet<T>,
    private val hasDefault: Boolean,
    expectedType: McType,
    override val returnType: McType
) : McNodeOp {
    override val paramTypes = listOf(expectedType) + Collections.nCopies(cases.size, returnType) + listOfNotNull(returnType.takeIf { hasDefault })
    override val isExpensive = true
    override val precedence = Precedence.PARENTHESES

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("switch (")
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(") {").indent()
        val yieldReturner: ReturnHandler = { func ->
            emitter.append("yield ")
            func()
            emitter.append(";")
        }
        for ((key, handler) in cases.asSequence().zip(node.inputs.asSequence().drop(1))) {
            emitter.appendNewLine().append("case ")
            McNode(createCstOp(key), mutableListOf()).emit(emitter, Precedence.COMMA)
            emitter.append(" -> ")
            if (handler.op.isExpression) {
                handler.emit(emitter, Precedence.COMMA)
                emitter.append(";")
            } else {
                emitter.append("{").indent().appendNewLine()
                emitter.pushReturnHandler(yieldReturner)
                handler.emit(emitter, Precedence.COMMA)
                emitter.popReturnHandler()
                emitter.dedent().appendNewLine().append("}")
            }
        }
        if (hasDefault) {
            emitter.appendNewLine().append("default -> ")
            if (node.inputs.last().op.isExpression) {
                node.inputs.last().emit(emitter, Precedence.COMMA)
                emitter.append(";")
            } else {
                emitter.append("{").indent().appendNewLine()
                emitter.pushReturnHandler(yieldReturner)
                node.inputs.last().emit(emitter, Precedence.COMMA)
                emitter.popReturnHandler()
                emitter.dedent().appendNewLine().append("}")
            }
        }
        emitter.dedent().appendNewLine().append("}")
    }
}

abstract class McStmtOp : McNodeOp {
    override val isExpression = false
    override val precedence = Precedence.PARENTHESES
    final override val returnType = McType.VOID
    final override val isExpensive = true
}

object PopStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.Any)

    override fun emit(node: McNode, emitter: Emitter) {
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(";")
    }
}

class ReturnStmtOp(retType: McType) : McStmtOp() {
    override val paramTypes = listOf(retType)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.appendReturn { node.inputs[0].emit(emitter, Precedence.COMMA) }
    }
}

class StoreVariableStmtOp(
    val variable: VariableId,
    private val type: McType,
    val declare: Boolean,
    private val operator: String = "="
) : McStmtOp() {
    constructor(variable: String, type: McType, declare: Boolean, operator: String = "=") : this(VariableId.immediate(variable), type, declare, operator)

    init {
        if (declare && operator != "=") {
            throw CompileException("Operator type for declare assignment must be =")
        }
    }

    override val paramTypes by lazy {
        when (operator) {
            "+=" -> if (type == McType.STRING) listOf(McType.Any) else listOf(type)
            "<<=", ">>=", ">>>=" -> listOf(McType.INT)
            else -> listOf(type)
        }
    }
    override val hasSideEffects = true
    override val declaredVariables = if (declare) listOf(variable) else emptyList()

    override fun emit(node: McNode, emitter: Emitter) {
        if (declare) {
            type.emit(emitter)
            emitter.append(" ")
        }
        emitter.append(variable.name).append(" ").append(operator).append(" ")
        node.inputs[0].emit(emitter, Precedence.ASSIGNMENT)
        emitter.append(";")
    }
}

class DeclareVariableOnlyStmtOp(private val variable: VariableId, private val type: McType) : McStmtOp() {
    constructor(variable: String, type: McType) : this(VariableId.immediate(variable), type)

    override val paramTypes = emptyList<McType>()
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        type.emit(emitter)
        emitter.append(" ").append(variable.name).append(";")
    }

}

object IfStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.BOOLEAN, McType.VOID)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("if (")
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(") {").indent().appendNewLine()
        node.inputs[1].emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("}")
    }
}

object IfElseStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.BOOLEAN, McType.VOID, McType.VOID)

    override fun emit(node: McNode, emitter: Emitter) {
        IfStmtOp.emit(node, emitter)
        emitter.append(" else {").indent().appendNewLine()
        node.inputs[2].emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("}")
    }
}

object DoWhileStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.VOID, McType.BOOLEAN)

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("do {").indent().appendNewLine()
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("} while (")
        node.inputs[1].emit(emitter, Precedence.COMMA)
        emitter.append(");")
    }
}

object ThrowStmtOp : McStmtOp() {
    override val paramTypes = listOf(McType.Any)
    override val hasSideEffects = true

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("throw ")
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.append(";")
    }
}

class ReturnAsVariableBlockOp(
    private val blockName: VariableId?,
    private val returnedVariableName: VariableId,
    private val returnedVariableType: McType
) : McStmtOp() {
    override val paramTypes = listOf(McType.VOID)
    override val declaredVariables = listOfNotNull(blockName, returnedVariableName)

    override fun emit(node: McNode, emitter: Emitter) {
        returnedVariableType.emit(emitter)
        emitter.append(" ").append(returnedVariableName.name).append(";").appendNewLine()
        if (blockName != null) {
            emitter.append(blockName.name).append(": {").indent().appendNewLine()
        }
        emitter.pushReturnHandler { func ->
            emitter.append(returnedVariableName.name).append(" = ")
            func()
            emitter.append(";")
            if (blockName != null) {
                emitter.appendNewLine().append("break ").append(blockName.name).append(";")
            }
        }
        node.inputs[0].emit(emitter, Precedence.COMMA)
        emitter.popReturnHandler()
        if (blockName != null) {
            emitter.dedent().appendNewLine().append("}")
        }
    }
}

object StmtListOp : McStmtOp() {
    override val paramTypes = emptyList<McType>() // special cased

    override fun emit(node: McNode, emitter: Emitter) {
        for ((index, input) in node.inputs.withIndex()) {
            if (index != 0) {
                emitter.appendNewLine()
            }
            input.emit(emitter, Precedence.COMMA)
        }
    }
}
