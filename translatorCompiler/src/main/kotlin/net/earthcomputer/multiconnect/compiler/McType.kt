package net.earthcomputer.multiconnect.compiler

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class McType {
    companion object {
        val VOID = PrimitiveType(PrimitiveTypeKind.VOID)
        val BOOLEAN = PrimitiveType(PrimitiveTypeKind.BOOLEAN)
        val BYTE = PrimitiveType(PrimitiveTypeKind.BYTE)
        val CHAR = PrimitiveType(PrimitiveTypeKind.CHAR)
        val DOUBLE = PrimitiveType(PrimitiveTypeKind.DOUBLE)
        val FLOAT = PrimitiveType(PrimitiveTypeKind.FLOAT)
        val INT = PrimitiveType(PrimitiveTypeKind.INT)
        val LONG = PrimitiveType(PrimitiveTypeKind.LONG)
        val SHORT = PrimitiveType(PrimitiveTypeKind.SHORT)
        val STRING = DeclaredType("java.lang.String")
    }

    @Serializable
    @SerialName("primitive")
    data class PrimitiveType(val kind: PrimitiveTypeKind) : McType() {
        override fun emit(emitter: Emitter) {
            emitter.append(kind.name.lowercase())
        }

        override fun toString() = kind.name.lowercase()
    }

    @Serializable
    @SerialName("array")
    data class ArrayType(val elementType: McType) : McType() {
        override fun emit(emitter: Emitter) {
            elementType.emit(emitter)
            emitter.append("[]")
        }

        override fun toString() = "$elementType[]"
    }

    @Serializable
    @SerialName("declared")
    data class DeclaredType(val name: String, val typeArguments: List<McType> = listOf()) : McType() {
        override fun emit(emitter: Emitter) {
            emitter.appendClassName(name)
            if (typeArguments.isNotEmpty()) {
                emitter.append("<")
                for ((index, arg) in typeArguments.withIndex()) {
                    if (index != 0) {
                        emitter.append(", ")
                    }
                    arg.emit(emitter)
                }
                emitter.append(">")
            }
        }

        override fun toString() = if (typeArguments.isEmpty()) {
            name
        } else {
            "$name<${typeArguments.joinToString(", ")}>"
        }
    }

    @Serializable
    object Any : McType() {
        override fun emit(emitter: Emitter) {
            emitter.appendClassName("java.lang.Object")
        }

        override fun toString() = "java.lang.Object"
    }

    abstract fun emit(emitter: Emitter)

    fun arrayOf(): ArrayType {
        return ArrayType(this)
    }

    fun listOf(): DeclaredType {
        return DeclaredType("java.util.List", listOf(this))
    }

    fun optionalOf(): DeclaredType {
        return DeclaredType("java.util.Optional", listOf(this))
    }
}

enum class PrimitiveTypeKind {
    BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID
}
