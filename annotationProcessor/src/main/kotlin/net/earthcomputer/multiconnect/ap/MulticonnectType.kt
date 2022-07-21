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
    @Contextual val datafixInfo: Datafix?,
    val polymorphicBy: String?,
    val introduce: List<@Contextual Introduce>,
    val customFix: List<@Contextual CustomFix>,
) {
    fun isConstantRepresentable(): Boolean {
        return realType.kind.isPrimitive
                || realType.isEnum
                || realType.hasQualifiedName(JAVA_LANG_STRING)
                || realType.hasQualifiedName(MINECRAFT_RESOURCE_LOCATION)
    }

    fun canCoerceFromString(): Boolean {
        return realType.isEnum
                || realType.hasQualifiedName(JAVA_LANG_STRING)
                || realType.hasQualifiedName(MINECRAFT_RESOURCE_LOCATION)
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
            if (realType.isMessage || realType.isMessageVariant) {
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
                    MINECRAFT_RESOURCE_LOCATION -> Types.RESOURCE_LOCATION
                    MINECRAFT_COMPOUND_TAG -> Types.COMPOUND_TAG
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
            return realType.isIntegral || realType.hasQualifiedName(MINECRAFT_RESOURCE_LOCATION)
        }

        fun canAutoFill(realType: TypeMirror): Boolean {
            return realType.hasQualifiedName(MINECRAFT_CLIENT_PACKET_LISTENER)
                    || realType.hasQualifiedName(MULTICONNECT_TYPED_MAP)
                    || realType.hasQualifiedName(MULTICONNECT_DELAYED_PACKET_SENDER)
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
