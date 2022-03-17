package net.earthcomputer.multiconnect.compiler

import com.google.common.collect.MapMaker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import net.earthcomputer.multiconnect.ap.DatafixTypes
import net.earthcomputer.multiconnect.ap.Introduce
import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import java.io.File
import java.lang.ref.SoftReference

lateinit var protocols: List<RegistryEntry>
lateinit var protocolNamesById: Map<Int, String>
val groups = mutableMapOf<String, MutableList<String>>()
val polymorphicChildren = mutableMapOf<String, MutableList<String>>()
fun fillIndexes() {
    for (className in FileLocations.jsonDir.walk()
        .filter { it.name.endsWith(".json") }
        .map { it.toRelativeString(FileLocations.jsonDir).substringBeforeLast('.').replace(File.separator, ".") }
    ) {
        val classInfo = getClassInfo(className)
        if (classInfo !is MessageVariantInfo) continue
        val group = classInfo.variantOf ?: className
        groups.computeIfAbsent(group) { mutableListOf() } += className
        if (classInfo.polymorphicParent != null) {
            polymorphicChildren.computeIfAbsent(classInfo.polymorphicParent) { mutableListOf() } += className
        }
    }
    for (group in groups.values) {
        group.sortBy { (getClassInfo(it) as MessageVariantInfo).minVersion ?: -1 }
    }
}

@Serializable
data class RegistryEntry(val id: Int, val name: String, val oldName: String = name) {
    override fun toString(): String {
        return "$name (ID $id)"
    }
}

@Serializable
data class PacketType(val id: Int, val clazz: String)

@Serializable
sealed class ClassInfo {
    @Transient
    lateinit var className: String
}

@Serializable
@SerialName("message")
class MessageInfo : ClassInfo()

@Serializable
@SerialName("enum")
data class EnumInfo(val values: List<String>) : ClassInfo()

@Serializable
@SerialName("messageVariant")
data class MessageVariantInfo(
    val fields: List<McField>,
    val functions: List<McFunction>,
    val polymorphicParent: String?,
    val polymorphic: Polymorphic?,
    val defaultConstruct: DefaultConstruct?,
    val handler: String?,
    val handlerProtocol: Int?,
    val partialHandlers: List<String>,
    val variantOf: String?,
    val minVersion: Int?,
    val maxVersion: Int?,
    val tailrec: Boolean = false
) : ClassInfo() {
    fun findFieldOrNull(name: String): McField? {
        return fields.firstOrNull { it.name == name }
    }

    fun findField(name: String): McField {
        return fields.first { it.name == name }
    }

    fun findFunctionOrNull(name: String): McFunction? {
        return functions.firstOrNull { it.name == name }
    }

    fun findFunction(name: String): McFunction {
        return functions.first { it.name == name }
    }
}

@Serializable
data class McField(val name: String, val type: FieldType)

@Serializable
data class FieldType(
    val realType: McType,
    val wireType: Types,
    val registry: Registries?,
    val lengthInfo: LengthInfo?,
    val defaultConstructInfo: DefaultConstruct?,
    val onlyIf: String?,
    val datafixType: DatafixTypes?,
    val polymorphicBy: String?,
    val introduce: List<IntroduceInfo>
)

@Serializable
data class McFunction(
    val name: String,
    val returnType: McType,
    val positionalParameters: List<McType>,
    val parameters: List<McParameter>
) {
    @Transient
    lateinit var owner: String
}

@Serializable
sealed class McParameter {
    abstract val paramType: McType
}
@Serializable
@SerialName("argument")
data class ArgumentParameter(override val paramType: McType, val name: String) : McParameter()
@Serializable
@SerialName("defaultConstructed")
data class DefaultConstructedParameter(override val paramType: McType) : McParameter()
@Serializable
@SerialName("suppliedDefaultConstructed")
data class SuppliedDefaultConstructedParameter(override val paramType: McType, val suppliedType: McType) : McParameter()
@Serializable
@SerialName("filled")
data class FilledParameter(override val paramType: McType, val fromRegistry: FilledFromRegistry?) : McParameter()

@Serializable
data class FilledFromRegistry(val registry: Registries, val value: String)

