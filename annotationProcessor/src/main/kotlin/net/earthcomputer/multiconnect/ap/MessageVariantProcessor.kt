package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.encodeToString
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.StandardLocation

object MessageVariantProcessor {
    fun process(type: Element, errorConsumer: ErrorConsumer, processingEnv: ProcessingEnvironment) {
        if (type !is TypeElement) return
        if (type.interfaces.size > 1) {
            errorConsumer.report("@MessageVariant cannot implement multiple interfaces", type)
            return
        }

        val messageVariant = type.getAnnotation(MessageVariant::class) ?: return
        val variantOf = type.interfaces.firstOrNull()
        val minVersion = messageVariant.minVersion.takeIf { it != -1 }
        val maxVersion = messageVariant.maxVersion.takeIf { it != -1 }

        if (variantOf != null) {
            if (!variantOf.isMessage) {
                errorConsumer.report("variantOf must refer to a @Message type", type)
                return
            }
        }
        if (minVersion != null && maxVersion != null && maxVersion < minVersion) {
            errorConsumer.report("maxVersion < minVersion", type)
            return
        }

        val multiconnectFields = mutableListOf<MulticonnectField>()
        val multiconnectFunctions = mutableListOf<MulticonnectFunction>()

        // check type is constructable
        val defaultConstruct = type.getAnnotation(DefaultConstruct::class)
        if (!type.isPolymorphicRoot) {
            if (defaultConstruct == null && !type.isConstructable(processingEnv)) {
                errorConsumer.report("@MessageVariant type must be constructable", type)
                return
            }
            if (type.polymorphicParent != null) {
                val polymorphic = type.getAnnotation(Polymorphic::class)
                if (polymorphic != null) {
                    if (polymorphic.condition.isNotEmpty()) {
                        val func = type.findMulticonnectFunction(processingEnv, polymorphic.condition)
                        if (func != null) {
                            multiconnectFunctions += func
                        }
                    }
                }
            }
        }

        if (defaultConstruct != null) {
            fun validateDefaultConstruct(): Boolean {
                val compute = defaultConstruct.compute
                val hasSubType = !toTypeMirror { defaultConstruct.subType }.hasQualifiedName(JAVA_LANG_OBJECT)
                if (compute.isEmpty()) {
                    if (hasSubType && type.isPolymorphicRoot) {
                        return true
                    }
                    errorConsumer.report("A default construct computation must be specified", type)
                    return false
                }
                if (defaultConstruct.booleanValue || defaultConstruct.intValue.isNotEmpty() || defaultConstruct.doubleValue.isNotEmpty() || defaultConstruct.stringValue.isNotEmpty() || hasSubType) {
                    errorConsumer.report("Cannot specify a primitive default value", type)
                    return false
                }
                val multiconnectFunction = type.findMulticonnectFunction(
                    processingEnv,
                    compute,
                    errorConsumer = errorConsumer,
                    errorElement = type
                ) ?: return false
                if (!processingEnv.typeUtils.isAssignable(multiconnectFunction.returnType, type.asType())) {
                    errorConsumer.report("Default construct function return type must be assignable to the default constructed type", type)
                    return false
                }
                if (multiconnectFunction.positionalParameters.isNotEmpty()) {
                    errorConsumer.report("Default construct function cannot have positional parameters", type)
                    return false
                }
                if (multiconnectFunction.parameters.any { it is MulticonnectParameter.Argument }) {
                    errorConsumer.report("Default construct function for type cannot accept captured @Arguments", type)
                    return false
                }
                return true
            }
            if (!validateDefaultConstruct()) {
                return
            }
        }

        val recordFields = type.recordFields
        fieldLoop@
        for ((fieldIndex, field) in recordFields.withIndex()) {
            if (!field.hasModifier(Modifier.PUBLIC) || field.hasModifier(Modifier.FINAL)) {
                errorConsumer.report("@MessageVariant field must be public and non-final", field)
                continue
            }
            val multiconnectType = field.getMulticonnectType(processingEnv)
            if (multiconnectType == null) {
                errorConsumer.report("Invalid @MessageVariant field type", field)
                continue
            }
            multiconnectFields += MulticonnectField(field.simpleName.toString(), multiconnectType)
            val deepComponentType = multiconnectType.realType.deepComponentType(processingEnv)

            if (!MulticonnectType.isWireTypeCompatible(deepComponentType, multiconnectType.wireType)) {
                errorConsumer.report("Type ${multiconnectType.realType} is not compatible with declared wire type ${multiconnectType.wireType.name}", field)
                continue
            }

            if (multiconnectType.registry != null) {
                if (!MulticonnectType.isRegistryCompatible(deepComponentType)) {
                    errorConsumer.report("@Registry must only be used on registry compatible types", field)
                    continue
                }
            }

            if (multiconnectType.lengthInfo != null) {
                if (!multiconnectType.realType.hasLength) {
                    errorConsumer.report("@Length can only be used on fields with a length", field)
                    continue
                }
                if (!multiconnectType.lengthInfo.type.isIntegral) {
                    errorConsumer.report("@Length type must be integral", field)
                    continue
                }
                if (count(
                        multiconnectType.lengthInfo.remainingBytes,
                        multiconnectType.lengthInfo.compute.isNotEmpty(),
                        multiconnectType.lengthInfo.constant != -1
                    ) > 1) {
                    errorConsumer.report("@Length cannot have more than one way to compute the length", field)
                    continue
                }
                if (multiconnectType.lengthInfo.remainingBytes) {
                    if (multiconnectType.wireType != Types.BYTE
                        || !multiconnectType.realType.hasLength
                        || multiconnectType.realType.componentType(processingEnv).kind != TypeKind.BYTE) {
                        errorConsumer.report("@Length(remainingBytes = true) can only be applied to byte arrays", field)
                        continue
                    }
                    if (fieldIndex != recordFields.lastIndex) {
                        errorConsumer.report("@Length(remainingBytes = true) can only be applied to the last field", field)
                        continue
                    }
                }
                if (multiconnectType.lengthInfo.constant < -1) {
                    errorConsumer.report("@Length must not be negative", field)
                    continue
                }
                if (multiconnectType.lengthInfo.compute.isNotEmpty()) {
                    val multiconnectFunction = type.findMulticonnectFunction(
                        processingEnv,
                        multiconnectType.lengthInfo.compute,
                        errorConsumer = errorConsumer,
                        errorElement = field
                    ) ?: continue
                    if (!multiconnectFunction.returnType.isIntegral) {
                        errorConsumer.report("Length computation must return an integer", field)
                        continue
                    }
                    if (multiconnectFunction.positionalParameters.isNotEmpty()) {
                        errorConsumer.report("Length computation cannot have any positional parameters", field)
                        continue
                    }
                    if (!validateFunctionCaptures(multiconnectFunction, type, field)) {
                        errorConsumer.report("Length computation cannot depend on later fields", field)
                        continue
                    }
                    multiconnectFunctions += multiconnectFunction
                }
            }

            if (multiconnectType.defaultConstructInfo != null) {
                val defaultInfo = validateDefaultInfo(
                    type,
                    processingEnv,
                    multiconnectType,
                    DefaultInfo(type, multiconnectType.defaultConstructInfo),
                    errorConsumer,
                    field
                ) ?: continue
                if (defaultInfo is MulticonnectFunction) {
                    if (!validateFunctionCaptures(defaultInfo, type, field)) {
                        errorConsumer.report("Default construct computation cannot depend on later fields", field)
                        continue
                    }
                    multiconnectFunctions += defaultInfo
                }
            }

            if (multiconnectType.onlyIf != null) {
                val multiconnectFunction = type.findMulticonnectFunction(
                    processingEnv,
                    multiconnectType.onlyIf,
                    errorConsumer = errorConsumer,
                    errorElement = field
                ) ?: continue
                if (multiconnectFunction.returnType.kind != TypeKind.BOOLEAN) {
                    errorConsumer.report("@OnlyIf function must return boolean", field)
                    continue
                }
                if (multiconnectFunction.positionalParameters.isNotEmpty()) {
                    errorConsumer.report("@OnlyIf function cannot have any positional parameters", field)
                    continue
                }
                if (!validateFunctionCaptures(multiconnectFunction, type, field)) {
                    errorConsumer.report("@OnlyIf computation cannot depend on later fields", field)
                    continue
                }
                multiconnectFunctions += multiconnectFunction
            }

            if (multiconnectType.datafixType != null) {
                if (!deepComponentType.hasQualifiedName(MINECRAFT_NBT_COMPOUND)) {
                    errorConsumer.report("@Datafix can only be used on fields of type NbtCompound", field)
                    continue
                }
            }

            if (multiconnectType.polymorphicBy != null) {
                val polymorphicType = deepComponentType.asTypeElement()
                if (polymorphicType?.isPolymorphicRoot != true) {
                    errorConsumer.report("@PolymorphicBy can only be used on fields of a polymorphic root type", field)
                    continue
                }
                val polymorphicByField = recordFields.firstOrNull { it.simpleName.contentEquals(multiconnectType.polymorphicBy) }
                if (polymorphicByField == null) {
                    errorConsumer.report("Cannot resolve field \"${multiconnectType.polymorphicBy}\"", field)
                    continue
                }
                if (recordFields.indexOf(polymorphicByField) >= fieldIndex) {
                    errorConsumer.report("@PolymorphicBy field must be before the target field", field)
                    continue
                }
                val typeField = polymorphicType.recordFields.firstOrNull() ?: continue
                if (!processingEnv.typeUtils.isAssignable(polymorphicByField.asType(), typeField.asType())) {
                    errorConsumer.report("@PolymorphicBy field must be assignable to the type field", field)
                    continue
                }
            }

            if (multiconnectType.introduce.distinctBy { it.direction }.size != multiconnectType.introduce.size) {
                errorConsumer.report("Duplicate @Introduce directions", field)
                continue
            }
            for (introduce in multiconnectType.introduce) {
                // TODO: move validate of @Introduce into the compiler step
//                if (introduce.direction == Introduce.Direction.AUTO && translateFromNewer != null && translateFromOlder != null) {
//                    errorConsumer.report("Ambiguous AUTO @Introduce direction", field)
//                    continue@fieldLoop
//                }
//                val translateFromType = when (introduce.direction) {
//                    Introduce.Direction.AUTO -> translateFromNewer?.let { toTypeMirror { it.type } } ?: translateFromOlder?.let { toTypeMirror { it.type } }
//                    Introduce.Direction.FROM_NEWER -> translateFromNewer?.let { toTypeMirror { it.type } }
//                    Introduce.Direction.FROM_OLDER -> translateFromOlder?.let { toTypeMirror { it.type } }
//                }?.asTypeElement()
//                if (translateFromType == null) {
//                    errorConsumer.report("There is no type to translate from in that direction", field)
//                    continue@fieldLoop
//                }
//                val defaultInfo = validateDefaultInfo(
//                    type,
//                    processingEnv,
//                    multiconnectType,
//                    DefaultInfo(translateFromType, introduce),
//                    errorConsumer,
//                    field
//                ) ?: continue@fieldLoop
                if (introduce.compute.isNotEmpty()) {
                    val function = type.findMulticonnectFunction(
                        processingEnv,
                        introduce.compute,
                        argumentResolveContext = null,
                        errorConsumer = errorConsumer,
                        errorElement = field
                    )
                    if (function != null) {
                        multiconnectFunctions += function
                    }
                }
            }
        }

        println("Checking ${type.qualifiedName}")
        checkForRecursiveTypes(type, type, mutableSetOf(), mutableMapOf(), errorConsumer, processingEnv)

        val handler = type.handler
        val handlerProtocol = handler?.getAnnotation(Handler::class.java)?.protocol?.takeIf { it != -1 }
        if (handler != null) {
            val multiconnectFunction = type.findMulticonnectFunction(
                processingEnv,
                handler.simpleName.toString(),
                errorConsumer = errorConsumer,
                errorElement = handler
            )
            if (multiconnectFunction != null) {
                multiconnectFunctions += multiconnectFunction
            }
        }
        val partialHandlers = type.getPartialHandlers(processingEnv, errorConsumer, type)
        for (partialHandler in partialHandlers) {
            multiconnectFunctions += partialHandler
        }

        val packageName = processingEnv.elementUtils.getPackageOf(type).qualifiedName.toString()
        val jsonFile = processingEnv.filer.createResource(
            StandardLocation.CLASS_OUTPUT,
            packageName,
            type.qualifiedName.toString().substring(packageName.length + 1) + ".json"
        )
        val messageVariantType = MessageVariantType(
            multiconnectFields,
            multiconnectFunctions,
            type.polymorphicParent?.qualifiedName?.toString(),
            type.getAnnotation(Polymorphic::class),
            defaultConstruct,
            handler?.simpleName?.toString(),
            handlerProtocol,
            partialHandlers.map { it.name },
            variantOf?.asTypeElement()?.qualifiedName?.toString(),
            minVersion,
            maxVersion,
            type.hasAnnotation(Sendable::class),
            messageVariant.tailrec
        )
        jsonFile.openWriter().use { writer ->
            writer.write(JSON.encodeToString(messageVariantType))
        }
    }

