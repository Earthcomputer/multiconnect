package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.encodeToString
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

object MessageProcessor {
    fun process(type: Element, errorConsumer: ErrorConsumer, processingEnv: ProcessingEnvironment) {
        if (type !is TypeElement) return
        if (type.kind != ElementKind.INTERFACE) {
            errorConsumer.report("@Message type must be an interface", type)
            return
        }

        val packageName = processingEnv.elementUtils.getPackageOf(type).qualifiedName.toString()
        val jsonFile = processingEnv.filer.createResource(
            StandardLocation.CLASS_OUTPUT,
            packageName,
            type.qualifiedName.toString().substring(packageName.length + 1) + ".json"
        )
        val messageType = MessageType()
        jsonFile.openWriter().use { writer ->
            writer.write(JSON.encodeToString(messageType))
        }
    }
}
