package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

class BinaryExpressionOp(private val operator: String, leftType: McType, rightType: McType) : McNodeOp {
    init {
        if (leftType == McType.STRING || rightType == McType.STRING) {
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
            else -> if (leftType == McType.STRING || rightType == McType.STRING) { McType.STRING } else { leftType }
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