    private fun checkForRecursiveTypes(
        originalType: TypeElement,
        type: TypeElement,
        seenTypes: MutableSet<String>,
        polymorphicSubclasses: MutableMap<String, MutableSet<String>>,
        errorConsumer: ErrorConsumer,
        processingEnv: ProcessingEnvironment
    ) {
        type.polymorphicParent?.let {
            polymorphicSubclasses.computeIfAbsent(it.qualifiedName.toString()) { mutableSetOf() } += type.qualifiedName.toString()
        }
        val fields = type.recordFields
        for ((index, field) in fields.withIndex()) {
            val fieldType = field.asType().asTypeElement()
            if (fieldType?.isMessageVariant != true) continue
            val fieldTypeName = fieldType.qualifiedName.toString()
            if (fieldTypeName in seenTypes) {
                if (type.qualifiedName != originalType.qualifiedName) continue
                if (index == fields.lastIndex) {
                    if (type.qualifiedName.contentEquals(fieldTypeName)) {
                        if (type.getAnnotation(MessageVariant::class)?.tailrec == true
                            && field.hasAnnotation(OnlyIf::class)
                            && !type.isPolymorphicRoot) {
                            continue
                        }
                    } else {
                        val polymorphicParent = type.polymorphicParent
                        if (polymorphicParent != null) {
                            if (polymorphicParent.qualifiedName.contentEquals(fieldTypeName)) {
                                if (polymorphicParent.getAnnotation(MessageVariant::class)?.tailrec == true) {
                                    continue
                                }
                            }
                        }
                    }
                }
                errorConsumer.report("MessageVariant is self-referential and not marked as tailrec", field)
            } else {
                seenTypes += fieldTypeName
                checkForRecursiveTypes(originalType, fieldType, seenTypes, polymorphicSubclasses, errorConsumer, processingEnv)
                seenTypes.remove(fieldTypeName)
            }
        }
        polymorphicSubclasses[type.qualifiedName.toString()]?.let { subclasses ->
            for (subclass in subclasses) {
                if (subclass in seenTypes) continue
                val subType = processingEnv.elementUtils.getTypeElement(subclass) ?: continue
                seenTypes += subclass
                checkForRecursiveTypes(originalType, subType, seenTypes, polymorphicSubclasses, errorConsumer, processingEnv)
                seenTypes.remove(subclass)
            }
        }
    }

