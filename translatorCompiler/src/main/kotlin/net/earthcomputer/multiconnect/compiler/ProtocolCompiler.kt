package net.earthcomputer.multiconnect.compiler

import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

object ProtocolCompiler {
    fun compile(protocolName: String) {
        val dataDir = dataDir.resolve(protocolName)
        val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"))
        emitPacketTranslators(emitter, "translateCPacket", dataDir.resolve("cpackets.csv"))

        val outputFile = outputDir.resolve(className.replace('.', '/') + ".java")
        val parentFile = outputFile.parentFile
        if (!parentFile.exists()) parentFile.mkdirs()
        outputFile.writeText(emitter.createClassText())
    }

    private fun emitPacketTranslators(emitter: Emitter, functionName: String, packetsFile: File) {
        val function = emitter.addMember(functionName) ?: return
        function.append("public static ").appendClassName(BYTE_BUF).append(" ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf) {").indent().appendNewLine()
        val packets = TreeMap<Int, McNode>()
        val byteBufType = McType.DeclaredType(BYTE_BUF)
        for ((id, clazz) in readCsv<PacketType>(packetsFile)) {
            val packetFunctionName = "translate${clazz.substringAfterLast('.')}"
            if (emitPacketTranslator(emitter, packetFunctionName, clazz)) {
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

    private fun emitPacketTranslator(emitter: Emitter, functionName: String, packetClass: String): Boolean {
        return false
    }
}
