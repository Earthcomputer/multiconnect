package net.earthcomputer.multiconnect.ap

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

object PolymorphicProcessor {
    fun process(type: Element, errorConsumer: ErrorConsumer, processingEnv: ProcessingEnvironment) {
        if (type !is TypeElement) return
        if (!type.hasAnnotation(Message::class)) {
            errorConsumer.report("@Polymorphic class must be annotation with @Message", type)
            return
        }
        val polymorphic = type.getAnnotation(Polymorphic::class) ?: return

        if (type.hasModifier(Modifier.ABSTRACT)) {
            if (!type.isPolymorphicRoot) {
                errorConsumer.report("Abstract @Polymorphic classes must extend Object", type)
                return
            }
            val typeField = type.recordFields.firstOrNull()
            if (typeField == null) {
                errorConsumer.report("@Polymorphic root must contain a type field", type)
                return
            }
            val multiconnectType = typeField.getMulticonnectType(processingEnv) ?: return
            if (!multiconnectType.isConstantRepresentable()) {
                errorConsumer.report("@Polymorphic type field must have constant representable type", typeField)
                return
            }

            val defaultConstruct = type.getAnnotation(DefaultConstruct::class)
            if (defaultConstruct != null) {
                fun validateDefaultConstruct(): Boolean {
                    val subType = toTypeMirror { defaultConstruct.subType }.asTypeElement()
                    if (subType == null || subType.qualifiedName.contentEquals(JAVA_LANG_OBJECT)) {
                        if (defaultConstruct.compute.isNotEmpty()) {
                            return true
                        }
                        errorConsumer.report("@Polymorphic root default construct must specify a sub-type", type)
                        return false
                    }
                    val polymorphicParent = subType.polymorphicParent
                    if (polymorphicParent == null || !processingEnv.typeUtils.isSameType(polymorphicParent.asType(), type.asType())) {
                        errorConsumer.report("@Polymorphic root default construct must specify a valid sub-type", type)
                        return false
                    }
                    return true
                }
                if (!validateDefaultConstruct()) {
                    return
                }
            }
        } else {
            val polymorphicParent = type.polymorphicParent
            if (polymorphicParent?.isPolymorphicRoot != true) {
                errorConsumer.report("@Polymorphic subclasses must extend a @Polymorphic root class", type)
                return
            }
            val typeField = polymorphicParent.recordFields.firstOrNull() ?: return
            val multiconnectType = typeField.getMulticonnectType(processingEnv) ?: return

            val matchValues = mutableListOf<Any>()
            if (multiconnectType.realType.kind == TypeKind.BOOLEAN) {
                matchValues += polymorphic.booleanValue
            }
            matchValues.addAll(polymorphic.intValue.toList())
            matchValues.addAll(polymorphic.doubleValue.toList())
            matchValues.addAll(polymorphic.stringValue.toList())

            if (polymorphic.condition.isNotEmpty()) {
                if (polymorphic.otherwise || matchValues.any { it != false }) {
                    errorConsumer.report("Cannot specify both a condition and a match value", type)
                    return
                }
                val multiconnectFunction = type.findMulticonnectFunction(
                    processingEnv,
                    polymorphic.condition,
                    errorConsumer = errorConsumer,
                    errorElement = type
                ) ?: return
                if (multiconnectFunction.returnType.kind != TypeKind.BOOLEAN) {
                    errorConsumer.report("Match condition must return boolean", type)
                    return
                }
                val positionalParam = multiconnectFunction.positionalParameters.singleOrNull()
                if (positionalParam == null) {
                    errorConsumer.report("Match condition must have a single positional parameter", type)
                    return
                }
                if (!processingEnv.typeUtils.isAssignable(multiconnectType.realType, positionalParam)) {
                    errorConsumer.report("Match condition positional parameter must match the type of the type field", type)
                    return
                }
                if (multiconnectFunction.parameters.any { it is MulticonnectParameter.Argument }) {
                    errorConsumer.report("Match condition cannot capture @Arguments", type)
                    return
                }
            } else if (polymorphic.otherwise) {
                if (matchValues.any { it != false }) {
                    errorConsumer.report("Cannot specify match values when otherwise = true", type)
                    return
                }
            } else {
                if (matchValues.isEmpty()) {
                    errorConsumer.report("Must specify at least one match value", type)
                    return
                }
                val isValid = when {
                    multiconnectType.canCoerceFromString() -> matchValues.all { it is String }
                    multiconnectType.realType.kind == TypeKind.BOOLEAN -> matchValues.all { it is Boolean }
                    multiconnectType.realType.isIntegral -> matchValues.all { it is Long }
                    multiconnectType.realType.isFloatingPoint -> matchValues.all { it is Double }
                    else -> false
                }
                if (!isValid) {
                    errorConsumer.report("Match values must match the type field", type)
                    return
                }
            }
        }
    }
}
