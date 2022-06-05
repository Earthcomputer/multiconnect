package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType

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

class CstFloatOp(private val value: Float) : CstOp() {
    override val returnType = McType.FLOAT
    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append(value.toString()).append("F")
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

class CstNullOp(override val returnType: McType) : CstOp() {
    init {
        if (returnType is McType.PrimitiveType) {
            throw IllegalArgumentException("Primitive types are not nullable")
        }
    }

    override fun emit(node: McNode, emitter: Emitter) {
        emitter.append("null")
        if (emitter.debugMode) {
            emitter.append("<")
            returnType.emit(emitter)
            emitter.append(">")
        }
    }
}

fun createCstNode(value: Any): McNode {
    return when (value) {
        is Boolean -> McNode(CstBoolOp(value))
        is Byte -> McNode(CastOp(McType.INT, McType.BYTE), McNode(CstIntOp(value.toInt())))
        is Short -> McNode(CastOp(McType.INT, McType.SHORT), McNode(CstIntOp(value.toInt())))
        is Int -> McNode(CstIntOp(value))
        is Long -> McNode(CstLongOp(value))
        is Float -> McNode(CstFloatOp(value))
        is Double -> McNode(CstDoubleOp(value))
        is String -> McNode(CstStringOp(value))
        else -> throw CompileException("Invalid constant $value of type ${value.javaClass.name}")
    }
}
