package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.REGISTRY_KEY
import net.earthcomputer.multiconnect.compiler.CommonClassNames.UNPOOLED
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.Either
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.FileLocations
import net.earthcomputer.multiconnect.compiler.IoOps
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.PacketType
import net.earthcomputer.multiconnect.compiler.ProtocolEntry
import net.earthcomputer.multiconnect.compiler.RegistryEntry
import net.earthcomputer.multiconnect.compiler.byName
import net.earthcomputer.multiconnect.compiler.classInfoOrNull
import net.earthcomputer.multiconnect.compiler.componentType
import net.earthcomputer.multiconnect.compiler.deepComponentType
import net.earthcomputer.multiconnect.compiler.getClassInfo
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.hasName
import net.earthcomputer.multiconnect.compiler.messageVariantInfo
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CastOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstStringOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.IfStmtOp
import net.earthcomputer.multiconnect.compiler.node.InstanceOfOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.NewOp
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.ReturnVoidStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.ThrowStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.WhileStmtOp
import net.earthcomputer.multiconnect.compiler.opto.optimize
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.protocolNamesById
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.readCsv
import net.earthcomputer.multiconnect.compiler.splitPackageClass
import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

enum class PacketDirection {
    SERVERBOUND, CLIENTBOUND, INVALID
}

private fun getPacketDirection(protocolId: Int, className: String): PacketDirection {
    val protocolName = protocolNamesById[protocolId]!!
    for ((dir, fileName) in listOf(
        PacketDirection.CLIENTBOUND to "spackets.csv",
        PacketDirection.SERVERBOUND to "cpackets.csv"
    )) {
        if (readCsv<PacketType>(FileLocations.dataDir.resolve(protocolName).resolve(fileName)).any { it.clazz == className }) {
            return dir
        }
    }
    return PacketDirection.INVALID
}

class ProtocolCompiler(internal val protocolName: String, internal val protocolId: Int) {
    internal val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
    internal val dataDir = FileLocations.dataDir.resolve(protocolName)
    internal val latestDataDir = FileLocations.dataDir.resolve(protocols[0].name)
    internal val cacheMembers = mutableMapOf<String, (Emitter) -> Unit>()
    internal var currentProtocolId: Int = protocolId

