package net.earthcomputer.multiconnect.ap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@SupportedSourceVersion(SourceVersion.RELEASE_16)
@SupportedAnnotationTypes("net.earthcomputer.multiconnect.ap.*")
public class MulticonnectAP extends AbstractProcessor {
    private final Map<Name, Consumer<Element>> handlers = new HashMap<>();
    private void addHandler(Elements elements, Class<? extends Annotation> annotation, Consumer<Element> handler) {
        handlers.put(elements.getName(annotation.getName()), handler);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Elements elements = processingEnv.getElementUtils();
        addHandler(elements, Message.class, this::processPacketClass);
        addHandler(elements, Polymorphic.class, this::processPolymorphicClass);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Consumer<Element> handler = handlers.get(annotation.getQualifiedName());
            if (handler == null) {
                continue;
            }
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                handler.accept(element);
            }
        }
        return true;
    }

    private void processPacketClass(Element element) {
        fieldLoop:
        for (Element field : element.getEnclosedElements()) {
            if (!field.getKind().isField()) continue;
            TypeMirror type = field.asType();
            while (type.getKind() == TypeKind.ARRAY || isAllowedContainer(type)) {
                if (type.getKind() == TypeKind.ARRAY) {
                    type = ((ArrayType) type).getComponentType();
                } else {
                    List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
                    if (typeArguments.isEmpty()) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Container type must have type arguments", field);
                        continue fieldLoop;
                    }
                    type = typeArguments.get(0);
                }
            }
            if (!type.getKind().isPrimitive() && type.getKind() != TypeKind.DECLARED) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Message field has invalid type", field);
                continue;
            }
            if (type.getKind() == TypeKind.DECLARED) {
                TypeElement typeElement = (TypeElement) ((DeclaredType) type).asElement();
                String qName = typeElement.getQualifiedName().toString();
                // exceptions
                boolean allowed = qName.equals("java.lang.String") || qName.equals("java.util.BitSet")
                        || qName.equals("java.util.UUID")
                        || qName.equals("java.util.OptionalInt")
                        || qName.equals("java.util.OptionalLong")
                        || qName.equals("it.unimi.dsi.fastutil.ints.IntList")
                        || qName.equals("it.unimi.dsi.fastutil.longs.LongList")
                        || qName.equals("net.minecraft.util.Identifier")
                        || qName.equals("net.minecraft.nbt.NbtCompound");
                if (!allowed) {
                    if (typeElement.getKind() == ElementKind.ENUM || typeElement.getAnnotation(Message.class) != null) {
                        allowed = true;
                    }
                }
                if (!allowed) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field type must be annotated with @Message", field);
                }
            }
        }
    }

    private void processPolymorphicClass(Element element) {
        if (element.getAnnotation(Message.class) == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Polymorphic class must be annotated with @Message", element);
        }
        boolean isAbstract = element.getModifiers().contains(Modifier.ABSTRACT);
        TypeMirror superClass = ((TypeElement) element).getSuperclass();
        if (isAbstract) {
            if (superClass.getKind() == TypeKind.DECLARED) {
                String qualifiedName = ((TypeElement) ((DeclaredType) superClass).asElement()).getQualifiedName().toString();
                if (!qualifiedName.equals("java.lang.Object")) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Abstract @Polymorphic class must extend Object", element);
                }
            }
        } else {
            boolean allowed = true;
            if (superClass.getKind() != TypeKind.DECLARED) {
                allowed = false;
            } else {
                Element superClassElement = ((DeclaredType) superClass).asElement();
                if (superClassElement.getAnnotation(Polymorphic.class) == null) {
                    allowed = false;
                } else if (!superClassElement.getModifiers().contains(Modifier.ABSTRACT)) {
                    allowed = false;
                }
            }
            if (!allowed) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Non-abstract @Polymorphic class must extend abstract @Polymorphic class", element);
            }
        }
    }

    private static boolean isAllowedContainer(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) return false;
        String qName = ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString();
        return qName.equals("java.util.List") || qName.equals("java.util.Optional");
    }
}
