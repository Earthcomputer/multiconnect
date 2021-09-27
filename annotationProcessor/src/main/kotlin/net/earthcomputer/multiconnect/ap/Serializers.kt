@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

object FromRegistrySerializer : AnnotationSerializer<FilledArgument.FromRegistry>(FilledArgument.FromRegistry::class)

abstract class AnnotationSerializer<T: Annotation>(private val clazz: KClass<T>): KSerializer<T> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor = buildClassSerialDescriptor(clazz.java.simpleName) {
        for (method in clazz.java.declaredMethods) {
            element(method.name, method.returnType.kotlin.serializer().descriptor)
        }
    }

    override fun deserialize(decoder: Decoder): T {
        throw UnsupportedOperationException()
    }

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: T) {
        @Suppress("UNCHECKED_CAST")
        fun <T> CompositeEncoder.encode(index: Int, serializer: KSerializer<T>, value: Any) {
            encodeSerializableElement(descriptor, index, serializer, value as T)
        }
        encoder.encodeStructure(descriptor) {
            for ((index, method) in clazz.java.declaredMethods.withIndex()) {
                encode(index, method.returnType.kotlin.serializer(), method.invoke(value))
            }
        }
    }
}

object PrimitiveTypeMirrorSerializer : SerializationStrategy<TypeMirror> {
    override val descriptor = buildClassSerialDescriptor("primitive") {
        element<TypeKind>("kind")
    }

    override fun serialize(encoder: Encoder, value: TypeMirror) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, serializer<TypeKind>(), value.kind)
        }
    }
}

object ArrayTypeMirrorSerializer : SerializationStrategy<TypeMirror> {
    override val descriptor = buildClassSerialDescriptor("array") {
        element<TypeMirror>("elementType")
    }

    override fun serialize(encoder: Encoder, value: TypeMirror) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, serializer<TypeMirror>(), (value as ArrayType).componentType)
        }
    }
}

object DeclaredTypeMirrorSerializer : SerializationStrategy<TypeMirror> {
    override val descriptor = buildClassSerialDescriptor("declared") {
        element<String>("name")
        element<List<TypeMirror>>("typeArguments")
    }

    override fun serialize(encoder: Encoder, value: TypeMirror) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.asTypeElement()!!.qualifiedName.toString())
            encodeSerializableElement(descriptor, 1, serializer<List<TypeMirror>>(), (value as DeclaredType).typeArguments)
        }
    }
}

val JSON = Json {
    prettyPrint = true

    serializersModule = SerializersModule {
        polymorphic(TypeMirror::class) {
            defaultSerializer { instance: TypeMirror ->
                when {
                    instance.kind.isPrimitive || instance.kind == TypeKind.VOID -> PrimitiveTypeMirrorSerializer
                    instance.kind == TypeKind.ARRAY -> ArrayTypeMirrorSerializer
                    instance.kind == TypeKind.DECLARED -> DeclaredTypeMirrorSerializer
                    else -> null
                }
            }
        }
    }
}
