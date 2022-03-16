package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

object FromRegistrySerializer : AnnotationSerializer<FilledArgument.FromRegistry>(FilledArgument.FromRegistry::class)
object LengthSerializer : AnnotationSerializer<Length>(Length::class)
object DefaultConstructSerializer : AnnotationSerializer<DefaultConstruct>(DefaultConstruct::class)
object IntroduceSerializer : AnnotationSerializer<Introduce>(Introduce::class)
object PolymorphicSerializer : AnnotationSerializer<Polymorphic>(Polymorphic::class)

abstract class AnnotationSerializer<T: Annotation>(private val clazz: KClass<T>): KSerializer<T> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor by lazy {
        buildClassSerialDescriptor(clazz.java.simpleName) {
            for (method in clazz.java.declaredMethods) {
                element(method.name, method.serializer().descriptor, isOptional = true)
            }
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
                val v = surrogateValue(method.kType!!) {
                    try {
                        method.invoke(value)
                    } catch (e: InvocationTargetException) {
                        throw e.cause ?: e
                    }
                }
                if (v == ""
                    || (v is List<*> && v.isEmpty())
                    || (v.javaClass.isArray && java.lang.reflect.Array.getLength(v) == 0)
                    || (v is DeclaredType && v.hasQualifiedName(JAVA_LANG_OBJECT))) {
                    continue
                }
                encode(index, method.serializer(), v)
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun Method.serializer(): KSerializer<*> {
        return JSON.serializersModule.serializer(surrogateType(kType!!))
    }

    private val Method.kType: KType?
        get() = declaringClass.kotlin.memberProperties.firstOrNull { it.name == name }?.returnType

    private fun surrogateType(type: KType): KType {
        return when {
            type.classifier == Array::class -> List::class.createType(type.arguments)
            type.jvmErasure == KClass::class -> TypeMirror::class.starProjectedType
            else -> type
        }
    }

    private fun surrogateValue(type: KType, value: () -> Any): Any {
        return when {
            type.classifier == Array::class -> {
                val v = value()
                (0 until java.lang.reflect.Array.getLength(v)).map {
                        index -> surrogateValue(type.arguments[0].type!!) { java.lang.reflect.Array.get(v, index) }
                }
            }
            type.jvmErasure == KClass::class -> {
                toTypeMirror { value() as KClass<*> }
            }
            else -> value()
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

object ClassSerializer : KSerializer<Class<*>> {
    override val descriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Class<*> {
        throw UnsupportedOperationException()
    }

    override fun serialize(encoder: Encoder, value: Class<*>) {
        encoder.encodeString(value.canonicalName)
    }
}

@OptIn(ExperimentalSerializationApi::class)
val JSON = Json {
    prettyPrint = true
    prettyPrintIndent = " "
    explicitNulls = false

    serializersModule = SerializersModule {
        polymorphicDefaultSerializer(TypeMirror::class) { instance ->
            when {
                instance.kind.isPrimitive || instance.kind == TypeKind.VOID -> PrimitiveTypeMirrorSerializer
                instance.kind == TypeKind.ARRAY -> ArrayTypeMirrorSerializer
                instance.kind == TypeKind.DECLARED -> DeclaredTypeMirrorSerializer
                else -> null
            }
        }

        contextual(Class::class, ClassSerializer)

        contextual(FilledArgument.FromRegistry::class, FromRegistrySerializer)
        contextual(Length::class, LengthSerializer)
        contextual(DefaultConstruct::class, DefaultConstructSerializer)
        contextual(Introduce::class, IntroduceSerializer)
        contextual(Polymorphic::class, PolymorphicSerializer)
    }
}
