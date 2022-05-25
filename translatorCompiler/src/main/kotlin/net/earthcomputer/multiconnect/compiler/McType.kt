package net.earthcomputer.multiconnect.compiler

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.node.CastOp
import net.earthcomputer.multiconnect.compiler.node.CstBoolOp
import net.earthcomputer.multiconnect.compiler.node.CstDoubleOp
import net.earthcomputer.multiconnect.compiler.node.CstFloatOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstLongOp
import net.earthcomputer.multiconnect.compiler.node.CstNullOp
import net.earthcomputer.multiconnect.compiler.node.ImplicitCastOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object CommonClassNames {
    const val BYTE_BUF = "io.netty.buffer.ByteBuf"
    const val UNPOOLED = "io.netty.buffer.Unpooled"
    const val CLASS = "java.lang.Class"
    const val OBJECT = "java.lang.Object"
    const val STRING = "java.lang.String"
    const val THROWABLE = "java.lang.Throwable"
    const val LIST = "java.util.List"
    const val MAP = "java.util.Map"
    const val SET = "java.util.Set"
    const val ARRAY_LIST = "java.util.ArrayList"
    const val OPTIONAL = "java.util.Optional"
    const val OPTIONAL_INT = "java.util.OptionalInt"
    const val OPTIONAL_LONG = "java.util.OptionalLong"
    const val UUID = "java.util.UUID"
    const val BITSET = "java.util.BitSet"
    const val CONSUMER = "java.util.function.Consumer"
    const val INT_UNARY_OPERATOR = "java.util.function.IntUnaryOperator"
    const val SUPPLIER = "java.util.function.Supplier"
    const val UNARY_OPERATOR = "java.util.function.UnaryOperator"
    const val INT_LIST = "it.unimi.dsi.fastutil.ints.IntList"
    const val INT_ARRAY_LIST = "it.unimi.dsi.fastutil.ints.IntArrayList"
    const val LONG_LIST = "it.unimi.dsi.fastutil.longs.LongList"
    const val LONG_ARRAY_LIST = "it.unimi.dsi.fastutil.longs.LongArrayList"
    const val NBT_COMPOUND = "net.minecraft.nbt.NbtCompound"
    const val IDENTIFIER = "net.minecraft.util.Identifier"
    const val NETWORK_HANDLER = "net.minecraft.client.network.ClientPlayNetworkHandler"
    const val PACKET_INTRINSICS = "net.earthcomputer.multiconnect.impl.PacketIntrinsics"
    const val PACKET_SENDER = "net.earthcomputer.multiconnect.impl.PacketIntrinsics.PacketSender"
    const val RAW_PACKET_SENDER = "net.earthcomputer.multiconnect.impl.PacketIntrinsics.RawPacketSender"
    const val START_SEND_PACKET_RESULT = "net.earthcomputer.multiconnect.impl.PacketIntrinsics.StartSendPacketResult"
    const val MULTICONNECT_DFU = "net.earthcomputer.multiconnect.datafix.MulticonnectDFU"
    const val DELAYED_PACKET_SENDER = "net.earthcomputer.multiconnect.impl.DelayedPacketSender"
    const val TYPED_MAP = "net.earthcomputer.multiconnect.protocols.generic.TypedMap"
    const val METHOD_HANDLE = "java.lang.invoke.MethodHandle"
    const val REGISTRY = "net.minecraft.util.registry.Registry"
    const val REGISTRY_KEY = "net.minecraft.util.registry.RegistryKey"
    const val SCHEMAS = "net.minecraft.datafixer.Schemas"
    const val TYPE_REFERENCES = "net.minecraft.datafixer.TypeReferences"
    const val TYPE_REFERENCE = "com.mojang.datafixers.DSL.TypeReference"
    const val DATA_FIXER = "com.mojang.datafixers.DataFixer"
    const val PAIR = "org.apache.commons.lang3.tuple.Pair"
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

        fun cast(node: McNode): McNode {
            val inputType = node.op.returnType as? PrimitiveType ?: throw CompileException("Cannot cast ${node.op.returnType} to $this")
            if (inputType == this) {
                return node
            }
            if (this == DOUBLE && inputType == FLOAT) {
                return McNode(ImplicitCastOp(inputType, this), node)
            }
            if (this == FLOAT && inputType == DOUBLE) {
                return McNode(CastOp(inputType, this), node)
            }
            if (this == VOID || this == BOOLEAN || this == FLOAT || this == DOUBLE
                || inputType == VOID || inputType == BOOLEAN || inputType == FLOAT || inputType == DOUBLE) {
                throw CompileException("Cannot cast $inputType to $this")
            }
            val implicit = this == LONG || (inputType != LONG
                    && (this == INT || (inputType != INT
                        && (this == SHORT || inputType != SHORT))))
            return if (implicit) {
                McNode(ImplicitCastOp(inputType, this), node)
            } else {
                McNode(CastOp(inputType, this), node)
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

fun Any.downcastConstant(targetType: McType): Any {
    if (targetType == McType.FLOAT && this is Double) {
        return toFloat()
    }
    if (this is Long && targetType is McType.PrimitiveType) {
        return when (targetType.kind) {
            PrimitiveTypeKind.BYTE -> toByte()
            PrimitiveTypeKind.CHAR -> toInt().toChar()
            PrimitiveTypeKind.SHORT -> toShort()
            PrimitiveTypeKind.INT -> toInt()
            else -> this
        }
    }
    if (this is Int && targetType is McType.PrimitiveType) {
        return when (targetType.kind) {
            PrimitiveTypeKind.BYTE -> toByte()
            PrimitiveTypeKind.CHAR -> toChar()
            PrimitiveTypeKind.SHORT -> toShort()
            else -> this
        }
    }
    if (this is Short && targetType is McType.PrimitiveType) {
        return when (targetType.kind) {
            PrimitiveTypeKind.BYTE -> toByte()
            PrimitiveTypeKind.CHAR -> toInt().toChar()
            else -> this
        }
    }
    if (this is Char && targetType is McType.PrimitiveType) {
        return when (targetType.kind) {
            PrimitiveTypeKind.BYTE -> code.toByte()
            PrimitiveTypeKind.SHORT -> code.toShort()
            else -> this
        }
    }
    return this
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

val McType.isGeneric: Boolean
    get() {
        var type = this
        while (true) {
            when (type) {
                is McType.ArrayType -> type = type.elementType
                is McType.DeclaredType -> return type.typeArguments.isNotEmpty()
                else -> return false
            }
        }
    }

val McType.rawType: McType
    get() {
        var arrayDimensions = 0
        var type = this
        while (type is McType.ArrayType) {
            arrayDimensions++
            type = type.elementType
        }
        if (type is McType.DeclaredType) {
            type = McType.DeclaredType(type.name)
        }
        repeat(arrayDimensions) {
            type = McType.ArrayType(type)
        }
        return type
    }

val McType.isList get() = this is McType.DeclaredType
        && (this.name == CommonClassNames.LIST || this.name == CommonClassNames.INT_LIST || this.name == CommonClassNames.LONG_LIST)

val McType.isOptional get() = this is McType.DeclaredType
        && (this.name == CommonClassNames.OPTIONAL || this.name == CommonClassNames.OPTIONAL_INT || this.name == CommonClassNames.OPTIONAL_LONG || this.name == CommonClassNames.OPTIONAL_INT)

val McType.isIntegral get() = this is McType.PrimitiveType && when (this.kind) {
    PrimitiveTypeKind.BYTE, PrimitiveTypeKind.INT, PrimitiveTypeKind.LONG, PrimitiveTypeKind.SHORT -> true
    else -> false
}

@OptIn(ExperimentalContracts::class)
fun McType.hasName(name: String): Boolean {
    contract {
        returns(true) implies (this@hasName is McType.DeclaredType)
    }
    return (this as? McType.DeclaredType)?.name == name
}

val McType.classInfo get() = if (this is McType.DeclaredType) getClassInfo(name) else throw CompileException("Class info not found for $this")

val McType.classInfoOrNull get() = (this as? McType.DeclaredType)?.name?.let(::getClassInfoOrNull)

val McType.messageVariantInfo get() = if (this is McType.DeclaredType) getMessageVariantInfo(name) else throw CompileException("Message variant info not found for $this")

val Types.isIntegral
    get() = when (this) {
        Types.BYTE, Types.INT, Types.LONG, Types.SHORT, Types.VAR_INT, Types.VAR_LONG, Types.UNSIGNED_BYTE -> true
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