@Serializable(with = Polymorphic.Serializer::class)
sealed class Polymorphic {
    @Serializable
    private class Surrogate(
        val booleanValue: Boolean,
        val intValue: LongArray?,
        val doubleValue: DoubleArray?,
        val stringValue: List<String>?,
        val otherwise: Boolean,
        val condition: String?
    )
    internal object Serializer : KSerializer<Polymorphic> {
        override val descriptor = serializer<Surrogate>().descriptor

        override fun deserialize(decoder: Decoder): Polymorphic {
            val surrogate = serializer<Surrogate>().deserialize(decoder)
            return when {
                !surrogate.condition.isNullOrEmpty() -> Condition(surrogate.condition)
                surrogate.otherwise -> Otherwise
                surrogate.intValue != null && surrogate.intValue.isNotEmpty() -> Constant(surrogate.intValue.toList())
                surrogate.doubleValue != null && surrogate.doubleValue.isNotEmpty() -> Constant(surrogate.doubleValue.toList())
                surrogate.stringValue != null && surrogate.stringValue.isNotEmpty() -> Constant(surrogate.stringValue)
                else -> Constant(listOf(surrogate.booleanValue))
            }
        }

        override fun serialize(encoder: Encoder, value: Polymorphic) {
            throw UnsupportedOperationException()
        }
    }

    data class Constant<T : Any>(val value: List<T>) : Polymorphic()
    object Otherwise : Polymorphic()
    data class Condition(val value: String) : Polymorphic()
}

@Serializable(with = DefaultConstruct.Serializer::class)
sealed class DefaultConstruct {
    @Serializable
    private class Surrogate(
        val subType: McType?,
        val booleanValue: Boolean,
        val intValue: LongArray?,
        val doubleValue: LongArray?,
        val stringValue: List<String>?,
        val compute: String?
    )
    internal object Serializer : KSerializer<DefaultConstruct> {
        override val descriptor = serializer<Surrogate>().descriptor

        override fun deserialize(decoder: Decoder): DefaultConstruct {
            val surrogate = serializer<Surrogate>().deserialize(decoder)
            return when {
                !surrogate.compute.isNullOrEmpty() -> Compute(surrogate.compute)
                surrogate.subType != null -> SubType(surrogate.subType)
                surrogate.intValue != null && surrogate.intValue.isNotEmpty() -> Constant(surrogate.intValue.single())
                surrogate.doubleValue != null && surrogate.doubleValue.isNotEmpty() -> Constant(surrogate.doubleValue.single())
                surrogate.stringValue != null && surrogate.stringValue.isNotEmpty() -> Constant(surrogate.stringValue.single())
                else -> Constant(surrogate.booleanValue)
            }
        }

        override fun serialize(encoder: Encoder, value: DefaultConstruct) {
            throw UnsupportedOperationException()
        }
    }
    data class SubType(val value: McType) : DefaultConstruct()
    data class Constant<T : Any>(val value: T) : DefaultConstruct()
    data class Compute(val value: String) : DefaultConstruct()
}

@Serializable(with = LengthInfo.Serializer::class)
data class LengthInfo(val type: Types, val computeInfo: ComputeInfo?) {
    @Serializable
    private class Surrogate(val type: Types, val constant: Int, val compute: String?, val remainingBytes: Boolean)
    internal object Serializer : KSerializer<LengthInfo> {
        override val descriptor = serializer<Surrogate>().descriptor

        override fun deserialize(decoder: Decoder): LengthInfo {
            val surrogate = serializer<Surrogate>().deserialize(decoder)
            val computeInfo = when {
                surrogate.remainingBytes -> ComputeInfo.RemainingBytes
                !surrogate.compute.isNullOrEmpty() -> ComputeInfo.Compute(surrogate.compute)
                surrogate.constant >= 0 -> ComputeInfo.Constant(surrogate.constant)
                else -> null
            }
            return LengthInfo(surrogate.type, computeInfo)
        }

        override fun serialize(encoder: Encoder, value: LengthInfo) {
            throw UnsupportedOperationException()
        }
    }

    sealed class ComputeInfo {
        data class Constant(val value: Int) : ComputeInfo()
        data class Compute(val value: String) : ComputeInfo()
        object RemainingBytes : ComputeInfo()
    }
}

