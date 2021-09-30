package net.earthcomputer.multiconnect.compiler

import java.util.SortedMap
import java.util.SortedSet

class Emitter(
    val currentClass: String,
    private val imports: SortedSet<String>,
    private val members: SortedMap<String, Emitter>,
    private val text: StringBuilder
) {
    private var indent = ""

    private val returnHandlers = mutableListOf<ReturnHandler>({ func ->
        append("return ")
        func()
        append(";")
    })
    fun pushReturnHandler(handler: ReturnHandler) {
        returnHandlers += handler
    }
    fun popReturnHandler() {
        returnHandlers.removeLast()
    }
    fun appendReturn(param: () -> Unit) {
        returnHandlers.last()(param)
    }

    fun append(text: String): Emitter {
        if (text.contains("\n")) {
            throw IllegalArgumentException("append() cannot contain newline, use appendNewLine() instead")
        }
        this.text.append(text)
        return this
    }

    fun appendClassName(name: String): Emitter {
        val (packageName, simpleName) = splitPackageClass(name)
        if (packageName != "java.lang" && packageName != splitPackageClass(currentClass).first) {
            imports += "$packageName.${simpleName.substringBefore('.')}"
        }
        this.text.append(simpleName)
        return this
    }

    fun indent(): Emitter {
        indent += "    "
        return this;
    }

    fun dedent(): Emitter {
        if (indent.isEmpty()) {
            throw IllegalStateException("Cannot dedent without indenting")
        }
        indent = indent.substring(0, indent.length - 4)
        return this
    }

    fun appendNewLine(): Emitter {
        text.append("\n").append(indent)
        return this
    }

    fun addMember(name: String): Emitter? {
        if (name in members) return null
        val emitter = Emitter(currentClass, imports, members, StringBuilder())
        emitter.indent()
        emitter.append(emitter.indent)
        members[name] = emitter
        return emitter
    }

    fun createClassText() = buildString {
        val (packageName, simpleName) = splitPackageClass(currentClass)
        if (packageName.isNotEmpty()) {
            append("package $packageName;\n\n")
        }
        for (import in imports) {
            append("import $import;\n")
        }
        if (imports.isNotEmpty()) {
            append("\n")
        }
        append("@SuppressWarnings(\"all\")\n")
        append("public class $simpleName {\n")
        for (member in members.values) {
            append(member.text).append("\n\n")
        }
        append("}\n")
    }
}

typealias ReturnHandler = (() -> Unit) -> Unit
