package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BITSET
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BLOCK_STATE
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CommonClassNames.CLASS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.RESOURCE_LOCATION
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_UNARY_OPERATOR
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.MAP
import net.earthcomputer.multiconnect.compiler.CommonClassNames.CLIENT_PACKET_LISTENER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OBJECT
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_INTRINSICS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_SENDER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PAIR
import net.earthcomputer.multiconnect.compiler.CommonClassNames.RAW_PACKET_SENDER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.RESOURCE_KEY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.SET
import net.earthcomputer.multiconnect.compiler.CommonClassNames.START_SEND_PACKET_RESULT
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
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CstStringOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.NewOp
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.ThrowStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.opto.optimize
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
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
    private val cacheMembers = mutableMapOf<String, (Emitter) -> Unit>()
    internal var currentProtocolId: Int = protocolId
    internal var defaultConstructProtocolId: Int? = null
    internal var fixRegistriesProtocolOverride: Int? = null
    private val readDependenciesCache = mutableMapOf<String, List<String>>()
    private val writeDependenciesCache = mutableMapOf<String, List<String>>()
    internal val readDependencies = TreeSet<String>()
    internal val writeDependencies = TreeSet<String>()
    private val dependencyIds = mutableMapOf<List<String>, Int>()

    fun compile() {
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        // These need to be first because they track global data dependencies
        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", latestDataDir.resolve("cpackets.csv"), false)

        emitPacketSenders(emitter, "sendToClient", true)
        emitPacketSenders(emitter, "sendToServer", false)

        emitDoesServerKnow(emitter)
        emitDoesServerKnowMulticonnect(emitter)
        emitDoesServerKnowBlockState(emitter)
        emitDoesServerKnowBlockStateMulticonnect(emitter)

        emitRemapFunc(emitter, clientbound = true, resourceLocation = false)
        emitRemapFunc(emitter, clientbound = false, resourceLocation = false)
        emitRemapFunc(emitter, clientbound = true, resourceLocation = true)
        emitRemapFunc(emitter, clientbound = false, resourceLocation = true)

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

    internal fun addMember(name: String, emitter: (Emitter) -> Unit) {
        if (cacheMembers.containsKey(name)) {
            readDependencies += readDependenciesCache[name]!!
            writeDependencies += writeDependenciesCache[name]!!
            return
        }
        // Run a dry-run of the emitter to grab its read/write dependencies
        val prevReadDeps = readDependencies.toList()
        val prevWriteDeps = writeDependencies.toList()
        readDependencies.clear()
        writeDependencies.clear()
        emitter(Emitter(className, TreeSet(), TreeMap(), StringBuilder()))
        val memberReadDeps = readDependencies.toList()
        val memberWriteDeps = writeDependencies.toList()
        readDependencies.clear()
        writeDependencies.clear()
        readDependencies += prevReadDeps
        readDependencies += memberReadDeps
        writeDependencies += prevWriteDeps
        writeDependencies += memberWriteDeps
        readDependenciesCache[name] = memberReadDeps
        writeDependenciesCache[name] = memberWriteDeps
        cacheMembers[name] = emitter
    }

    private fun emitPacketTranslators(emitter: Emitter, functionName: String, packetsFile: File, clientbound: Boolean) {
        val function = emitter.addMember(functionName) ?: return
        function.append("public static ").appendClassName(START_SEND_PACKET_RESULT).append(" ").append(functionName)
            .append("(").appendClassName(BYTE_BUF).append(" buf) {").indent().appendNewLine()
        val packets = TreeMap<Int, McNode>()

        val retType = McType.DeclaredType(START_SEND_PACKET_RESULT)
        val senderType = McType.DeclaredType(RAW_PACKET_SENDER)
        val classType = McType.DeclaredType(CLASS)

        for ((id, clazz) in readCsv<PacketType>(packetsFile)) {
            if (readDependencies.isNotEmpty() || writeDependencies.isNotEmpty()) {
                throw AssertionError("readDependencies or writeDependencies is not empty")
            }
            val packetFunctionName = "translate${clazz.substringAfterLast('.')}"
            if (emitPacketTranslator(emitter, packetFunctionName, clazz, clientbound)) {
                packets[id] = McNode(NewOp(START_SEND_PACKET_RESULT, listOf(classType.arrayOf(), classType.arrayOf(), senderType)),
                    McNode(LoadFieldOp(McType.DeclaredType(className), makeDependencyField(readDependencies), classType.arrayOf(), isStatic = true)),
                    McNode(LoadFieldOp(McType.DeclaredType(className), makeDependencyField(writeDependencies), classType.arrayOf(), isStatic = true)),
                    McNode(LoadVariableOp(
                        VariableId.immediate("${splitPackageClass(className).second}::$packetFunctionName"),
                        senderType
                    ))
                )
            }
            readDependencies.clear()
            writeDependencies.clear()
        }
        val packetIdVar = VariableId.immediate("packetId")
        val stmt = McNode(StmtListOp,
            McNode(StoreVariableStmtOp(packetIdVar, McType.INT, true),
                IoOps.readType(VariableId.immediate("buf"), Types.VAR_INT)
            ),
            McNode(ReturnStmtOp(retType),
                McNode(SwitchOp(packets.keys as SortedSet<Int>, true, McType.INT, retType),
                    mutableListOf<McNode>().apply {
                        add(McNode(LoadVariableOp(packetIdVar, McType.INT)))
                        addAll(packets.values)
                        add(McNode(StmtListOp,
                            McNode(ThrowStmtOp,
                                McNode(NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                                    McNode(BinaryExpressionOp("+", McType.STRING, McType.INT),
                                        McNode(CstStringOp("Bad packet id: ")),
                                        McNode(LoadVariableOp(packetIdVar, McType.INT))
                                    )
                                )
                            )
                        ))
                    }
                )
            )
        ).optimize()
        stmt.emit(function, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
    }

    private fun makeDependencyField(classes: Collection<String>): String {
        val classesList = classes.toList()
        var id = dependencyIds[classesList]
        val exists = id != null
        if (!exists) {
            id = dependencyIds.size
            dependencyIds[classesList] = id
        }
        val fieldName = "DEPENDENCY_$id"
        if (exists) {
            return fieldName
        }
        addMember(fieldName) { emitter ->
            emitter.append("private static final ").appendClassName(CLASS).append("<?>[] ").append(fieldName)
                .append(" = { ")
            for ((index, clazz) in classesList.withIndex()) {
                if (index != 0) {
                    emitter.append(", ")
                }
                emitter.appendClassName(clazz).append(".class")
            }
            emitter.append(" };")
        }
        return fieldName
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
            .appendClassName(CLIENT_PACKET_LISTENER).append(" connection, ")
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
                    } + listOfNotNull(protocols[0].id.takeIf { variantInfo.sendableFromLatest })
                }
                val packetImpls = if (variantInfo.polymorphic != null && variantInfo.polymorphicParent == null) {
                    polymorphicChildren[packet]!!
                } else {
                    listOf(packet)
                }
                for (packetImpl in packetImpls) {
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
                        field.appendClassName(packetImpl).append(".class")
                        if (!clientbound) {
                            field.append(", ").append(protocolId.toString()).append(")")
                        }
                        field.append(", (")
                        field.appendClassName(PACKET_SENDER)
                        field.append(") (packet, outBufs, connection, globalData, userData) -> ").append(specializedFunctionName).append("((")
                            .appendClassName(packet).append(") packet, outBufs, connection, globalData, userData)")

                        emitter.addMember(specializedFunctionName)?.let { specializedFunction ->
                            specializedFunction.append("private static void ").append(specializedFunctionName)
                                .append("(").appendClassName(packet).append(" protocol_$protocolId, ").appendClassName(LIST)
                                .append("<").appendClassName(BYTE_BUF).append("> outBufs, ")
                                .appendClassName(CLIENT_PACKET_LISTENER).append(" connection, ")
                                .appendClassName(MAP).append("<").appendClassName(CLASS).append("<?>, ").appendClassName(OBJECT).append("> globalData, ")
                                .appendClassName(TYPED_MAP).append(" userData) {").indent().appendNewLine()
                            generateExplicitSenderClientRegistries(variantInfo, protocolId, clientbound)
                                .optimize().emit(specializedFunction, Precedence.COMMA)
                            specializedFunction.dedent().appendNewLine().append("}")
                        }
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
            .appendClassName(CLIENT_PACKET_LISTENER).append(" connection, ")
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
        function.append("sender.send(packet, outBufs, connection, globalData, userData);")
            .dedent().appendNewLine().append("}")
    }

    private fun emitDoesServerKnow(emitter: Emitter) {
        val function = emitter.addMember("doesServerKnow") ?: return
        function.append("public static boolean doesServerKnow(")
            .appendClassName(RESOURCE_KEY).append("<? extends ").appendClassName(REGISTRY)
            .append("<?>> registry, int newId) {").indent().appendNewLine()
            .appendClassName(BITSET).append(" bitset = DOES_SERVER_KNOW.get(registry);").appendNewLine()
            .append("if (bitset == null) {").indent().appendNewLine()
            .append("return true;").dedent().appendNewLine()
            .append("}").appendNewLine()
            .append("return bitset.get(newId);").dedent().appendNewLine()
            .append("}")

        val field = emitter.addMember("DOES_SERVER_KNOW") ?: return
        field.append("private static final ").appendClassName(MAP)
            .append("<").appendClassName(RESOURCE_KEY).append("<? extends ").appendClassName(REGISTRY)
            .append("<?>>, ").appendClassName(BITSET).append("> DOES_SERVER_KNOW = ")
            .appendClassName(PACKET_INTRINSICS).append(".makeMap(").indent()
        var addedAnyRegistry = false
        for (registry in Registries.values()) {
            if (!registry.isRealRegistry) {
                continue
            }

            if(addedAnyRegistry) {
                field.append(",")
            }
            addedAnyRegistry = true

            field.appendNewLine().appendClassName(REGISTRY).append(".").append(registry.resourceKeyFieldName)
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
            .appendClassName(RESOURCE_KEY).append("<?> value) {").indent().appendNewLine()
            .append("return DOES_SERVER_KNOW_MULTICONNECT.contains(value);").dedent().appendNewLine()
            .append("}")

        val field = emitter.addMember("DOES_SERVER_KNOW_MULTICONNECT") ?: return
        field.append("private static final ").appendClassName(SET).append("<")
            .appendClassName(RESOURCE_KEY).append("<?>>").append(" DOES_SERVER_KNOW_MULTICONNECT = ")
            .appendClassName(SET).append(".of(").indent()
        var addedAnyKey = false
        for (registry in Registries.values()) {
            if (!registry.isRealRegistry) {
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

                field.appendNewLine().appendClassName(RESOURCE_KEY).append(".create(").appendClassName(REGISTRY)
                    .append(".").append(registry.resourceKeyFieldName).append(", new ").appendClassName(RESOURCE_LOCATION)
                    .append("(\"multiconnect\", \"").append(entry.name.substring("multiconnect:".length)).append("\"))")
            }
        }
        field.dedent().appendNewLine().append(");")
    }

    private fun emitDoesServerKnowBlockState(emitter: Emitter) {
        val function = emitter.addMember("doesServerKnowBlockState") ?: return
        function.append("public static boolean doesServerKnowBlockState(int newId) {").indent()
            .appendNewLine().append("return DOES_SERVER_KNOW_BLOCK_STATE.get(newId);").dedent()
            .appendNewLine().append("}")

        val field = emitter.addMember("DOES_SERVER_KNOW_BLOCK_STATE") ?: return
        field.append("private static final ").appendClassName(BITSET)
            .append(" DOES_SERVER_KNOW_BLOCK_STATE = ").appendClassName(PACKET_INTRINSICS)
            .append(".makeRLEBitSet(")
        val bitset = BitSet()
        currentProtocolId = protocols[0].id
        val latestEntries = Registries.BLOCK_STATE.entries
        currentProtocolId = protocolId
        val protocolEntries = Registries.BLOCK_STATE.entries
        for (entry in latestEntries) {
            bitset.set(entry.id, protocolEntries.byName(entry.name) != null)
        }
        McNode(CstStringOp(encodeRLEBitSet(bitset))).emit(field, Precedence.COMMA)
        field.append(");")
    }

    private fun emitDoesServerKnowBlockStateMulticonnect(emitter: Emitter) {
        val function = emitter.addMember("doesServerKnowBlockStateMulticonnect") ?: return
        function.append("public static boolean doesServerKnowBlockStateMulticonnect(")
            .appendClassName(BLOCK_STATE).append(" value) {").indent().appendNewLine()
            .append("return DOES_SERVER_KNOW_BLOCK_STATE_MULTICONNECT.contains(value);").dedent().appendNewLine()
            .append("}")

        val field = emitter.addMember("DOES_SERVER_KNOW_BLOCK_STATE_MULTICONNECT") ?: return
        field.append("private static final ").appendClassName(SET).append("<")
            .appendClassName(BLOCK_STATE).append(">").append(" DOES_SERVER_KNOW_BLOCK_STATE_MULTICONNECT = ")
            .appendClassName(PACKET_INTRINSICS).append(".makeMulticonnectBlockStateSet(").indent()
        var addedAnyKey = false
        for (entry in Registries.BLOCK_STATE.entries) {
            if (!entry.name.startsWith("multiconnect:")) {
                continue
            }

            if (addedAnyKey) {
                field.append(",")
            }
            addedAnyKey = true

            field.appendNewLine().append("\"").append(entry.name.substring("multiconnect:".length)).append("\"")
        }
        field.dedent().appendNewLine().append(");")
    }

    private fun emitRemapFunc(emitter: Emitter, clientbound: Boolean, resourceLocation: Boolean) {
        val methodName = "remap%s%s".format(if (clientbound) "S" else "C", if (resourceLocation) "ResourceLocation" else "Int")
        val fieldName = "REMAP_%s_%s".format(if (clientbound) "S" else "C", if (resourceLocation) "RESOURCE_LOCATION" else "INT")

        fun emitType(emitter: Emitter) {
            if (resourceLocation) {
                emitter.appendClassName(RESOURCE_LOCATION)
            } else {
                emitter.append("int")
            }
        }

        fun emitUnaryOperator(emitter: Emitter) {
            if (resourceLocation) {
                emitter.appendClassName(UNARY_OPERATOR).append("<").appendClassName(RESOURCE_LOCATION).append(">")
            } else {
                emitter.appendClassName(INT_UNARY_OPERATOR)
            }
        }

        val function = emitter.addMember(methodName) ?: return
        function.append("public static ")
        emitType(function)
        function.append(" ").append(methodName).append("(")
            .appendClassName(RESOURCE_KEY).append("<? extends ").appendClassName(REGISTRY)
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
        if (!resourceLocation) {
            function.append("AsInt")
        }
        function.append("(value);").dedent().appendNewLine().append("}")

        val field = emitter.addMember(fieldName) ?: return
        field.append("private static final ").appendClassName(MAP).append("<").appendClassName(RESOURCE_KEY)
            .append("<? extends ").appendClassName(REGISTRY).append("<?>>, ")
        emitUnaryOperator(field)
        field.append("> ").append(fieldName).append(" = ").appendClassName(PACKET_INTRINSICS).append(".makeMap(")
            .indent()
        var addedAny = false
        for (registry in Registries.values()) {
            if (!registry.isRealRegistry) {
                continue
            }
            if (addedAny) {
                field.append(",")
            }
            addedAny = true
            field.appendNewLine().appendClassName(REGISTRY).append(".").append(registry.resourceKeyFieldName)
                .append(", (")
            emitUnaryOperator(field)
            field.append(") ").appendClassName(className).append("::").append(
                if (resourceLocation) {
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

        if (entry != null) {
            return Either.Left(entry.id)
        }

        // load id dynamically if necessary
        if (currentProtocolId != protocolId && value.startsWith("multiconnect:")) {
            val registryType = McType.DeclaredType(REGISTRY)
            val resourceKeyType = McType.DeclaredType(RESOURCE_KEY)
            val registryElementType = McType.DeclaredType("RegistryElement")
            return Either.Right(
                McNode(FunctionCallOp(REGISTRY, "getId", listOf(registryType, registryElementType), McType.INT, false, isStatic = false),
                    McNode(LoadFieldOp(registryType, this.name, registryType, isStatic = true)),
                    McNode(FunctionCallOp(REGISTRY, "get", listOf(registryType, resourceKeyType), registryElementType, false, isStatic = false),
                        McNode(LoadFieldOp(registryType, this.name, registryType, isStatic = true)),
                        McNode(LoadFieldOp(McType.DeclaredType(className), createResourceKeyField(this, value), resourceKeyType, isStatic = true))
                    )
                )
            )
        }

        checkNameLegal(value)
        return null
    }

    internal fun Registries.getOldName(value: String): String? {
        val entry = entries.byName(value)

        if (entry != null) {
            return entry.oldName
        }

        if (currentProtocolId != protocolId && value.startsWith("multiconnect:")) {
            return value
        }

        checkNameLegal(value)
        return null
    }

    private fun Registries.checkNameLegal(value: String) {
        // check if the name appears in the registry in any version
        val prevCurrentProtocolId = currentProtocolId
        for (protocol in protocols) {
            currentProtocolId = protocol.id
            if (entries.byName(value) != null) {
                currentProtocolId = prevCurrentProtocolId
                return
            }
        }
        currentProtocolId = prevCurrentProtocolId
        throw CompileException("Unknown ${this.name} registry entry: $value")
    }

    internal val Registries.entries: List<RegistryEntry> get() {
        val registryDir = FileLocations.dataDir.resolve(protocolNamesById[currentProtocolId]!!)
        return readCsv(registryDir.resolve("${this.name.lowercase()}.csv"))
    }
}