    fun compile() {
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", latestDataDir.resolve("cpackets.csv"), false)

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
        generatePacketGraph(messageVariantInfo, clientbound).optimize().emit(function, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
        return true
    }

    private fun generatePacketGraph(messageVariantInfo: MessageVariantInfo, clientbound: Boolean): McNode {
        val group = getClassInfo(messageVariantInfo.variantOf ?: messageVariantInfo.className) as? MessageInfo
        fun getPacket(protocol: Int): MessageVariantInfo {
            if (group == null) {
                return messageVariantInfo
            }
            return group.getVariant(protocol) ?: throw CompileException("No variant of packet \"${group.className}\" found for protocol $protocol")
        }

        var protocolsSubset = protocols.takeWhile { it.id >= protocolId }
        if (clientbound) {
            protocolsSubset = protocolsSubset.reversed()
        }

        val nodes = mutableListOf<McNode>()
        if (!clientbound) {
            currentProtocolId = protocols[0].id
        }
        nodes += McNode(StoreVariableStmtOp(VariableId.immediate("protocol_${protocolsSubset.first().id}"), messageVariantInfo.toMcType(), true),
            generateMessageReadGraph(getPacket(protocolsSubset.first().id))
        )
        if (!clientbound) {
            currentProtocolId = protocolId

            nodes += if (group != null) {
                fixRegistries(group, VariableId.immediate("protocol_${protocolsSubset.first().id}"), false)
            } else {
                fixRegistries(messageVariantInfo, VariableId.immediate("protocol_${protocolsSubset.first().id}"), false)
            }
        }

        nodes += generatePartialHandlers(protocolsSubset, 0, ::getPacket)
        val (handler0, handled0) = generateHandler(VariableId.immediate("protocol_${protocolsSubset.first().id}"), protocolsSubset.getOrNull(1)?.id, getPacket(protocolsSubset[0].id), clientbound)
        nodes += handler0
        if (handled0) {
            return McNode(StmtListOp, nodes)
        }

        for (index in protocolsSubset.indices.drop(1)) {
            nodes += McNode(StoreVariableStmtOp(VariableId.immediate("protocol_${protocolsSubset[index].id}"), group?.toMcType() ?: messageVariantInfo.toMcType(), true),
                if (group != null) {
                    translate(group, protocolsSubset[index - 1].id, protocolsSubset[index].id, VariableId.immediate("protocol_${protocolsSubset[index - 1].id}"))
                } else {
                    translate(messageVariantInfo, protocolsSubset[index - 1].id, protocolsSubset[index].id, VariableId.immediate("protocol_${protocolsSubset[index - 1].id}"))
                }
            )
            nodes += generatePartialHandlers(protocolsSubset, index, ::getPacket)
            val (handler, handled) = generateHandler(VariableId.immediate("protocol_${protocolsSubset[index].id}"), protocolsSubset.getOrNull(index + 1)?.id, getPacket(protocolsSubset[index].id), clientbound)
            nodes += handler
            if (handled) {
                return McNode(StmtListOp, nodes)
            }
        }

        if (clientbound) {
            nodes += if (group != null) {
                fixRegistries(group, VariableId.immediate("protocol_${protocolsSubset.last().id}"), true)
            } else {
                fixRegistries(messageVariantInfo, VariableId.immediate("protocol_${protocolsSubset.last().id}"), true)
            }
        }

        val resultBufVar = VariableId.create()
        nodes += McNode(StoreVariableStmtOp(resultBufVar, McType.BYTE_BUF, true),
            McNode(FunctionCallOp(UNPOOLED, "buffer", listOf(), McType.BYTE_BUF, true))
        )
        if (clientbound) {
            currentProtocolId = protocols[0].id
        }
        nodes += if (group != null) {
            generateWriteGraph(group, VariableId.immediate("protocol_${protocolsSubset.last().id}"), resultBufVar)
        } else {
            generateWriteGraph(messageVariantInfo, VariableId.immediate("protocol_${protocolsSubset.last().id}"), resultBufVar)
        }
        if (clientbound) {
            currentProtocolId = protocolId
        }
        nodes += McNode(PopStmtOp,
            McNode(FunctionCallOp(LIST, "add", listOf(McType.BYTE_BUF.listOf(), McType.BYTE_BUF), McType.VOID, true, isStatic = false),
                McNode(LoadVariableOp(VariableId.immediate("outBufs"), McType.BYTE_BUF.listOf())),
                McNode(LoadVariableOp(resultBufVar, McType.BYTE_BUF))
            )
        )

        return McNode(StmtListOp, nodes)
    }

    private inline fun generatePartialHandlers(
        protocolsSubset: List<ProtocolEntry>,
        index: Int,
        getPacket: (Int) -> MessageVariantInfo
    ): McNode {
        val nodes = mutableListOf<McNode>()
        val packet = getPacket(protocolsSubset[index].id)
        if (index == protocolsSubset.lastIndex || packet.className != getPacket(protocolsSubset[index + 1].id).className) {
            for (partialHandler in packet.partialHandlers) {
                nodes += McNode(PopStmtOp,
                    generateFunctionCallGraph(packet.findFunction(partialHandler)) { name, type ->
                        McNode(LoadFieldOp(packet.toMcType(), name, type),
                            McNode(LoadVariableOp(VariableId.immediate("protocol_${protocolsSubset[index].id}"), packet.toMcType()))
                        )
                    }
                )
            }

            if (packet.polymorphic != null && packet.polymorphicParent == null) {
                var ifElseChain: McNode? = null
                for (childName in polymorphicChildren[packet.className]!!) {
                    val childMessage = getMessageVariantInfo(packet.className)
                    if (childMessage.partialHandlers.isNotEmpty()) {
                        val ifBlock = mutableListOf<McNode>()
                        val castedChild = McNode(CastOp(packet.toMcType(), childMessage.toMcType()),
                            McNode(LoadVariableOp(VariableId.immediate("protocol_${protocolsSubset[index].id}"), packet.toMcType()))
                        )
                        for (partialHandler in childMessage.partialHandlers.asReversed()) {
                            ifBlock += McNode(PopStmtOp,
                                generateFunctionCallGraph(childMessage.findFunction(partialHandler)) { name, type ->
                                    McNode(LoadFieldOp(childMessage.toMcType(), name, type), castedChild)
                                }
                            )
                        }
                        val condition = McNode(InstanceOfOp(packet.toMcType(), childMessage.toMcType()),
                            McNode(LoadVariableOp(VariableId.immediate("protocol_${protocolsSubset[index].id}"), packet.toMcType()))
                        )
                        ifElseChain = if (ifElseChain == null) {
                            McNode(IfStmtOp, condition, McNode(StmtListOp, ifBlock))
                        } else {
                            McNode(IfElseStmtOp, condition, McNode(StmtListOp, ifBlock), ifElseChain)
                        }
                    }
                }

                if (ifElseChain != null) {
                    nodes += ifElseChain
                }
            }
        }
        return McNode(StmtListOp, nodes)
    }

    private fun generateHandler(varId: VariableId, nextProtocolId: Int?, packet: MessageVariantInfo, clientbound: Boolean): Pair<McNode, Boolean> {
        if (packet.handler != null) {
            return generateHandlerInner(McNode(LoadVariableOp(varId, packet.toMcType())), nextProtocolId, packet, packet.handler, clientbound) to true
        }
        if (packet.polymorphic != null && packet.polymorphicParent == null) {
            val alwaysHandled = polymorphicChildren[packet.className]!!.all { getMessageVariantInfo(it).handler != null }
            var ifElseChain = if (alwaysHandled) {
                McNode(ThrowStmtOp,
                    McNode(NewOp("java.lang.IllegalStateException", listOf(McType.STRING)),
                        McNode(CstStringOp("Polymorphic subclass of \"${splitPackageClass(packet.className).second}\" has instance of illegal type"))
                    )
                )
            } else {
                null
            }
            for (childName in polymorphicChildren[packet.className]!!.asReversed()) {
                val childMessage = getMessageVariantInfo(childName)
                if (childMessage.handler != null) {
                    val condition = McNode(InstanceOfOp(packet.toMcType(), childMessage.toMcType()),
                        McNode(LoadVariableOp(varId, packet.toMcType()))
                    )
                    var ifBlock = generateHandlerInner(
                        McNode(CastOp(packet.toMcType(), childMessage.toMcType()),
                            McNode(LoadVariableOp(varId, packet.toMcType()))
                        ),
                        nextProtocolId,
                        childMessage,
                        childMessage.handler,
                        clientbound
                    )
                    if (!alwaysHandled) {
                        ifBlock = McNode(StmtListOp,
                            ifBlock,
                            McNode(ReturnVoidStmtOp)
                        )
                    }
                    ifElseChain = if (ifElseChain != null) {
                        McNode(IfElseStmtOp,
                            condition,
                            ifBlock,
                            McNode(StmtListOp, ifElseChain)
                        )
                    } else {
                        McNode(IfStmtOp,
                            condition,
                            ifBlock
                        )
                    }
                }
            }

            if (ifElseChain != null) {
                return ifElseChain to alwaysHandled
            }
        }

        return McNode(StmtListOp) to false
    }

    private fun generateHandlerInner(packetNode: McNode, nextProtocolId: Int?, packet: MessageVariantInfo, handlerName: String, clientbound: Boolean): McNode {
        val handlerFunc = packet.findFunction(handlerName)
        val functionCall = generateFunctionCallGraph(handlerFunc) { name, type ->
            McNode(LoadFieldOp(packet.toMcType(), name, type), packetNode)
        }
        if (handlerFunc.returnType == McType.VOID) {
            return McNode(PopStmtOp, functionCall)
        }

        if (nextProtocolId == null) {
            throw CompileException("@Handler ${splitPackageClass(packet.className).second}.$handlerName cannot return more packets as it is the last packet in the chain for protocol $protocolName")
        }

        val varId = VariableId.create()
        val nodes = mutableListOf<McNode>()

        nodes += McNode(StoreVariableStmtOp(varId, handlerFunc.returnType, true), functionCall)

        val possibleTypes = handlerFunc.possibleReturnTypes ?: if (handlerFunc.returnType.hasName(LIST)) {
            listOf(handlerFunc.returnType.componentType())
        } else {
            listOf(handlerFunc.returnType)
        }

        for (type in possibleTypes) {
            val info = type.classInfoOrNull as? MessageVariantInfo
            val isValid = info?.let {
                getPacketDirection(nextProtocolId, it.className) == (
                    if (clientbound) PacketDirection.CLIENTBOUND else PacketDirection.SERVERBOUND
                )
            } ?: false
            if (!isValid) {
                throw CompileException("Returning invalid type $type from packet @Handler")
            }
        }

        val elementVar = if (handlerFunc.returnType.hasName(LIST)) VariableId.create() else varId

        val handlerBody = if (handlerFunc.possibleReturnTypes.isNullOrEmpty()) {
            generateExplicitSender(handlerFunc.returnType.deepComponentType().messageVariantInfo, nextProtocolId, elementVar)
        } else {
            var ifElseChain = McNode(ThrowStmtOp,
                McNode(NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                    McNode(CstStringOp("Packet @Handler \"${splitPackageClass(packet.className).second}.$handlerName\" returned invalid type. Expected one of $possibleTypes"))
                )
            )
            for (returnType in handlerFunc.possibleReturnTypes.asReversed()) {
                val instanceVarId = VariableId.create()
                ifElseChain = McNode(IfElseStmtOp,
                    McNode(InstanceOfOp(handlerFunc.returnType.deepComponentType(), returnType),
                        McNode(LoadVariableOp(elementVar, handlerFunc.returnType.deepComponentType()))
                    ),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(instanceVarId, returnType, true),
                            McNode(CastOp(handlerFunc.returnType.deepComponentType(), returnType),
                                McNode(LoadVariableOp(elementVar, handlerFunc.returnType.deepComponentType()))
                            )
                        ),
                        generateExplicitSender(returnType.messageVariantInfo, nextProtocolId, instanceVarId)
                    ),
                    McNode(StmtListOp, ifElseChain)
                )
            }
            ifElseChain
        }

