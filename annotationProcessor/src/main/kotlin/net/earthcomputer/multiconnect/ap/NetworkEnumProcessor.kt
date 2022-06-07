package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

object NetworkEnumProcessor {
    fun process(enum: Element, errorConsumer: ErrorConsumer, processingEnv: ProcessingEnvironment) {
        if (enum !is TypeElement) return
        if (enum.kind != ElementKind.ENUM) {
            errorConsumer.report("@NetworkEnum must be used on enums", enum)
            return
        }
        val enumConstants = enum.enumConstants
        if (enumConstants.isEmpty()) {
            errorConsumer.report("@NetworkEnum must have at least one enum constant", enum)
            return
        }

        val packageName = processingEnv.elementUtils.getPackageOf(enum).qualifiedName.toString()
        val jsonFile = processingEnv.filer.createResource(
            StandardLocation.CLASS_OUTPUT,
            packageName,
            enum.qualifiedName.toString().substring(packageName.length + 1) + ".json"
        )
        jsonFile.openWriter().use { writer ->
            writer.write(JSON.encodeToString(JsonObject(mapOf(
                "type" to JsonPrimitive("enum"),
                "values" to JsonArray(enumConstants.map { JsonPrimitive(it.simpleName.toString()) })
            ))))
        }
    }
}
