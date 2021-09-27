package net.earthcomputer.multiconnect.ap

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("net.earthcomputer.multiconnect.ap.*")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
class MulticonnectAP : AbstractProcessor(), ErrorConsumer {
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.any { it.qualifiedName.contentEquals(messageName) }) {
            for (element in roundEnv.getElementsAnnotatedWith(Message::class.java)) {
                MessageProcessor.process(element, this, processingEnv)
            }
        }
        if (annotations.any { it.qualifiedName.contentEquals(polymorphicName) }) {
            for (element in roundEnv.getElementsAnnotatedWith(Polymorphic::class.java)) {
                PolymorphicProcessor.process(element, this, processingEnv)
            }
        }
        if (annotations.any { it.qualifiedName.contentEquals(networkEnumName) }) {
            for (element in roundEnv.getElementsAnnotatedWith(NetworkEnum::class.java)) {
                NetworkEnumProcessor.process(element, this, processingEnv)
            }
        }
        return true
    }

    override fun report(message: String, element: Element) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }
}
