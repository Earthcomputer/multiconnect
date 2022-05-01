package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BI_CONSUMER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CommonClassNames.CLASS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.MAP
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OBJECT
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_INTRINSICS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PAIR
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY_KEY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.SUPPLIER
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Either
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.FileLocations
import net.earthcomputer.multiconnect.compiler.IoOps
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.PacketType
import net.earthcomputer.multiconnect.compiler.RegistryEntry
import net.earthcomputer.multiconnect.compiler.allPackets
import net.earthcomputer.multiconnect.compiler.byName
import net.earthcomputer.multiconnect.compiler.explicitConstructibleMessages
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.opto.optimize
import net.earthcomputer.multiconnect.compiler.protocolNamesById
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.readCsv
import net.earthcomputer.multiconnect.compiler.splitPackageClass
import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

class ProtocolCompiler(internal val protocolName: String, internal val protocolId: Int) {
    internal val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
    internal val dataDir = FileLocations.dataDir.resolve(protocolName)
    internal val latestDataDir = FileLocations.dataDir.resolve(protocols[0].name)
    internal val cacheMembers = mutableMapOf<String, (Emitter) -> Unit>()
    internal var currentProtocolId: Int = protocolId
    internal var fixRegistriesProtocolOverride: Int? = null

    fun compile() {
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", latestDataDir.resolve("cpackets.csv"), false)

        emitPacketSenders(emitter, "sendToClient", true)
        emitPacketSenders(emitter, "sendToServer", false)

        val emittedMembers = mutableSetOf<String>()
        while (cacheMembers.size != emittedMembers.size) {
            for ((memberName, cacheMember) in cacheMembers.toMap()) {
                if (memberName !in emittedMembers) {
                    emittedMembers += memberName
                    emitter.addMember(memberName)?.let(cacheMember)
                }
            }
        }

        val outputFile = FileLocations.outputDir.resolve(className.replace('.', '/') + ".java")
        val parentFile = outputFile.parentFile
        if (!parentFile.exists()) parentFile.mkdirs()
        outputFile.writeText(emitter.createClassText())
    }

