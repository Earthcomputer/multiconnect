package net.earthcomputer.multiconnect.compiler.node

import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Emitter

class VariableId private constructor(val isImmediate: Boolean, private var nameInternal: String?) {
    var name: String
        get() = nameInternal ?: throw CompileException("Accessed variable name before it was initialized")
        set(newName) {
            nameInternal = newName
        }

    fun emit(emitter: Emitter) {
        if (nameInternal == null && emitter.debugMode) {
            emitter.append("<uninitialized variable name>")
            return
        }
        emitter.append(name)
    }

    override fun hashCode() = if (isImmediate) {
        nameInternal.hashCode()
    } else {
        System.identityHashCode(this)
    }

    override fun equals(other: Any?) = if (isImmediate) {
        other is VariableId && other.isImmediate && nameInternal == other.nameInternal
    } else {
        this === other
    }

    companion object {
        fun immediate(name: String): VariableId {
            return VariableId(true, name)
        }

        fun create(): VariableId {
            return VariableId(false, null)
        }
    }
}
