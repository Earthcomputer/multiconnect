package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BITSET
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CommonClassNames.CLASS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.IDENTIFIER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_UNARY_OPERATOR
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.MAP
import net.earthcomputer.multiconnect.compiler.CommonClassNames.NETWORK_HANDLER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OBJECT
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_INTRINSICS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_SENDER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PAIR
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY_KEY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.SET
import net.earthcomputer.multiconnect.compiler.CommonClassNames.SUPPLIER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.TYPED_MAP
import net.earthcomputer.multiconnect.compiler.CommonClassNames.UNARY_OPERATOR
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
import net.earthcomputer.multiconnect.compiler.encodeRLEBitSet
import net.earthcomputer.multiconnect.compiler.explicitConstructibleMessages
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.node.CstStringOp
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
import java.util.BitSet
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

class ProtocolCompiler(internal val protocolName: String, internal val protocolId: Int) {
    internal val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
    internal val dataDir = FileLocations.dataDir.resolve(protocolName)
    internal val latestDataDir = FileLocations.dataDir.resolve(protocols[0].name)
    internal val cacheMembers = mutableMapOf<String, (Emitter) -> Unit>()
    internal var currentProtocolId: Int = protocolId
    internal var defaultConstructProtocolId: Int? = null
    internal var fixRegistriesProtocolOverride: Int? = null

