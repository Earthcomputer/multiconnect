package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames
import net.earthcomputer.multiconnect.compiler.CommonClassNames.NETWORK_HANDLER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.TYPED_MAP
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.FileLocations
import net.earthcomputer.multiconnect.compiler.IoOps
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.PacketType
import net.earthcomputer.multiconnect.compiler.ProtocolEntry
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
import net.earthcomputer.multiconnect.compiler.node.ThrowStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.WhileStmtOp
import net.earthcomputer.multiconnect.compiler.opto.optimize
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.protocolNamesById
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.readCsv
import net.earthcomputer.multiconnect.compiler.splitPackageClass

// region TOP LEVEL FUNCTIONS

internal fun ProtocolCompiler.generateByteBufHandler(packet: MessageVariantInfo, clientbound: Boolean): McNode {
    val group = getClassInfo(packet.variantOf ?: packet.className) as? MessageInfo

    var protocolsSubset = protocols.takeWhile { it.id >= protocolId }
    if (clientbound) {
        protocolsSubset = protocolsSubset.reversed()
    }

    val nodes = mutableListOf<McNode>()

    nodes += generateRead(group, packet, protocolsSubset, clientbound)
    if (!clientbound) {
        nodes += generateFixRegistries(group, packet, protocolsSubset, false)
    }
    val (translator, handled) = generateTranslate(group, packet, protocolsSubset, clientbound)
    nodes += translator
    if (handled) {
        return McNode(StmtListOp, nodes)
    }
    if (clientbound) {
        nodes += generateFixRegistries(group, packet, protocolsSubset, true)
    }
    nodes += generateWrite(group, packet, protocolsSubset, clientbound)

    return McNode(StmtListOp, nodes)
}

internal fun ProtocolCompiler.generateExplicitSenderClientRegistries(packet: MessageVariantInfo, protocolId: Int, clientbound: Boolean): McNode {
    val group = getClassInfo(packet.variantOf ?: packet.className) as? MessageInfo

    val protocolsSubset = if (clientbound) {
        protocols.takeWhile { it.id >= protocolId }.reversed()
    } else {
        protocols.asSequence().dropWhile { it.id > protocolId }.takeWhile { it.id >= this.protocolId }.toList()
    }

    val nodes = mutableListOf<McNode>()
    if (!clientbound) {
        fixRegistriesProtocolOverride = protocolId
        nodes += generateFixRegistries(group, packet, protocolsSubset, false)
        fixRegistriesProtocolOverride = null
    }
    val (translator, handled) = generateTranslate(group, packet, protocolsSubset, clientbound)
    nodes += translator
    if (handled) {
        return McNode(StmtListOp, nodes)
    }
    nodes += generateWrite(group, packet, protocolsSubset, clientbound)

    return McNode(StmtListOp, nodes)
}

internal fun ProtocolCompiler.generateExplicitSenderServerRegistries(
    packet: MessageVariantInfo,
    protocolId: Int,
    packetVar: VariableId,
    clientbound: Boolean,
    bufsVar: VariableId = VariableId.immediate("outBufs")
): McNode {
    val functionName = "translateExplicit${splitPackageClass(packet.className).second.replace('.', '_')}$protocolId"

    cacheMembers[functionName] = { emitter ->
        emitter.append("private static void ").append(functionName).append("(")
        packet.toMcType().emit(emitter)
        emitter.append(" protocol_").append(protocolId.toString()).append(", ").appendClassName(CommonClassNames.LIST)
            .append("<").appendClassName(CommonClassNames.BYTE_BUF).append("> outBufs, ")
            .appendClassName(NETWORK_HANDLER).append(" networkHandler, ")
            .appendClassName(TYPED_MAP).append(" userData) {").indent().appendNewLine()

        val group = getClassInfo(packet.variantOf ?: packet.className) as? MessageInfo

        val protocolsSubset = if (clientbound) {
            protocols.takeWhile { it.id >= protocolId }.reversed()
        } else {
            protocols.asSequence().dropWhile { it.id > protocolId }.takeWhile { it.id >= this.protocolId }.toList()
        }

        val nodes = mutableListOf<McNode>()
        val (translator, handled) = generateTranslate(group, packet, protocolsSubset, clientbound)
        nodes += translator
        if (!handled) {
            if (clientbound) {
                nodes += generateFixRegistries(group, packet, protocolsSubset, clientbound)
            }
            nodes += generateWrite(group, packet, protocolsSubset, clientbound)
        }

        McNode(StmtListOp, nodes).optimize().emit(emitter, Precedence.COMMA)

        emitter.dedent().appendNewLine().append("}")
    }

    return McNode(PopStmtOp,
        McNode(FunctionCallOp(className, functionName, listOf(
            packet.toMcType(),
            McType.BYTE_BUF.listOf(),
            McType.DeclaredType(NETWORK_HANDLER),
            McType.DeclaredType(TYPED_MAP),
        ), McType.VOID, true),
            McNode(LoadVariableOp(packetVar, packet.toMcType())),
            McNode(LoadVariableOp(bufsVar, McType.BYTE_BUF.listOf())),
            McNode(LoadVariableOp(VariableId.immediate("networkHandler"), McType.DeclaredType(NETWORK_HANDLER))),
            McNode(LoadVariableOp(VariableId.immediate("userData"), McType.DeclaredType(TYPED_MAP))),
        )
    )
}