    private fun emitPacketTranslators(emitter: Emitter, functionName: String, packetsFile: File, clientbound: Boolean) {
        val function = emitter.addMember(functionName) ?: return
        function.append("public static void ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf, ").appendClassName(LIST)
            .append("<").appendClassName(BYTE_BUF).append("> outBufs) {").indent().appendNewLine()
        val packets = TreeMap<Int, McNode>()
        for ((id, clazz) in readCsv<PacketType>(packetsFile)) {
            val packetFunctionName = "translate${clazz.substringAfterLast('.')}"
            if (emitPacketTranslator(emitter, packetFunctionName, clazz, clientbound)) {
                packets[id] = McNode(
                    FunctionCallOp(
                        emitter.currentClass,
                        packetFunctionName,
                        listOf(McType.BYTE_BUF, McType.BYTE_BUF.listOf()),
                        McType.VOID,
                        true
                    ),
                    McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF)),
                    McNode(LoadVariableOp(VariableId.immediate("outBufs"), McType.BYTE_BUF.listOf()))
                )
            }
        }
        val stmt = McNode(
            SwitchOp(packets.keys as SortedSet<Int>, false, McType.INT, McType.VOID),
            mutableListOf<McNode>().apply {
                add(IoOps.readType(VariableId.immediate("buf"), Types.VAR_INT))
                addAll(packets.values)
            }
        ).optimize()
        stmt.emit(function, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
    }

    private fun emitPacketTranslator(
        emitter: Emitter,
        functionName: String,
        packetClass: String,
        clientbound: Boolean
    ): Boolean {
        val messageVariantInfo = getMessageVariantInfo(packetClass)
        val function = emitter.addMember(functionName) ?: return true
        function.append("private static void ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf, ").appendClassName(LIST)
            .append("<").appendClassName(BYTE_BUF).append("> outBufs) {").indent().appendNewLine()
        generateByteBufHandler(messageVariantInfo, clientbound).optimize().emit(function, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
        return true
    }

    private fun emitPacketSenders(emitter: Emitter, functionName: String, clientbound: Boolean) {
        fun keyType(emitter: Emitter) {
            if (!clientbound) {
                emitter.appendClassName(PAIR).append("<")
            }
            emitter.appendClassName(CLASS).append("<?>")
            if (!clientbound) {
                emitter.append(", ").appendClassName("java.lang.Integer").append(">")
            }
        }

        fun biConsumer(emitter: Emitter) {
            emitter.appendClassName(BI_CONSUMER).append("<").appendClassName(OBJECT).append(", ")
                .appendClassName(LIST).append("<").appendClassName(BYTE_BUF).append(">>")
        }

        emitter.addMember("SENDERS_$functionName")?.let { field ->
            field.append("private static final ").appendClassName(MAP).append("<")
        keyType(field)
            field.append(", ")
        biConsumer(field)
            field.append("> SENDERS_").append(functionName).append(" = ").appendClassName(PACKET_INTRINSICS)
            .append(".makeMap(").indent()
            var empty = true
            for (packet in allPackets) {
                val variantInfo = getMessageVariantInfo(packet)
                val protocolIds = if (clientbound) {
                    listOfNotNull(protocolId.takeIf {
                        variantInfo.sendableFrom?.contains(it) == true
                            && getPacketDirection(it, packet) == PacketDirection.CLIENTBOUND
                    })
                } else {
                    variantInfo.sendableFrom.orEmpty().filter {
                        it >= protocolId && getPacketDirection(it, packet) == PacketDirection.SERVERBOUND
                    }
                }
                for (protocolId in protocolIds) {
                    val specializedFunctionName = buildString {
                        append("send")
                        append(splitPackageClass(packet).second.replace('.', '_'))
                        append(if (clientbound) "ToClient" else "ToServer")
                        if (!clientbound) {
                            append(protocolId)
                        }
                    }

                    if (!empty) {
                        field.append(",")
                    }
                    empty = false
                    field.appendNewLine()
                    if (!clientbound) {
                        field.appendClassName(PAIR).append(".of(")
                    }
                    field.appendClassName(packet).append(".class")
                    if (!clientbound) {
                        field.append(", ").append(protocolId.toString()).append(")")
                    }
                    field.append(", (")
                    biConsumer(field)
                    field.append(") (packet, outBufs) -> ").append(specializedFunctionName).append("((")
                        .appendClassName(packet).append(") packet, outBufs)")

                    emitter.addMember(specializedFunctionName)?.let { specializedFunction ->
                        specializedFunction.append("private static void ").append(specializedFunctionName)
                            .append("(").appendClassName(packet).append(" protocol_$protocolId, ").appendClassName(LIST)
                            .append("<").appendClassName(BYTE_BUF).append("> outBufs) {").indent().appendNewLine()
                        generateExplicitSenderClientRegistries(variantInfo, protocolId, clientbound)
                            .optimize().emit(specializedFunction, Precedence.COMMA)
                        specializedFunction.dedent().appendNewLine().append("}")
                    }
                }
            }
            field.dedent().appendNewLine().append(");")
        }

        val function = emitter.addMember(functionName) ?: return
        function.append("public static void ").append(functionName).append("(")
            .appendClassName(OBJECT).append(" packet, ")
        if (!clientbound) {
            function.append("int fromProtocol, ")
        }
        function.appendClassName(LIST)
            .append("<").appendClassName(BYTE_BUF).append("> outBufs) {").indent().appendNewLine()

        biConsumer(function)
        function.append(" function = SENDERS_").append(functionName).append(".get(")
        if (!clientbound) {
            function.appendClassName(PAIR).append(".of(")
        }
        function.append("packet.getClass()")
        if (!clientbound) {
            function.append(", fromProtocol)")
        }
        function.append(");").appendNewLine()
        function.append("if (function == null) {").indent().appendNewLine()
        function.append("throw new ").appendClassName("java.lang.IllegalArgumentException")
            .append("(\"Cannot send packet \" + packet.getClass().getSimpleName() + \" to protocol $protocolId")
        if (!clientbound) {
            function.append(" from protocol \" + fromProtocol")
        } else {
            function.append("\"")
        }
        function.append(");").dedent().appendNewLine().append("}").appendNewLine()
        function.append("function.accept(packet, outBufs);")
            .dedent().appendNewLine().append("}")
    }

    fun compileDefaultConstructors() {
        val emitter = Emitter("net.earthcomputer.multiconnect.generated.DefaultConstructors", TreeSet(), TreeMap(), StringBuilder())

        emitter.addMember("DEFAULT_CONSTRUCT_METHODS")?.let { field ->
            field.append("private static final ").appendClassName(MAP)
                .append("<").appendClassName(CLASS).append("<?>, ").appendClassName(SUPPLIER)
                .append("<").appendClassName(OBJECT).append(">> DEFAULT_CONSTRUCT_METHODS = ")
                .appendClassName(PACKET_INTRINSICS).append(".makeMap(").indent()
            for ((index, className) in explicitConstructibleMessages.withIndex()) {
                val methodName = "construct${splitPackageClass(className).second.replace('.', '_')}"

                if (index != 0) {
                    field.append(",")
                }
                field.appendNewLine().appendClassName(className).append(".class, (").appendClassName(SUPPLIER)
                    .append("<").appendClassName(OBJECT).append(">) ").appendClassName(emitter.currentClass)
                    .append("::").append(methodName)

                val method = emitter.addMember(methodName) ?: throw CompileException("Default constructible classes must have a unique simple name")
                method.append("private static ").appendClassName(className).append(" ").append(methodName).append("() {").indent().appendNewLine()
                McNode(StmtListOp, McNode(ReturnStmtOp(McType.DeclaredType(className)), generateDefaultConstructGraph(getMessageVariantInfo(className))))
                    .optimize().emit(method, Precedence.COMMA)
                method.dedent().appendNewLine()
                method.append("}")
            }
            field.dedent().appendNewLine().append(");")
        }

        val method = emitter.addMember("construct")!!
        method.append("public static ").appendClassName(OBJECT).append(" construct(").appendClassName(CLASS).append("<?> type) {").indent().appendNewLine()
        method.appendClassName(SUPPLIER).append("<").appendClassName(OBJECT).append("> constructor = DEFAULT_CONSTRUCT_METHODS.get(type);").appendNewLine()
        method.append("if (constructor == null) {").indent().appendNewLine()
        method.append("throw new ").appendClassName("java.lang.IllegalArgumentException").append("(\"Cannot explicitly default construct class \" + type.getName());").dedent().appendNewLine()
        method.append("}").appendNewLine()
        method.append("return constructor.get();").dedent().appendNewLine()
        method.append("}")

        val outputFile = FileLocations.outputDir.resolve(emitter.currentClass.replace('.', '/') + ".java")
        val parentFile = outputFile.parentFile
        if (!parentFile.exists()) parentFile.mkdirs()
        outputFile.writeText(emitter.createClassText())
    }

    internal fun Registries.getRawId(value: String): Either<Int, McNode>? {
        val registryDir = FileLocations.dataDir.resolve(protocolNamesById[currentProtocolId]!!)
        val entries = readCsv<RegistryEntry>(registryDir.resolve("${this.name.lowercase()}.csv"))
        val entry = entries.byName(value)

        // load id dynamically if necessary
        if (entry == null && currentProtocolId != protocolId && value.startsWith("multiconnect:")) {
            val registryType = McType.DeclaredType(REGISTRY)
            val registryKeyType = McType.DeclaredType(REGISTRY_KEY)
            val registryElementType = McType.DeclaredType("RegistryElement")
            return Either.Right(
                McNode(FunctionCallOp(REGISTRY, "getRawId", listOf(registryType, registryElementType), McType.INT, false, isStatic = false),
                    McNode(LoadFieldOp(registryType, this.name, registryType, isStatic = true)),
                    McNode(FunctionCallOp(REGISTRY, "get", listOf(registryType, registryKeyType), registryElementType, false, isStatic = false),
                        McNode(LoadFieldOp(registryType, this.name, registryType, isStatic = true)),
                        McNode(LoadFieldOp(McType.DeclaredType(className), createRegistryKeyField(this, value), registryKeyType, isStatic = true))
                    )
                )
            )
        }

        return entry?.id?.let { Either.Left(it) }
    }
}
