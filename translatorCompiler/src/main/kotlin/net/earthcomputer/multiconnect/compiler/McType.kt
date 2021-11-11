package net.earthcomputer.multiconnect.compiler

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.earthcomputer.multiconnect.ap.Types

object CommonClassNames {
    const val BYTE_BUF = "io.netty.buffer.ByteBuf"
    const val COMMON_TYPES = "net.earthcomputer.multiconnect.packets.CommonTypes"
    const val OBJECT = "java.lang.Object"
    const val STRING = "java.lang.String"
    const val LIST = "java.util.List"
    const val OPTIONAL = "java.util.Optional"
    const val OPTIONAL_INT = "java.util.OptionalInt"
    const val OPTIONAL_LONG = "java.util.OptionalLong"
    const val UUID = "java.util.UUID"
    const val BITSET = "java.util.BitSet"
    const val INT_LIST = "it.unimi.dsi.fastutil.ints.IntList"
    const val LONG_LIST = "it.unimi.dsi.fastutil.ints.LongList"
    const val NBT_COMPOUND = "net.minecraft.nbt.NbtCompound"
    const val IDENTIFIER = "net.minecraft.util.Identifier"
}

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
        val STRING = DeclaredType(CommonClassNames.STRING)
        val BYTE_BUF = DeclaredType(CommonClassNames.BYTE_BUF)
    }

    @Serializable
    @SerialName("primitive")
    data class PrimitiveType(val kind: PrimitiveTypeKind) : McType() {
        override fun emit(emitter: Emitter) {
            emitter.append(kind.name.lowercase())
        }

        override fun toString() = kind.name.lowercase()

        override fun defaultValue(): McNode {
            return when (kind) {
                PrimitiveTypeKind.VOID -> throw IllegalStateException("void does not have a default value")
                PrimitiveTypeKind.BOOLEAN -> McNode(CstBoolOp(false))
                PrimitiveTypeKind.BYTE, PrimitiveTypeKind.CHAR, PrimitiveTypeKind.SHORT -> McNode(
                    CastOp(INT, this), McNode(CstIntOp(0))
                )
                PrimitiveTypeKind.INT -> McNode(CstIntOp(0))
                PrimitiveTypeKind.LONG -> McNode(CstLongOp(0))
                PrimitiveTypeKind.FLOAT -> McNode(CstFloatOp(0f))
                PrimitiveTypeKind.DOUBLE -> McNode(CstDoubleOp(0.0))
            }
        }
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
            emitter.appendClassName(CommonClassNames.OBJECT)
        }

        override fun toString() = CommonClassNames.OBJECT
    }

    abstract fun emit(emitter: Emitter)

    open fun defaultValue(): McNode {
        return McNode(CstNullOp(this))
    }

    fun arrayOf(): ArrayType {
        return ArrayType(this)
    }

    fun listOf(): DeclaredType {
        return DeclaredType(CommonClassNames.LIST, listOf(this))
    }

    fun optionalOf(): DeclaredType {
        return DeclaredType(CommonClassNames.OPTIONAL, listOf(this))
    }
}

enum class PrimitiveTypeKind {
    BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID
}

fun McType.componentTypeOrNull(): McType? {
    return when (this) {
        is McType.ArrayType -> this.elementType
        is McType.DeclaredType -> when (this.name) {
            CommonClassNames.LIST, CommonClassNames.OPTIONAL -> this.typeArguments.single()
            CommonClassNames.INT_LIST, CommonClassNames.OPTIONAL_INT -> McType.INT
            CommonClassNames.LONG_LIST, CommonClassNames.OPTIONAL_LONG -> McType.LONG
            else -> null
        }
        else -> null
    }
}

fun McType.componentType(): McType {
    return componentTypeOrNull() ?: throw CompileException("Type $this does not have a component type")
}

fun McType.deepComponentType(): McType = generateSequence(this) { it.componentTypeOrNull() }.last()

val McType.hasComponentType get() = componentTypeOrNull() != null

val McType.dimensions: Int
    get() = generateSequence(this) { it.componentTypeOrNull() }.count() - 1

val McType.isOptional get() = this is McType.DeclaredType
        && (this.name == CommonClassNames.OPTIONAL || this.name == CommonClassNames.OPTIONAL_INT || this.name == CommonClassNames.OPTIONAL_LONG || this.name == CommonClassNames.OPTIONAL_INT)

val Types.isIntegral
    get() = when (this) {
        Types.BOOLEAN, Types.BYTE, Types.INT, Types.LONG, Types.SHORT, Types.VAR_INT, Types.VAR_LONG, Types.UNSIGNED_BYTE -> true
        else -> false
    }

fun Types.canCast(other: Types): Boolean {
    if (isIntegral) {
        return other.isIntegral
    }
    if (this == Types.FLOAT || this == Types.DOUBLE) {
        return other == Types.FLOAT || other == Types.DOUBLE
    }
    return this == other
}
