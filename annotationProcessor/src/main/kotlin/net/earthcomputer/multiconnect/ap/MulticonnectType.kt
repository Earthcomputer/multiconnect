package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

@Serializable
data class MulticonnectType(
    val realType: TypeMirror,
    val wireType: Types,
    val registry: Registries?,
    @Contextual val lengthInfo: Length?,
    @Contextual val defaultConstructInfo: DefaultConstruct?,
    val onlyIf: String?,
    val datafixType: DatafixTypes?,
    val polymorphicBy: String?,
    val introduce: List<@Contextual Introduce>
) {
    fun isConstantRepresentable(): Boolean {
        return realType.kind.isPrimitive
                || realType.isEnum
                || realType.hasQualifiedName(JAVA_LANG_STRING)
                || realType.hasQualifiedName(MINECRAFT_IDENTIFIER)
    }

    fun canCoerceFromString(): Boolean {
        return realType.isEnum
                || realType.hasQualifiedName(JAVA_LANG_STRING)
                || realType.hasQualifiedName(MINECRAFT_IDENTIFIER)
                || registry != null
    }

    companion object {
        fun isSupportedType(processingEnv: ProcessingEnvironment, realType: TypeMirror): Boolean {
            return defaultWireType(realType.deepComponentType(processingEnv)) != null
        }

        fun defaultWireType(realType: TypeMirror): Types? {
            if (realType.isEnum) {
                return Types.VAR_INT
            }
            if (realType.isMessageVariant) {
                return Types.MESSAGE
            }
            return when (realType.kind) {
                TypeKind.BOOLEAN -> Types.BOOLEAN
                TypeKind.BYTE -> Types.BYTE
                TypeKind.SHORT -> Types.SHORT
                TypeKind.INT -> Types.VAR_INT
                TypeKind.LONG -> Types.VAR_LONG
                TypeKind.FLOAT -> Types.FLOAT
                TypeKind.DOUBLE -> Types.DOUBLE
                TypeKind.DECLARED -> when(realType.asTypeElement()?.qualifiedName?.toString()) {
                    JAVA_LANG_STRING -> Types.STRING
                    JAVA_UTIL_BITSET -> Types.BITSET
                    JAVA_UTIL_UUID -> Types.UUID
                    MINECRAFT_IDENTIFIER -> Types.IDENTIFIER
                    MINECRAFT_NBT_COMPOUND -> Types.NBT_COMPOUND
                    else -> null
                }
                else -> null
            }
        }

        fun isWireTypeCompatible(realType: TypeMirror, wireType: Types): Boolean {
            val defaultWireType = defaultWireType(realType) ?: return false
            if (defaultWireType.isIntegral && wireType.isIntegral) {
                // make sure the default wire type is "wider" than the actual wire type
                return INTEGRAL_SIZES[defaultWireType]!! >= INTEGRAL_SIZES[wireType]!!
            }
            return defaultWireType == wireType
        }

        fun isRegistryCompatible(realType: TypeMirror): Boolean {
            return realType.isIntegral || realType.hasQualifiedName(MINECRAFT_IDENTIFIER)
        }

        fun canAutoFill(realType: TypeMirror): Boolean {
            return realType.hasQualifiedName(MINECRAFT_NETWORK_HANDLER)
                    || realType.hasQualifiedName(MULTICONNECT_USER_DATA_HOLDER)
        }
    }
}

private val INTEGRAL_SIZES = mapOf(
    Types.BOOLEAN to 1,
    Types.BYTE to 1,
    Types.UNSIGNED_BYTE to 1,
    Types.SHORT to 2,
    Types.INT to 4,
    Types.VAR_INT to 4,
    Types.LONG to 8,
    Types.VAR_LONG to 8
)
val Types.isIntegral: Boolean
    get() = this in INTEGRAL_SIZES
