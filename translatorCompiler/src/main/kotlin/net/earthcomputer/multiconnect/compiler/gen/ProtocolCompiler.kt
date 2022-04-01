package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.Emitter
import net.earthcomputer.multiconnect.compiler.FileLocations
import net.earthcomputer.multiconnect.compiler.IoOps
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.PacketType
import net.earthcomputer.multiconnect.compiler.RegistryEntry
import net.earthcomputer.multiconnect.compiler.byId
import net.earthcomputer.multiconnect.compiler.byName
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.groups
import net.earthcomputer.multiconnect.compiler.isIntegral
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
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

    fun compile() {
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", latestDataDir.resolve("cpackets.csv"), false)

        for ((memberName, cacheMember) in cacheMembers) {
            emitter.addMember(memberName)?.let(cacheMember)
        }

        val outputFile = FileLocations.outputDir.resolve(className.replace('.', '/') + ".java")
        val parentFile = outputFile.parentFile
        if (!parentFile.exists()) parentFile.mkdirs()
        outputFile.writeText(emitter.createClassText())
    }

    private fun emitPacketTranslators(emitter: Emitter, functionName: String, packetsFile: File, clientbound: Boolean) {
        val function = emitter.addMember(functionName) ?: return
        function.append("public static ").appendClassName(BYTE_BUF).append(" ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf) {").indent().appendNewLine()
        val packets = TreeMap<Int, McNode>()
        for ((id, clazz) in readCsv<PacketType>(packetsFile)) {
            val packetFunctionName = "translate${clazz.substringAfterLast('.')}"
            if (emitPacketTranslator(emitter, packetFunctionName, clazz, clientbound)) {
                packets[id] = McNode(
                    FunctionCallOp(
                        emitter.currentClass,
                        packetFunctionName,
                        listOf(McType.BYTE_BUF),
                        McType.BYTE_BUF,
                        true
                    ),
                    McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF))
                )
            }
        }
        val stmt = McNode(
            ReturnStmtOp(McType.BYTE_BUF),
            McNode(
                SwitchOp(packets.keys as SortedSet<Int>, true, McType.INT, McType.BYTE_BUF),
                mutableListOf<McNode>().apply {
                    add(IoOps.readType(VariableId.immediate("buf"), Types.VAR_INT))
                    addAll(packets.values)
                    add(McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF), mutableListOf()))
                }
            )
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
        function.append("private static ").appendClassName(BYTE_BUF).append(" ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf) {").indent().appendNewLine()
        generatePacketGraph(messageVariantInfo, clientbound).optimize().emit(function, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
        return true
    }

    private fun generatePacketGraph(messageVariantInfo: MessageVariantInfo, clientbound: Boolean): McNode {
        val group = groups[messageVariantInfo.variantOf ?: messageVariantInfo.className]!!
        val packetChain = if (clientbound)
            group.asSequence().dropWhile { it != messageVariantInfo.className }.toList()
        else
            (group.asReversed().asSequence().takeWhile { it != messageVariantInfo.className } + sequenceOf(messageVariantInfo.className)).toList()

        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(VariableId.immediate("result"), McType.DeclaredType(messageVariantInfo.className), true),
                generateMessageReadGraph(getMessageVariantInfo(packetChain.first()))
            ),
            McNode(ReturnStmtOp(McType.BYTE_BUF), McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF)))
        )
    }

    internal fun Registries.hasChanged(wireType: Types): Boolean {
        val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${this.name.lowercase()}.csv"))
        val newEntries = readCsv<RegistryEntry>(latestDataDir.resolve("${this.name.lowercase()}.csv"))
        return if (wireType.isIntegral) {
            oldEntries.any { newEntries.byId(it.id)?.name != it.name }
        } else {
            oldEntries.any { it.name != it.oldName }
        }
    }

    internal fun Registries.getRawId(value: String): Int? {
        val entries = readCsv<RegistryEntry>(dataDir.resolve("${this.name.lowercase()}.csv"))
        return entries.byName(value)?.id
    }
}