    private fun validateFunctionCaptures(
        multiconnectFunction: MulticonnectFunction,
        type: TypeElement,
        field: VariableElement
    ): Boolean {
        val allRecordFields = type.allRecordFields
        val fieldIndex = allRecordFields.indexOf(field)
        for (parameter in multiconnectFunction.parameters) {
            if (parameter is MulticonnectParameter.Argument) {
                val dependentFieldIndex = allRecordFields.indexOfFirst { it.simpleName.contentEquals(parameter.name) }
                if (dependentFieldIndex >= fieldIndex) {
                    return false
                }
            }
        }
        return true
    }

    class DefaultInfo(
        val booleanValue: Boolean,
        val intValue: LongArray,
        val doubleValue: DoubleArray,
        val stringValue: Array<String>,
        val subType: TypeMirror?,
        val defaultConstruct: Boolean,
        val compute: String,
        val computeContext: TypeElement
    ) {
        constructor(enclosingType: TypeElement, a: DefaultConstruct)
                : this(a.booleanValue, a.intValue, a.doubleValue,a.stringValue, toTypeMirror { a.subType }, false, a.compute, enclosingType)
        constructor(introducedFrom: TypeElement, a: Introduce)
                : this(a.booleanValue, a.intValue, a.doubleValue, a.stringValue, null, a.defaultConstruct, a.compute, introducedFrom)
        object DefaultConstructSenetial
    }

