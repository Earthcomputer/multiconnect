package net.earthcomputer.multiconnect.compiler.node

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
