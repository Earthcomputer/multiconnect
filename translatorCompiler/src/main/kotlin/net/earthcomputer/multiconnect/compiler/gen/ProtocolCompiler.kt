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
import net.earthcomputer.multiconnect.compiler.RegistryEntry
import net.earthcomputer.multiconnect.compiler.byName
import net.earthcomputer.multiconnect.compiler.getClassInfo
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.opto.optimize
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.readCsv
import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

class ProtocolCompiler(internal val protocolName: String, internal val protocolId: Int) {
    internal val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
    internal val dataDir = FileLocations.dataDir.resolve(protocolName)
    internal val latestDataDir = FileLocations.dataDir.resolve(protocols[0].name)
    internal val cacheMembers = mutableMapOf<String, (Emitter) -> Unit>()
    internal var useLatestRegistries = false

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
            useLatestRegistries = true
        }
        nodes += McNode(StoreVariableStmtOp(VariableId.immediate("protocol_${protocolsSubset.first().id}"), messageVariantInfo.toMcType(), true),
            generateMessageReadGraph(getPacket(protocolsSubset.first().id))
        )
        if (!clientbound) {
            useLatestRegistries = false

            nodes += if (group != null) {
                fixRegistries(group, VariableId.immediate("protocol_${protocolsSubset.first().id}"), false)
            } else {
                fixRegistries(messageVariantInfo, VariableId.immediate("protocol_${protocolsSubset.first().id}"), false)
            }
        }

        for (index in protocolsSubset.indices.drop(1)) {
            nodes += McNode(StoreVariableStmtOp(VariableId.immediate("protocol_${protocolsSubset[index].id}"), group?.toMcType() ?: messageVariantInfo.toMcType(), true),
                if (group != null) {
                    translate(group, protocolsSubset[index - 1].id, protocolsSubset[index].id, VariableId.immediate("protocol_${protocolsSubset[index - 1].id}"))
                } else {
                    translate(messageVariantInfo, protocolsSubset[index - 1].id, protocolsSubset[index].id, VariableId.immediate("protocol_${protocolsSubset[index - 1].id}"))
                }
            )
        }

        if (clientbound) {
            nodes += if (group != null) {
                fixRegistries(group, VariableId.immediate("protocol_${protocolsSubset.last().id}"), true)
            } else {
                fixRegistries(messageVariantInfo, VariableId.immediate("protocol_${protocolsSubset.last().id}"), true)
            }
        }

        // TODO: this isn't necessarily how we want to handle packets
        val resultBufVar = VariableId.create()
        nodes += McNode(StoreVariableStmtOp(resultBufVar, McType.BYTE_BUF, true),
            McNode(FunctionCallOp(UNPOOLED, "buffer", listOf(), McType.BYTE_BUF, true))
        )
        nodes += if (group != null) {
            generateWriteGraph(group, VariableId.immediate("protocol_${protocolsSubset.last().id}"), resultBufVar)
        } else {
            generateWriteGraph(messageVariantInfo, VariableId.immediate("protocol_${protocolsSubset.last().id}"), resultBufVar)
        }
        nodes += McNode(PopStmtOp,
            McNode(FunctionCallOp(LIST, "add", listOf(McType.BYTE_BUF.listOf(), McType.BYTE_BUF), McType.VOID, true, isStatic = false),
                McNode(LoadVariableOp(VariableId.immediate("outBufs"), McType.BYTE_BUF.listOf())),
                McNode(LoadVariableOp(resultBufVar, McType.BYTE_BUF))
            )
        )

        return McNode(StmtListOp, nodes)
    }

    internal fun Registries.getRawId(value: String): Either<Int, McNode>? {
        val registryDir = if (useLatestRegistries) latestDataDir else dataDir
        val entries = readCsv<RegistryEntry>(registryDir.resolve("${this.name.lowercase()}.csv"))
        val entry = entries.byName(value)

        // load id dynamically if necessary
        if (entry == null && useLatestRegistries && value.startsWith("multiconnect:")) {
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
