package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.ReturnHandler
import java.util.Collections
import java.util.SortedSet

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
            emitCase(key, emitter)
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

    private fun emitCase(case: Any, emitter: Emitter) {
        when (case) {
            is EnumCase -> emitter.append(case.name)
            is GroupCase<*> -> {
                for ((index, subCase) in case.cases.withIndex()) {
                    if (index != 0) {
                        emitter.append(", ")
                    }
                    emitCase(subCase, emitter)
                }
            }
            else -> McNode(createCstOp(case)).emit(emitter, Precedence.COMMA)
        }
    }

    data class EnumCase(val name: String): Comparable<EnumCase> {
        override fun compareTo(other: EnumCase) = name.compareTo(other.name)
    }
    data class GroupCase<T: Comparable<T>>(val cases: SortedSet<T>): Comparable<GroupCase<T>> {
        init {
            if (cases.isEmpty()) {
                throw IllegalArgumentException("Cases must not be empty")
            }
        }

        override fun compareTo(other: GroupCase<T>): Int {
            return cases.zip(other.cases)
                .firstOrNull { (first, second) -> first != second }
                ?.let { (first, second) -> first.compareTo(second) }
                ?: cases.size.compareTo(other.cases.size)
        }
    }
}
