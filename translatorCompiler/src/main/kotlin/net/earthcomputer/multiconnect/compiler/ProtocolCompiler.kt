package net.earthcomputer.multiconnect.compiler

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

class ProtocolCompiler(private val protocolName: String, private val protocolId: Int) {
    private val dataDir = FileLocations.dataDir.resolve(protocolName)
    fun compile() {
        val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", dataDir.resolve("cpackets.csv"), false)

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
        val byteBufType = McType.DeclaredType(BYTE_BUF)
        for ((id, clazz) in readCsv<PacketType>(packetsFile)) {
            val packetFunctionName = "translate${clazz.substringAfterLast('.')}"
            if (emitPacketTranslator(emitter, packetFunctionName, clazz, clientbound)) {
                packets[id] = McNode(
                    FunctionCallOp(emitter.currentClass, packetFunctionName, listOf(byteBufType), byteBufType, true),
                    mutableListOf(McNode(LoadVariableOp("buf", byteBufType), mutableListOf()))
                )
            }
        }
        val stmt = McNode(ReturnStmtOp(byteBufType), mutableListOf(
            McNode(SwitchOp(packets.keys as SortedSet<Int>, true, McType.INT, byteBufType),
                mutableListOf(McNode(IoOps.readVarIntOp(), mutableListOf(McNode(LoadVariableOp("buf", byteBufType), mutableListOf())))).apply {
                    addAll(packets.values)
                    add(McNode(LoadVariableOp("buf", byteBufType), mutableListOf()))
                }
            )
        )).optimize()
        stmt.emit(function, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
    }

    private fun emitPacketTranslator(emitter: Emitter, functionName: String, packetClass: String, clientbound: Boolean): Boolean {
        val oldPacket = getClassInfo(packetClass) as? MessageInfo ?: throw CompileException("Packet $packetClass must be a message type")
        if (oldPacket.translateFromNewer != null && oldPacket.translateFromNewer.value <= protocolId) {
            throw CompileException("Packet $packetClass is registered for protocol $protocolId but claims to translate from newer ${oldPacket.translateFromNewer.value}")
        }
        if (oldPacket.translateFromOlder != null && oldPacket.translateFromOlder.value >= protocolId) {
            throw CompileException("Packet $packetClass is registered for protocol $protocolId but claims to translate from older ${oldPacket.translateFromOlder.value}")
        }
        if (!oldPacket.needsTranslating(clientbound)) {
            return false
        }
        val function = emitter.addMember(functionName) ?: return true
        function.append("private static ").appendClassName(BYTE_BUF).append(" ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf) {").indent().appendNewLine()
        function.append("return buf;") // TODO something smarter ofc
        function.dedent().appendNewLine().append("}")
        return true
    }

    private fun MessageInfo.needsTranslating(
        clientbound: Boolean,
        checkedClasses: MutableSet<String> = mutableSetOf()
    ): Boolean {
        if (!checkedClasses.add(this.className)) {
            return false
        }

        if (clientbound) {
            if (this.className in translateToNewer) {
                return true
            }
        } else {
            if (this.translateFromNewer != null) {
                return true
            }
        }

        if (handler != null) {
            return true
        }

        for (field in fields) {
            if (field.type.datafixType != null) {
                return true
            }
            if (field.type.registry != null) {
                if (field.type.registry.hasChanged(field.type.wireType)) {
                    return true
                }
            }
            if (field.type.wireType == Types.MESSAGE) {
                val messageName = (field.type.realType.deepComponentType() as McType.DeclaredType).name
                if ((getClassInfo(messageName) as MessageInfo).needsTranslating(clientbound, checkedClasses)) {
                    return true
                }
            }
        }

        val children = polymorphicChildren[this.className]
        if (children != null && children.any { (getClassInfo(it) as MessageInfo).needsTranslating(clientbound, checkedClasses) }) {
            return true
        }

        return false
    }

    private fun Registries.hasChanged(wireType: Types): Boolean {
        val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${this.name.lowercase()}.csv"))
        val newEntries = readCsv<RegistryEntry>(FileLocations.dataDir.resolve("${protocols[0].name}/${this.name.lowercase()}.csv"))
        return if (wireType.isIntegral) {
            oldEntries.any { newEntries.byId(it.id)?.name != it.name }
        } else {
            oldEntries.any { it.name != it.oldName }
        }
    }
}