// endregion

// region PARTS

private fun ProtocolCompiler.generateRead(group: MessageInfo?, packet: MessageVariantInfo, protocolsSubset: List<ProtocolEntry>, clientbound: Boolean): McNode {
    if (!clientbound) {
        currentProtocolId = protocols[0].id
    }
    val ret = McNode(
        StoreVariableStmtOp(protocolVariable(protocolsSubset.first().id), packet.toMcType(), true),
        generateMessageReadGraph(getVariant(group, protocolsSubset.first().id, packet), null)
    )
    if (!clientbound) {
        currentProtocolId = protocolId
    }

    return ret
}

private fun ProtocolCompiler.generateFixRegistries(group: MessageInfo?, packet: MessageVariantInfo, protocolsSubset: List<ProtocolEntry>, clientbound: Boolean): McNode {
    val protocol = if (clientbound) protocolsSubset.last() else protocolsSubset.first()
    return if (group != null) {
        fixRegistries(group, protocolVariable(protocol.id), clientbound)
    } else {
        fixRegistries(packet, protocolVariable(protocol.id), clientbound)
    }
}

private fun ProtocolCompiler.generateTranslate(group: MessageInfo?, packet: MessageVariantInfo, protocolsSubset: List<ProtocolEntry>, clientbound: Boolean): Pair<McNode, Boolean> {
    val nodes = mutableListOf<McNode>()
    nodes += generatePartialHandlers(protocolsSubset, 0) { getVariant(group, it, packet) }
    val (handler0, handled0) = generateHandler(protocolVariable(protocolsSubset.first().id), protocolsSubset.getOrNull(1)?.id, getVariant(group, protocolsSubset[0].id, packet), clientbound)
    nodes += handler0
    if (handled0) {
        return McNode(StmtListOp, nodes) to true
    }

    for (index in protocolsSubset.indices.drop(1)) {
        nodes += McNode(
            StoreVariableStmtOp(protocolVariable(protocolsSubset[index].id), group?.toMcType() ?: packet.toMcType(), true),
            if (group != null) {
                translate(group, protocolsSubset[index - 1].id, protocolsSubset[index].id, protocolVariable(protocolsSubset[index - 1].id), null)
            } else {
                translate(packet, protocolsSubset[index - 1].id, protocolsSubset[index].id, protocolVariable(protocolsSubset[index - 1].id), null)
            }
        )
        nodes += generatePartialHandlers(protocolsSubset, index) { getVariant(group, it, packet) }
        val (handler, handled) = generateHandler(protocolVariable(protocolsSubset[index].id), protocolsSubset.getOrNull(index + 1)?.id, getVariant(group, protocolsSubset[index].id, packet), clientbound)
        nodes += handler
        if (handled) {
            return McNode(StmtListOp, nodes) to true
        }
    }

    return McNode(StmtListOp, nodes) to false
}