    private fun validateDefaultInfo(
        enclosingType: TypeElement,
        processingEnv: ProcessingEnvironment,
        multiconnectType: MulticonnectType,
        defaultInfo: DefaultInfo,
        errorConsumer: ErrorConsumer,
        errorElement: Element
    ): Any? {
        val explicits = mutableListOf<Any>()
        if (multiconnectType.realType.kind == TypeKind.BOOLEAN) {
            explicits += defaultInfo.booleanValue
        }
        explicits.addAll(defaultInfo.intValue.toList())
        explicits.addAll(defaultInfo.doubleValue.toList())
        explicits.addAll(defaultInfo.stringValue.toList())
        if (defaultInfo.subType?.hasQualifiedName(JAVA_LANG_OBJECT) == false) {
            explicits += defaultInfo.subType
        }
        if (defaultInfo.compute.isNotEmpty()) {
            if (defaultInfo.defaultConstruct || explicits.any { it != false }) {
                errorConsumer.report("Cannot specify both compute and a default value", errorElement)
                return null
            }
            val multiconnectFunction = enclosingType.findMulticonnectFunction(
                processingEnv,
                defaultInfo.compute,
                argumentResolveContext = defaultInfo.computeContext,
                errorConsumer = errorConsumer,
                errorElement = errorElement
            ) ?: return null
            if (!processingEnv.typeUtils.isAssignable(multiconnectFunction.returnType, multiconnectType.realType)) {
                errorConsumer.report("Compute function must return a value assignable to the field", errorElement)
                return null
            }
            if (multiconnectFunction.positionalParameters.isNotEmpty()) {
                errorConsumer.report("Compute function must not have any positional parameters", errorElement)
                return null
            }
            return multiconnectFunction
        }
        if (defaultInfo.defaultConstruct) {
            if (explicits.any { it != false }) {
                errorConsumer.report("Cannot specify both default construct and a default value", errorElement)
                return null
            }
            return DefaultInfo.DefaultConstructSenetial
        }
        val singleVal = explicits.singleOrNull()
        if (singleVal == null) {
            errorConsumer.report("Must only specify exactly one default value", errorElement)
            return null
        }
        val compatibleTypes = when {
            multiconnectType.canCoerceFromString() ->
                singleVal is String
            multiconnectType.realType.kind == TypeKind.BOOLEAN ->
                singleVal is Boolean
            multiconnectType.realType.isIntegral ->
                singleVal is Long
            multiconnectType.realType.isFloatingPoint ->
                singleVal is Double
            multiconnectType.realType.asTypeElement()?.isPolymorphicRoot == true -> {
                if (singleVal !is TypeMirror) {
                    false
                } else {
                    singleVal.asTypeElement()?.polymorphicParent?.let { polymorphicParent ->
                        processingEnv.typeUtils.isSameType(polymorphicParent.asType(), multiconnectType.realType)
                    } ?: false
                }
            }
            else -> {
                errorConsumer.report("No way to default construct this type was specified", errorElement)
                return null
            }
        }
        if (!compatibleTypes) {
            errorConsumer.report("Default construct value is incompatible with the field type", errorElement)
            return null
        }
        return singleVal
    }
}
