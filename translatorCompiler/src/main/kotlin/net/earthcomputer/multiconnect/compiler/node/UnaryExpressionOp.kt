package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.isIntegral

class UnaryExpressionOp(private val operator: String, type: McType) : McNodeOp {
    init {
        when (operator) {
            "!" -> if (type != McType.BOOLEAN) throw CompileException("The ! operator only supports boolean")
            "~" -> if (!type.isIntegral) throw CompileException("The ~ operator only supports integral types")
            "+", "-" -> if (!type.isIntegral && type != McType.FLOAT && type != McType.DOUBLE) throw CompileException("The $operator operator only supports numeric types")
            else -> throw CompileException("Unknown unary operator $operator")
        }
    }

    override val paramTypes = listOf(type)
    override val returnType = type
    override val isExpensive = true
    override val precedence = Precedence.UNARY

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(operator)
        node.inputs[0].emit(emitter, Precedence.UNARY)
    }
}