private inline fun ProtocolCompiler.generatePartialHandlers(
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
                    McNode(
                        LoadFieldOp(packet.toMcType(), name, type),
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
                    val castedChild = McNode(
                        CastOp(packet.toMcType(), childMessage.toMcType()),
                        McNode(LoadVariableOp(VariableId.immediate("protocol_${protocolsSubset[index].id}"), packet.toMcType()))
                    )
                    for (partialHandler in childMessage.partialHandlers.asReversed()) {
                        ifBlock += McNode(PopStmtOp,
                            generateFunctionCallGraph(childMessage.findFunction(partialHandler)) { name, type ->
                                McNode(LoadFieldOp(childMessage.toMcType(), name, type), castedChild)
                            }
                        )
                    }
                    val condition = McNode(
                        InstanceOfOp(packet.toMcType(), childMessage.toMcType()),
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

private fun ProtocolCompiler.generateHandler(varId: VariableId, nextProtocolId: Int?, packet: MessageVariantInfo, clientbound: Boolean): Pair<McNode, Boolean> {
    if (packet.handler != null && (clientbound || this.protocolId <= (packet.handlerProtocol ?: throw CompileException("@Handler protocol is required for serverbound packets (${packet.className})")))) {
        return generateHandlerInner(McNode(LoadVariableOp(varId, packet.toMcType())), nextProtocolId, packet, packet.handler, clientbound) to true
    }
    if (packet.polymorphic != null && packet.polymorphicParent == null) {
        val alwaysHandled = polymorphicChildren[packet.className]!!.all { getMessageVariantInfo(it).handler != null }
        var ifElseChain = if (alwaysHandled) {
            McNode(
                ThrowStmtOp,
                McNode(
                    NewOp("java.lang.IllegalStateException", listOf(McType.STRING)),
                    McNode(CstStringOp("Polymorphic subclass of \"${splitPackageClass(packet.className).second}\" has instance of illegal type"))
                )
            )
        } else {
            null
        }
        for (childName in polymorphicChildren[packet.className]!!.asReversed()) {
            val childMessage = getMessageVariantInfo(childName)
            if (childMessage.handler != null && (clientbound || this.protocolId <= (childMessage.handlerProtocol ?: throw CompileException("@Handler protocol is required for serverbound packets ($childName)")))) {
                val condition = McNode(
                    InstanceOfOp(packet.toMcType(), childMessage.toMcType()),
                    McNode(LoadVariableOp(varId, packet.toMcType()))
                )
                var ifBlock = generateHandlerInner(
                    McNode(
                        CastOp(packet.toMcType(), childMessage.toMcType()),
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
                    McNode(
                        IfElseStmtOp,
                        condition,
                        ifBlock,
                        McNode(StmtListOp, ifElseChain)
                    )
                } else {
                    McNode(
                        IfStmtOp,
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

private fun ProtocolCompiler.generateHandlerInner(packetNode: McNode, nextProtocolId: Int?, packet: MessageVariantInfo, handlerName: String, clientbound: Boolean): McNode {
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

    val possibleTypes = handlerFunc.possibleReturnTypes ?: if (handlerFunc.returnType.hasName(CommonClassNames.LIST)) {
        listOf(handlerFunc.returnType.componentType())
    } else {
        listOf(handlerFunc.returnType)
    }

    for (type in possibleTypes) {
        val info = type.classInfoOrNull as? MessageVariantInfo
        val isValid = info?.let {
            getPacketDirection(nextProtocolId, it.className) == PacketDirection.fromClientbound(clientbound)
        } ?: false
        if (!isValid) {
            throw CompileException("Returning invalid type $type from packet @Handler")
        }
    }

    val elementVar = if (handlerFunc.returnType.hasName(CommonClassNames.LIST)) VariableId.create() else varId

    val handlerBody = if (handlerFunc.possibleReturnTypes.isNullOrEmpty()) {
        generateExplicitSenderServerRegistries(handlerFunc.returnType.deepComponentType().messageVariantInfo, nextProtocolId, elementVar, clientbound)
    } else {
        var ifElseChain = McNode(
            ThrowStmtOp,
            McNode(
                NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                McNode(CstStringOp("Packet @Handler \"${splitPackageClass(packet.className).second}.$handlerName\" returned invalid type. Expected one of $possibleTypes"))
            )
        )
        for (returnType in handlerFunc.possibleReturnTypes.asReversed()) {
            val instanceVarId = VariableId.create()
            ifElseChain = McNode(
                IfElseStmtOp,
                McNode(
                    InstanceOfOp(handlerFunc.returnType.deepComponentType(), returnType),
                    McNode(LoadVariableOp(elementVar, handlerFunc.returnType.deepComponentType()))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(instanceVarId, returnType, true),
                        McNode(
                            CastOp(handlerFunc.returnType.deepComponentType(), returnType),
                            McNode(LoadVariableOp(elementVar, handlerFunc.returnType.deepComponentType()))
                        )
                    ),
                    generateExplicitSenderServerRegistries(returnType.messageVariantInfo, nextProtocolId, instanceVarId, clientbound)
                ),
                McNode(StmtListOp, ifElseChain)
            )
        }
        ifElseChain
    }

    if (handlerFunc.returnType.hasName(CommonClassNames.LIST)) {
        val indexVar = VariableId.create()
        val sizeVar = VariableId.create()
        nodes += McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0)))
        nodes += McNode(StoreVariableStmtOp(sizeVar, McType.INT, true),
            McNode(FunctionCallOp(CommonClassNames.LIST, "size", listOf(handlerFunc.returnType), McType.INT, false, isStatic = false),
                McNode(LoadVariableOp(varId, handlerFunc.returnType))
            )
        )
        nodes += McNode(
            WhileStmtOp,
            McNode(
                BinaryExpressionOp("<", McType.INT, McType.INT),
                McNode(LoadVariableOp(indexVar, McType.INT)),
                McNode(LoadVariableOp(sizeVar, McType.INT))
            ),
            McNode(StmtListOp,
                McNode(StoreVariableStmtOp(elementVar, handlerFunc.returnType.deepComponentType(), true),
                    McNode(FunctionCallOp(CommonClassNames.LIST, "get", listOf(handlerFunc.returnType, McType.INT), handlerFunc.returnType.deepComponentType(), false, isStatic = false),
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

private fun ProtocolCompiler.generateWrite(group: MessageInfo?, packet: MessageVariantInfo, protocolsSubset: List<ProtocolEntry>, clientbound: Boolean): McNode {
    val resultBufVar = VariableId.create()
    val nodes = mutableListOf<McNode>()
    nodes += McNode(
        StoreVariableStmtOp(resultBufVar, McType.BYTE_BUF, true),
        McNode(FunctionCallOp(CommonClassNames.UNPOOLED, "buffer", listOf(), McType.BYTE_BUF, true))
    )
    if (clientbound) {
        currentProtocolId = protocols[0].id
    }
    // write the packet id
    val packetId = readCsv<PacketType>(
        FileLocations.dataDir
            .resolve(protocolNamesById[currentProtocolId]!!)
            .resolve(if (clientbound) "spackets.csv" else "cpackets.csv")
    ).firstOrNull { it.clazz == getVariant(group, currentProtocolId, packet).className }?.id
        ?: throw CompileException("Packet ${splitPackageClass(getVariant(group, currentProtocolId, packet).className).second} not present in protocol ${protocolNamesById[currentProtocolId]!!}")
    nodes += IoOps.writeType(resultBufVar, Types.VAR_INT, McNode(CstIntOp(packetId)))
    nodes += if (group != null) {
        generateWriteGraph(group, protocolVariable(protocolsSubset.last().id), resultBufVar, null)
    } else {
        generateWriteGraph(packet, protocolVariable(protocolsSubset.last().id), resultBufVar, null)
    }
    if (clientbound) {
        currentProtocolId = protocolId
    }
    nodes += McNode(
        PopStmtOp,
        McNode(
            FunctionCallOp(CommonClassNames.LIST, "add", listOf(McType.BYTE_BUF.listOf(), McType.BYTE_BUF), McType.VOID, true, isStatic = false),
            McNode(LoadVariableOp(VariableId.immediate("outBufs"), McType.BYTE_BUF.listOf())),
            McNode(LoadVariableOp(resultBufVar, McType.BYTE_BUF))
        )
    )

    return McNode(StmtListOp, nodes)
}

// endregion

// region UTILITIES

private fun protocolVariable(id: Int): VariableId {
    return VariableId.immediate("protocol_$id")
}

private fun getVariant(group: MessageInfo?, protocol: Int, dflt: MessageVariantInfo): MessageVariantInfo {
    if (group == null) {
        return dflt
    }
    return group.getVariant(protocol) ?: throw CompileException("No variant of packet \"${group.className}\" found for protocol $protocol")
}

internal enum class PacketDirection {
    SERVERBOUND, CLIENTBOUND, INVALID;

    companion object {
        fun fromClientbound(clientbound: Boolean): PacketDirection {
            return if (clientbound) {
                CLIENTBOUND
            } else {
                SERVERBOUND
            }
        }
    }
}

internal fun getPacketDirection(protocolId: Int, className: String): PacketDirection {
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

// endregion