@Serializable(with = IntroduceInfo.Serializer::class)
sealed class IntroduceInfo {
    @Serializable
    private class Surrogate(
        val direction: Introduce.Direction,
        val booleanValue: Boolean,
        val intValue: LongArray?,
        val doubleValue: DoubleArray?,
        val stringValue: List<String>?,
        val defaultConstruct: Boolean,
        val compute: String?
    )
    object Serializer : KSerializer<IntroduceInfo> {
        override val descriptor = serializer<Surrogate>().descriptor

        override fun deserialize(decoder: Decoder): IntroduceInfo {
            val surrogate = serializer<Surrogate>().deserialize(decoder)
            return when {
                surrogate.defaultConstruct -> DefaultConstruct(surrogate.direction)
                !surrogate.compute.isNullOrEmpty() -> Compute(surrogate.direction, surrogate.compute)
                surrogate.intValue != null && surrogate.intValue.isNotEmpty() -> Constant(surrogate.direction, surrogate.intValue.single())
                surrogate.doubleValue != null && surrogate.doubleValue.isNotEmpty() -> Constant(surrogate.direction, surrogate.doubleValue.single())
                surrogate.stringValue != null && surrogate.stringValue.isNotEmpty() -> Constant(surrogate.direction, surrogate.stringValue.single())
                else -> Constant(surrogate.direction, surrogate.booleanValue)
            }
        }

        override fun serialize(encoder: Encoder, value: IntroduceInfo) {
            throw UnsupportedOperationException()
        }

    }

    abstract val direction: Introduce.Direction

    data class Constant<T : Any>(override val direction: Introduce.Direction, val value: T) : IntroduceInfo()
    data class DefaultConstruct(override val direction: Introduce.Direction) : IntroduceInfo()
    data class Compute(override val direction: Introduce.Direction, val value: String) : IntroduceInfo()
}

private val classInfoCache = mutableMapOf<String, SoftReference<ClassInfo>>()

fun getClassInfo(typeName: String): ClassInfo {
    return getClassInfoOrNull(typeName) ?: throw CompileException("Class info not found for $typeName")
}

@OptIn(ExperimentalSerializationApi::class)
fun getClassInfoOrNull(typeName: String): ClassInfo? {
    classInfoCache[typeName]?.get()?.let { return it }
    val (pkg, className) = splitPackageClass(typeName)
    val jsonFile = FileLocations.jsonDir.resolve("${pkg.replace('.', '/')}/$className.json")
    if (!jsonFile.exists()) return null
    return jsonFile.inputStream().use {
        Json { explicitNulls = false }.decodeFromStream<ClassInfo>(it)
    }.also {
        it.className = typeName
        if (it is MessageVariantInfo) {
            for (function in it.functions) {
                function.owner = typeName
            }
        }
        classInfoCache[typeName] = SoftReference(it)
    }
}

@PublishedApi
internal val csvCache = mutableMapOf<File, SoftReference<List<*>>>()

inline fun <reified T> readCsv(file: File): List<T> {
    csvCache[file.absoluteFile]?.get()?.let {
        @Suppress("UNCHECKED_CAST")
        return it as List<T>
    }
    return file.useLines { lines ->
        val itr = lines
            .map { it.substringBefore('#') }
            .filter { it.isNotBlank() }
            .iterator()
        val keys = itr.next().split(" ")
        itr.asSequence()
            .map { line ->
                Json.decodeFromJsonElement<T>(
                    JsonObject(
                    keys.zip(line.split(" ").map { JsonPrimitive(it) }).toMap()
                )
                )
            }
            .toList()
            .also { csvCache[file.absoluteFile] = SoftReference(it) }
    }
}

private val idToEntryCache = MapMaker().weakKeys().makeMap<List<RegistryEntry>, Map<Int, RegistryEntry>>()
private val nameToEntryCache = MapMaker().weakKeys().makeMap<List<RegistryEntry>, Map<String, RegistryEntry>>()

fun List<RegistryEntry>.byId(id: Int): RegistryEntry? {
    return idToEntryCache.computeIfAbsent(this) { entry ->
        entry.associateBy { it.id }
    }[id]
}

fun List<RegistryEntry>.byName(name: String): RegistryEntry? {
    return nameToEntryCache.computeIfAbsent(this) { entry ->
        entry.associateBy { it.name }
    }[name]
}