    fun compile() {
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", latestDataDir.resolve("cpackets.csv"), false)

        emitPacketSenders(emitter, "sendToClient", true)
        emitPacketSenders(emitter, "sendToServer", false)

        emitDoesServerKnow(emitter)
        emitDoesServerKnowMulticonnect(emitter)

        emitRemapFunc(emitter, clientbound = true, identifier = false)
        emitRemapFunc(emitter, clientbound = false, identifier = false)
        emitRemapFunc(emitter, clientbound = true, identifier = true)
        emitRemapFunc(emitter, clientbound = false, identifier = true)

        createIntRemapFunc(Registries.BLOCK_STATE, true)
        createIntRemapFunc(Registries.BLOCK_STATE, false)

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
            .append("<").appendClassName(BYTE_BUF).append("> outBufs, ")
            .appendClassName(NETWORK_HANDLER).append(" networkHandler, ")
            .appendClassName(MAP).append("<").appendClassName(CLASS).append("<?>, ").appendClassName(OBJECT).append("> globalData, ")
            .appendClassName(TYPED_MAP).append(" userData) {").indent().appendNewLine()
        val packets = TreeMap<Int, McNode>()
        for ((id, clazz) in readCsv<PacketType>(packetsFile)) {
            val packetFunctionName = "translate${clazz.substringAfterLast('.')}"
            if (emitPacketTranslator(emitter, packetFunctionName, clazz, clientbound)) {
                packets[id] = McNode(
                    FunctionCallOp(
                        emitter.currentClass,
                        packetFunctionName,
                        listOf(
                            McType.BYTE_BUF,
                            McType.BYTE_BUF.listOf(),
                            McType.DeclaredType(NETWORK_HANDLER),
                            McType.DeclaredType(MAP),
                            McType.DeclaredType(TYPED_MAP),
                        ),
                        McType.VOID,
                        true
                    ),
                    McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF)),
                    McNode(LoadVariableOp(VariableId.immediate("outBufs"), McType.BYTE_BUF.listOf())),
                    McNode(LoadVariableOp(VariableId.immediate("networkHandler"), McType.DeclaredType(NETWORK_HANDLER))),
                    McNode(LoadVariableOp(VariableId.immediate("globalData"), McType.DeclaredType(MAP))),
                    McNode(LoadVariableOp(VariableId.immediate("userData"), McType.DeclaredType(TYPED_MAP))),
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
            .append("<").appendClassName(BYTE_BUF).append("> outBufs, ")
            .appendClassName(NETWORK_HANDLER).append(" networkHandler, ")
            .appendClassName(MAP).append("<").appendClassName(CLASS).append("<?>, ").appendClassName(OBJECT).append("> globalData, ")
            .appendClassName(TYPED_MAP).append(" userData) {").indent().appendNewLine()
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

        emitter.addMember("SENDERS_$functionName")?.let { field ->
            field.append("private static final ").appendClassName(MAP).append("<")
        keyType(field)
            field.append(", ")
        field.appendClassName(PACKET_SENDER)
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
                    field.appendClassName(PACKET_SENDER)
                    field.append(") (packet, outBufs, networkHandler, globalData, userData) -> ").append(specializedFunctionName).append("((")
                        .appendClassName(packet).append(") packet, outBufs, networkHandler, globalData, userData)")

                    emitter.addMember(specializedFunctionName)?.let { specializedFunction ->
                        specializedFunction.append("private static void ").append(specializedFunctionName)
                            .append("(").appendClassName(packet).append(" protocol_$protocolId, ").appendClassName(LIST)
                            .append("<").appendClassName(BYTE_BUF).append("> outBufs, ")
                            .appendClassName(NETWORK_HANDLER).append(" networkHandler, ")
                            .appendClassName(MAP).append("<").appendClassName(CLASS).append("<?>, ").appendClassName(OBJECT).append("> globalData, ")
                            .appendClassName(TYPED_MAP).append(" userData) {").indent().appendNewLine()
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
            .append("<").appendClassName(BYTE_BUF).append("> outBufs, ")
            .appendClassName(NETWORK_HANDLER).append(" networkHandler, ")
            .appendClassName(MAP).append("<").appendClassName(CLASS).append("<?>, ").appendClassName(OBJECT).append("> globalData, ")
            .appendClassName(TYPED_MAP).append(" userData) {").indent().appendNewLine()

        function.append(PACKET_SENDER)
        function.append(" sender = SENDERS_").append(functionName).append(".get(")
        if (!clientbound) {
            function.appendClassName(PAIR).append(".of(")
        }
        function.append("packet.getClass()")
        if (!clientbound) {
            function.append(", fromProtocol)")
        }
        function.append(");").appendNewLine()
        function.append("if (sender == null) {").indent().appendNewLine()
        function.append("throw new ").appendClassName("java.lang.IllegalArgumentException")
            .append("(\"Cannot send packet \" + packet.getClass().getSimpleName() + \" to protocol $protocolId")
        if (!clientbound) {
            function.append(" from protocol \" + fromProtocol")
        } else {
            function.append("\"")
        }
        function.append(");").dedent().appendNewLine().append("}").appendNewLine()
        function.append("sender.send(packet, outBufs, networkHandler, globalData, userData);")
            .dedent().appendNewLine().append("}")
    }

    private fun emitDoesServerKnow(emitter: Emitter) {
        val function = emitter.addMember("doesServerKnow") ?: return
        function.append("public static boolean doesServerKnow(")
            .appendClassName(REGISTRY_KEY).append("<? extends ").appendClassName(REGISTRY)
            .append("<?>> registry, int newId) {").indent().appendNewLine()
            .appendClassName(BITSET).append(" bitset = DOES_SERVER_KNOW.get(registry);").appendNewLine()
            .append("if (bitset == null) {").indent().appendNewLine()
            .append("return true;").dedent().appendNewLine()
            .append("}").appendNewLine()
            .append("return bitset.get(newId);").dedent().appendNewLine()
            .append("}")

        val field = emitter.addMember("DOES_SERVER_KNOW") ?: return
        field.append("private static final ").appendClassName(MAP)
            .append("<").appendClassName(REGISTRY_KEY).append("<? extends ").appendClassName(REGISTRY)
            .append("<?>>, ").appendClassName(BITSET).append("> DOES_SERVER_KNOW = ")
            .appendClassName(PACKET_INTRINSICS).append(".makeMap(").indent()
        var addedAnyRegistry = false
        for (registry in Registries.values()) {
            if (registry == Registries.BLOCK_STATE) {
                continue
            }

            if(addedAnyRegistry) {
                field.append(",")
            }
            addedAnyRegistry = true

            field.appendNewLine().appendClassName(REGISTRY).append(".").append(registry.registryKeyFieldName)
                .append(", ").appendClassName(PACKET_INTRINSICS).append(".makeRLEBitSet(")

            val bitset = BitSet()
            currentProtocolId = protocols[0].id
            val latestEntries = registry.entries
            currentProtocolId = protocolId
            val protocolEntries = registry.entries
            for (entry in latestEntries) {
                bitset.set(entry.id, protocolEntries.byName(entry.name) != null)
            }

            McNode(CstStringOp(encodeRLEBitSet(bitset))).emit(field, Precedence.COMMA)
            field.append(")")
        }
        field.dedent().appendNewLine().append(");")
    }

    private fun emitDoesServerKnowMulticonnect(emitter: Emitter) {
        val function = emitter.addMember("doesServerKnowMulticonnect") ?: return
        function.append("public static boolean doesServerKnowMulticonnect(")
            .appendClassName(REGISTRY_KEY).append("<?> value) {").indent().appendNewLine()
            .append("return DOES_SERVER_KNOW_MULTICONNECT.contains(value);").dedent().appendNewLine()
            .append("}")

        val field = emitter.addMember("DOES_SERVER_KNOW_MULTICONNECT") ?: return
        field.append("private static final ").appendClassName(SET).append("<")
            .appendClassName(REGISTRY_KEY).append("<?>>").append(" DOES_SERVER_KNOW_MULTICONNECT = ")
            .appendClassName(SET).append(".of(").indent()
        var addedAnyKey = false
        for (registry in Registries.values()) {
            if (registry == Registries.BLOCK_STATE) {
                continue
            }

            for (entry in registry.entries) {
                if (!entry.name.startsWith("multiconnect:")) {
                    continue
                }

                if (addedAnyKey) {
                    field.append(",")
                }
                addedAnyKey = true

                field.appendNewLine().appendClassName(REGISTRY_KEY).append(".of(").appendClassName(REGISTRY)
                    .append(".").append(registry.registryKeyFieldName).append(", new ").appendClassName(IDENTIFIER)
                    .append("(\"multiconnect\", \"").append(entry.name.substring("multiconnect:".length)).append("\"))")
            }
        }
        field.dedent().appendNewLine().append(");")
    }

    private fun emitRemapFunc(emitter: Emitter, clientbound: Boolean, identifier: Boolean) {
        val methodName = "remap%s%s".format(if (clientbound) "S" else "C", if (identifier) "Identifier" else "Int")
        val fieldName = "REMAP_%s_%s".format(if (clientbound) "S" else "C", if (identifier) "IDENTIFIER" else "INT")

        fun emitType(emitter: Emitter) {
            if (identifier) {
                emitter.appendClassName(IDENTIFIER)
            } else {
                emitter.append("int")
            }
        }

        fun emitUnaryOperator(emitter: Emitter) {
            if (identifier) {
                emitter.appendClassName(UNARY_OPERATOR).append("<").appendClassName(IDENTIFIER).append(">")
            } else {
                emitter.appendClassName(INT_UNARY_OPERATOR)
            }
        }

        val function = emitter.addMember(methodName) ?: return
        function.append("public static ")
        emitType(function)
        function.append(" ").append(methodName).append("(")
            .appendClassName(REGISTRY_KEY).append("<? extends ").appendClassName(REGISTRY)
            .append("<?>> registry, ")
        emitType(function)
        function.append(" value) {").indent().appendNewLine()
        emitUnaryOperator(function)
        function.append(" remapper = ").append(fieldName)
            .append(".get(registry);").appendNewLine()
            .append("if (remapper == null) {").indent().appendNewLine()
            .append("return value;").dedent().appendNewLine()
            .append("}").appendNewLine()
            .append("return remapper.apply")
        if (!identifier) {
            function.append("AsInt")
        }
        function.append("(value);").dedent().appendNewLine().append("}")

        val field = emitter.addMember(fieldName) ?: return
        field.append("private static final ").appendClassName(MAP).append("<").appendClassName(REGISTRY_KEY)
            .append("<? extends ").appendClassName(REGISTRY).append("<?>>, ")
        emitUnaryOperator(field)
        field.append("> ").append(fieldName).append(" = ").appendClassName(PACKET_INTRINSICS).append(".makeMap(")
            .indent()
        var addedAny = false
        for (registry in Registries.values()) {
            if (registry == Registries.BLOCK_STATE) {
                continue
            }
            if (addedAny) {
                field.append(",")
            }
            addedAny = true
            field.appendNewLine().appendClassName(REGISTRY).append(".").append(registry.registryKeyFieldName)
                .append(", (")
            emitUnaryOperator(field)
            field.append(") ").appendClassName(className).append("::").append(
                if (identifier) {
                    createStringRemapFunc(registry, clientbound)
                } else {
                    createIntRemapFunc(registry, clientbound)
                }
            )
        }
        field.dedent().appendNewLine().append(");")
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
                McNode(StmtListOp, McNode(ReturnStmtOp(McType.DeclaredType(className)), generateDefaultConstructGraph(getMessageVariantInfo(className), null)))
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

    internal fun Registries.getOldName(value: String): String? {
        val entry = entries.byName(value)

        if (entry == null && currentProtocolId != protocolId && value.startsWith("multiconnect:")) {
            return value
        }

        return entry?.oldName
    }

    internal val Registries.entries: List<RegistryEntry> get() {
        val registryDir = FileLocations.dataDir.resolve(protocolNamesById[currentProtocolId]!!)
        return readCsv(registryDir.resolve("${this.name.lowercase()}.csv"))
    }
}
