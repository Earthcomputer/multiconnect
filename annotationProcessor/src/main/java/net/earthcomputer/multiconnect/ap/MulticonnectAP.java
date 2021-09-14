package net.earthcomputer.multiconnect.ap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
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
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

// TODO: this is horrible and needs cleaning up
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@SupportedAnnotationTypes("net.earthcomputer.multiconnect.ap.*")
public class MulticonnectAP extends AbstractProcessor {
    private static final Pattern CLASS_NAME_START = Pattern.compile("\\.(?=[A-Z])");

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
            if (field.getModifiers().contains(Modifier.STATIC)) continue;
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

        String qualifiedName = ((TypeElement) element).getQualifiedName().toString();
        String[] parts = CLASS_NAME_START.split(qualifiedName, 2);
        if (parts.length < 2) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type must follow name conventions", element);
            return;
        }
        String utilName = parts[1].replace('.', '_') + "_Util";

        String generatedClassName = "net.earthcomputer.multiconnect.generated.messageutil." + utilName;
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(generatedClassName);
            try (PrintWriter pw = new PrintWriter(sourceFile.openWriter())) {
                pw.println("package net.earthcomputer.multiconnect.generated.messageutil;");
                pw.println();
                pw.println("public final class " + utilName + " {");
                pw.println("\tprivate " + utilName + "() {}");
                pw.println("\tprivate static final " + utilName + " INSTANCE = new " + utilName + "();");
                pw.println("\tpublic static " + utilName + " instance() { return INSTANCE; }");
                if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
                    pw.println("\tpublic " + qualifiedName + " constructDefault() {");
                    pw.println("\t\t" + qualifiedName + " instance = new " + qualifiedName + "();");
                    Polymorphic polymorphic = element.getAnnotation(Polymorphic.class);
                    if (polymorphic != null) {
                        TypeElement superType = (TypeElement) ((DeclaredType) ((TypeElement) element).getSuperclass()).asElement();
                        if (!superType.getQualifiedName().toString().equals("java.lang.Object")) {
                            Element firstField = superType.getEnclosedElements().stream().filter(it -> it.getKind() == ElementKind.FIELD && !it.getModifiers().contains(Modifier.STATIC)).findFirst().orElse(null);
                            if (firstField == null) {
                                return;
                            }
                            TypeMirror firstFieldType = firstField.asType();
                            if (firstFieldType.getKind() == TypeKind.BOOLEAN) {
                                pw.println("\t\tinstance." + firstField.getSimpleName() + " = " + polymorphic.booleanValue() + ";");
                            } else if (firstFieldType.getKind().isPrimitive()) {
                                int[] ints = polymorphic.intValue();
                                if (ints.length > 0) {
                                    pw.println("\t\tinstance." + firstField.getSimpleName() + " = " + ints[0] + ";");
                                }
                                // TODO: registries
                            } else {
                                TypeElement fieldType = (TypeElement) ((DeclaredType) firstFieldType).asElement();
                                String qName = fieldType.getQualifiedName().toString();
                                switch (qName) {
                                    case "java.lang.String" -> {
                                        if (polymorphic.stringValue().length > 0) {
                                            pw.println("\t\tinstance." + firstField.getSimpleName() + " = " + processingEnv.getElementUtils().getConstantExpression(polymorphic.stringValue()[0]) + ";");
                                        }
                                    }
                                    case "net.minecraft.util.Identifier" -> {
                                        // TODO: registries
                                        if (polymorphic.stringValue().length > 0) {
                                            pw.println("\t\tinstance." + firstField.getSimpleName() + " = new net.minecraft.util.Identifier(" + processingEnv.getElementUtils().getConstantExpression(polymorphic.stringValue()[0]) + ");");
                                        }
                                    }
                                }
                                if (fieldType.getKind() == ElementKind.ENUM) {
                                    if (polymorphic.stringValue().length > 0) {
                                        pw.println("\t\tinstance." + firstField.getSimpleName() + " = " + qName + "." + polymorphic.stringValue()[0] + ";");
                                    }
                                }
                            }
                        }
                    }
                    for (Element field : element.getEnclosedElements()) {
                        if (!field.getKind().isField()) continue;
                        if (field.getModifiers().contains(Modifier.STATIC)) continue;
                        TypeMirror fieldType = field.asType();
                        if (fieldType.getKind() == TypeKind.BOOLEAN) {
                            DefaultConstruct defaultConstruct = field.getAnnotation(DefaultConstruct.class);
                            if (defaultConstruct != null) {
                                pw.println("\t\tinstance." + field.getSimpleName() + " = " + defaultConstruct.booleanValue() + ";");
                            }
                        } else if (fieldType.getKind().isPrimitive()) {
                            DefaultConstruct defaultConstruct = field.getAnnotation(DefaultConstruct.class);
                            if (defaultConstruct != null) {
                                pw.println("\t\tinstance." + field.getSimpleName() + " = " + defaultConstruct.intValue() + ";");
                            }
                        } else if (fieldType.getKind() == TypeKind.DECLARED) {
                            TypeElement fieldTypeElement = (TypeElement) ((DeclaredType) fieldType).asElement();
                            String qName = fieldTypeElement.getQualifiedName().toString();
                            String toConstruct = switch (qName) {
                                case "java.util.UUID" -> "net.minecraft.util.Util.NIL_UUID";
                                case "java.util.BitSet" -> "new java.util.BitSet()";
                                case "java.util.OptionalInt" -> "java.util.OptionalInt.empty()";
                                case "java.util.OptionalLong" -> "java.util.OptionalLong.empty()";
                                case "it.unimi.dsi.fastutil.ints.IntList" -> "new it.unimi.dsi.fastutil.ints.IntArrayList()";
                                case "it.unimi.dsi.fastutil.longs.LongList" -> "new it.unimi.dsi.fastutil.longs.LongArrayList()";
                                case "java.util.List" -> "new java.util.ArrayList<>()";
                                case "java.util.Optional" -> "java.util.Optional.empty()";
                                default -> null;
                            };
                            if (toConstruct == null) {
                                String constructQName = qName;
                                AnnotationMirror defaultConstruct = fieldTypeElement.getAnnotationMirrors().stream()
                                        .filter(it -> ((TypeElement) it.getAnnotationType().asElement()).getQualifiedName().toString().equals("net.earthcomputer.multiconnect.ap.DefaultConstruct"))
                                        .findFirst().orElse(null);
                                if (defaultConstruct != null) {
                                    var value = defaultConstruct.getElementValues().entrySet().stream()
                                            .filter(it -> it.getKey().getSimpleName().toString().equals("value"))
                                            .findFirst().orElse(null);
                                    if (value != null) {
                                        TypeMirror defaultConstructType = (TypeMirror) value.getValue().getValue();
                                        constructQName = ((TypeElement) ((DeclaredType) defaultConstructType).asElement()).getQualifiedName().toString();
                                    }
                                }
                                if (fieldTypeElement.getAnnotation(Message.class) != null && (!fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT) || !constructQName.equals(qName))) {
                                    String[] constructParts = CLASS_NAME_START.split(constructQName, 2);
                                    if (constructParts.length >= 2) {
                                        toConstruct = constructParts[1].replace('.', '_') + "_Util.instance()" +
                                                ".constructDefault()";
                                    }
                                }
                            }
                            if (toConstruct == null && fieldTypeElement.getKind() == ElementKind.ENUM) {
                                Element firstEnumConstant = fieldTypeElement.getEnclosedElements().stream().filter(it -> it.getKind() == ElementKind.ENUM_CONSTANT).findFirst().orElse(null);
                                if (firstEnumConstant != null) {
                                    toConstruct = qName + "." + firstEnumConstant.getSimpleName();
                                }
                            }
                            if (toConstruct != null) {
                                pw.println("\t\tinstance." + field.getSimpleName() + " = " + toConstruct + ";");
                            }
                        }
                    }
                    pw.println("\t\treturn instance;");
                    pw.println("\t}");
                }
                pw.println("}");
                pw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