        if (handlerFunc.returnType.hasName(LIST)) {
            val indexVar = VariableId.create()
            val sizeVar = VariableId.create()
            nodes += McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0)))
            nodes += McNode(StoreVariableStmtOp(sizeVar, McType.INT, true),
                McNode(FunctionCallOp(LIST, "size", listOf(handlerFunc.returnType), McType.INT, false, isStatic = false),
                    McNode(LoadVariableOp(varId, handlerFunc.returnType))
                )
            )
            nodes += McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    McNode(LoadVariableOp(sizeVar, McType.INT))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(elementVar, handlerFunc.returnType.deepComponentType(), true),
                        McNode(FunctionCallOp(LIST, "get", listOf(handlerFunc.returnType, McType.INT), handlerFunc.returnType.deepComponentType(), false, isStatic = false),
                            McNode(LoadVariableOp(varId, handlerFunc.returnType)),
                            McNode(LoadVariableOp(indexVar, McType.INT))
                        )
                    ),
                    handlerBody,
                    McNode(StoreVariableStmtOp(indexVar, McType.INT, false, "+="), McNode(CstIntOp(1)))
                )
            )
        } else {
            nodes += handlerBody
        }

        return McNode(StmtListOp, nodes)
    }

    private fun generateExplicitSender(packet: MessageVariantInfo, protocolId: Int, packetVar: VariableId): McNode {
        val functionName = "translateExplicit${splitPackageClass(packet.className).second.replace('.', '_')}$protocolId"

        cacheMembers[functionName] = { emitter ->
            emitter.append("private static void ").append(functionName).append("(")
            packet.toMcType().emit(emitter)
            emitter.append(" packet, ").appendClassName(LIST).append("<").appendClassName(BYTE_BUF).append("> outBufs) {")
                .indent().appendNewLine()
            // TODO
            emitter.dedent().appendNewLine().append("}")
        }

        return McNode(PopStmtOp,
            McNode(FunctionCallOp(className, functionName, listOf(packet.toMcType(), McType.BYTE_BUF.listOf()), McType.VOID, true),
                McNode(LoadVariableOp(packetVar, packet.toMcType())),
                McNode(LoadVariableOp(VariableId.immediate("outBufs"), McType.BYTE_BUF.listOf()))
            )
        )
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
