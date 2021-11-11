package net.earthcomputer.multiconnect.compiler

import net.earthcomputer.multiconnect.ap.Introduce
import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

fun checkMessages() {
    val usedByProtocol = mutableMapOf<Int, Set<String>>()
    for (protocol in protocols) {
        val used = mutableSetOf<String>()
        fun processUsed(message: String) {
            if (used.add(message)) {
                val info = getClassInfo(message) as MessageInfo
                if ((info.minVersion != null && protocol.id < info.minVersion) || (info.maxVersion != null && protocol.id > info.maxVersion)) {
                    throw IllegalStateException("Protocol $protocol uses message variant $message, but is not supported by that message variant")
                }
                for (field in info.fields) {
                    val type = field.type.realType.deepComponentType()
                    if (type is McType.DeclaredType && getClassInfoOrNull(type.name) is MessageInfo) {
                        processUsed(type.name)
                    }
                }
            }
        }
        for (packet in readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve("cpackets.csv"))) {
            if (getClassInfo(packet.clazz) !is MessageInfo) {
                throw IllegalStateException("Packet class ${packet.clazz} is not a message")
            }
            processUsed(packet.clazz)
        }
        for (packet in readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve("spackets.csv"))) {
            if (getClassInfo(packet.clazz) !is MessageInfo) {
                throw IllegalStateException("Packet class ${packet.clazz} is not a message")
            }
            processUsed(packet.clazz)
        }
        usedByProtocol[protocol.id] = used
    }

    for (group in groups.values) {
        var lastMaxVersion = (getClassInfo(group[0]) as MessageInfo).maxVersion
        for (message in group) {
            val info = getClassInfo(message) as MessageInfo
            if (lastMaxVersion == null || info.minVersion == null || info.minVersion <= lastMaxVersion) {
                throw IllegalStateException("Message $message has a lower min version than the max version of the previous message in the variant group")
            }
            val index = protocols.binarySearch { it.id.compareTo(info.minVersion) }
            if (index < 0) {
                throw IllegalStateException("Message $message has a min version that is not in the protocol list")
            }
            if (info.maxVersion != null && protocols.binarySearch { it.id.compareTo(info.maxVersion) } < 0) {
                throw IllegalStateException("Message $message has a max version that is not in the protocol list")
            }
            assert(index != protocols.lastIndex)
            if (protocols[index + 1].id != lastMaxVersion) {
                throw IllegalStateException("Message $message has a gap in the protocol versions before it")
            }
            lastMaxVersion = info.maxVersion

            for (i in index downTo 0) {
                val protocol = protocols[i].id
                // check that this message is used somewhere in this protocol
                if (usedByProtocol[protocol]?.contains(message) != true) {
                    throw IllegalStateException("Message variant $message is not used in protocol $protocol which it supports")
                }
                if (protocol == info.maxVersion) {
                    break
                }
            }
        }
    }
}

class ProtocolCompiler(private val protocolName: String, private val protocolId: Int) {
    private val dataDir = FileLocations.dataDir.resolve(protocolName)
    private val latestDataDir = FileLocations.dataDir.resolve(protocols[0].name)

    fun compile() {
        val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
        val emitter = Emitter(className, TreeSet(), TreeMap(), StringBuilder())

        emitPacketTranslators(emitter, "translateSPacket", dataDir.resolve("spackets.csv"), true)
        emitPacketTranslators(emitter, "translateCPacket", latestDataDir.resolve("cpackets.csv"), false)

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
                    McNode(LoadVariableOp("buf", McType.BYTE_BUF))
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
                    add(McNode(LoadVariableOp("buf", McType.BYTE_BUF), mutableListOf()))
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
        val messageInfo = getClassInfo(packetClass) as MessageInfo
        val function = emitter.addMember(functionName) ?: return true
        function.append("private static ").appendClassName(BYTE_BUF).append(" ").append(functionName).append("(")
            .appendClassName(BYTE_BUF).append(" buf) {").indent().appendNewLine()
        generatePacketGraph(messageInfo, clientbound).optimize().emit(emitter, Precedence.COMMA)
        function.dedent().appendNewLine().append("}")
        return true
    }

    private fun Registries.hasChanged(wireType: Types): Boolean {
        val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${this.name.lowercase()}.csv"))
        val newEntries = readCsv<RegistryEntry>(latestDataDir.resolve("${this.name.lowercase()}.csv"))
        return if (wireType.isIntegral) {
            oldEntries.any { newEntries.byId(it.id)?.name != it.name }
        } else {
            oldEntries.any { it.name != it.oldName }
        }
    }

    private fun generatePacketGraph(messageInfo: MessageInfo, clientbound: Boolean): McNode {
        // figure out what's going to handle it
        val group = groups[messageInfo.variantOf ?: messageInfo.className]!!
        val packetChain = if (clientbound)
            group.asSequence().dropWhile { it != messageInfo.className }
        else
            (group.asReversed().asSequence().takeWhile { it != messageInfo.className } + sequenceOf(messageInfo.className))
        var handler: McFunction? = null
        val partialHandlers = mutableMapOf<String, List<McFunction>>()
        for (variant in packetChain) {
            val info = getClassInfo(variant) as MessageInfo
            partialHandlers[info.className] = info.partialHandlers.map { info.findFunction(it) }
            val handlerAllowed = info.handlerProtocol == null || if (clientbound) protocolId >= info.handlerProtocol else protocolId <= info.handlerProtocol
            if (handlerAllowed) {
                handler = info.handler?.let { info.findFunction(it) }
                if (handler != null) {
                    break
                }
            }
        }

        // figure out what data we need for the handler(s)
        val dataFlowNodes = if (handler == null) {
            // check that the resulting packet exists in the target version
            val packets = readCsv<RegistryEntry>(if (clientbound) {
                latestDataDir.resolve("spackets.csv")
            } else {
                dataDir.resolve("cpackets.csv")
            })
            if (packets.byName(latestPacket.className) == null) {
                throw CompileException("Packet ${messageInfo.className} translates into ${latestPacket.className} which cannot be received on the target protocol")
            }
            latestPacket.fields.associateTo(mutableMapOf()) { it.name to DataFlowNode(it.name, false) }
        } else {
            handler.parameters.filterIsInstance<ArgumentParameter>().associateTo(mutableMapOf()) { it.name to DataFlowNode(it.name, true) }
        }

        val dataFlowLayers = mutableListOf(dataFlowNodes)
        for ((reversedIndex, packet) in packetChain.asReversed().withIndex()) {
            val packetInfo = getClassInfo(packet) as MessageInfo
            val dataFlowLayer = dataFlowLayers.last()

            // check partial handlers
            val partHandlers = packetInfo.partialHandlers.map(packetInfo::findFunction)
            for (partHandler in partHandlers) {
                for (fieldName in partHandler.argumentDependencies()) {
                    dataFlowLayer.addNode(fieldName, DataFlowNode(fieldName, true))
                }
            }
            partialHandlers += partHandlers

            // check for any implicit dependencies
            for ((fieldName, dfNode) in dataFlowLayer) {
                val field = packetInfo.findField(fieldName).type
                for (dependencyField in sequenceOf(
                    field.onlyIf?.let(packetInfo::findFunction)?.argumentDependencies(),
                    (field.lengthInfo?.computeInfo as? LengthInfo.ComputeInfo.Compute)?.value?.let(packetInfo::findFunction)?.argumentDependencies(),
                    field.polymorphicBy?.let { sequenceOf(it) }
                ).filterNotNull().flatMap { it }) {
                    dataFlowLayer.addNode(dependencyField, DataFlowNode(dependencyField, true))
                    dfNode.sameLayerDependencies += dependencyField
                }
            }

            if (packetInfo.polymorphic != null && packetInfo.polymorphicParent == null) {
                val firstField = packetInfo.fields.first().name
                dataFlowLayer.addNode(firstField, DataFlowNode(firstField, true))
            }

            if (reversedIndex == packetChain.lastIndex) break // no previous packet

            val prevPacketInfo = getClassInfo(packetChain[packetChain.lastIndex - reversedIndex - 1]) as MessageInfo
            val prevDataFlowLayer = mutableMapOf<String, DataFlowNode>()
            for ((fieldName, dfNode) in dataFlowLayer) {
                val field = packetInfo.findField(fieldName).type
                val introduce = field.introduce.firstOrNull { it.direction != (if (clientbound) Introduce.Direction.FROM_NEWER else Introduce.Direction.FROM_OLDER) }
                if (introduce != null) {
                    if (introduce is IntroduceInfo.Compute) {
                        val introduceFunc = packetInfo.findFunction(introduce.value)
                        for (dependency in introduceFunc.argumentDependencies()) {
                            if (prevPacketInfo.findFieldOrNull(dependency) == null) {
                                throw CompileException("@Introduce field $fieldName depends on field $dependency which doesn't exist")
                            }
                            prevDataFlowLayer.addNode(dependency, DataFlowNode(dependency, true))
                            dfNode.dependencies += dependency
                        }
                    }
                } else {
                    val oldField = prevPacketInfo.findFieldOrNull(fieldName)?.type
                        ?: throw CompileException("Field $fieldName has nowhere to take its value from, and is not marked with @Introduce")
                    if (!field.wireType.canCast(oldField.wireType)) {
                        throw CompileException("Cannot cast ${oldField.wireType} to ${field.wireType} for field $fieldName. Use @Introduce for custom behavior.")
                    }
                    if (field.wireType == Types.MESSAGE) {
                        // TODO: message translation
                        if (field.realType.deepComponentType() != oldField.realType.deepComponentType()) {
                            throw CompileException("Cannot cast ${oldField.realType} to ${field.realType} for field $fieldName. Use @Introduce for custom behavior.")
                        }
                    } else {
                        val typeName = (field.realType.deepComponentType() as? McType.DeclaredType)?.name
                        val isEnum = typeName?.let { getClassInfoOrNull(it) } is EnumInfo
                        val oldTypeName = (oldField.realType.deepComponentType() as? McType.DeclaredType)?.name
                        val oldIsEnum = oldTypeName?.let { getClassInfoOrNull(it) } is EnumInfo
                        if (isEnum != oldIsEnum || (isEnum && typeName != oldTypeName)) {
                            throw CompileException("Cannot cast ${oldField.realType} to ${field.realType} for field $fieldName. Use @Introduce for custom behavior.")
                        }
                    }
                    if (field.realType.dimensions != oldField.realType.dimensions) {
                        throw CompileException("Cannot cast ${oldField.realType} to ${field.realType} for field $fieldName. Use @Introduce for custom behavior.")
                    }
                    prevDataFlowLayer.addNode(fieldName, DataFlowNode(fieldName, dfNode.interpreted || field.wireType != oldField.wireType))
                    dfNode.dependencies += fieldName
                }
            }
        }


    }

    private class MessageGraphSettings(val partialHandlers: Map<String, List<McFunction>>) {
        companion object {
            val DEFAULT = MessageGraphSettings(emptyMap())
        }
    }

    private fun generateMessageGraph(group: List<String>, fromProtocol: Int, toProtocol: Int, settings: MessageGraphSettings = MessageGraphSettings.DEFAULT): McNode {

    }

    private fun McFunction.argumentDependencies(): Sequence<String> {
        return parameters.asSequence().filterIsInstance<ArgumentParameter>().map { it.name }
    }
}

private class DataFlowNode(
    val name: String,
    val interpreted: Boolean
) {
    val dependencies = mutableSetOf<String>()
    val sameLayerDependencies = mutableSetOf<String>()
}

private fun <T> MutableMap<T, DataFlowNode>.addNode(key: T, value: DataFlowNode) {
    merge(key, value) { a, b ->
        if (a.interpreted) {
            a.dependencies += b.dependencies
            a.sameLayerDependencies += b.sameLayerDependencies
            a
        } else {
            b.dependencies += a.dependencies
            b.sameLayerDependencies += a.sameLayerDependencies
            b
        }
    }
}

fun generateFunctionCallGraph(function: McFunction, variableNames: Map<String, VariableId>): McNode {
    val loadParams = function.parameters.mapTo(mutableListOf()) { param ->
        when (param) {
            is ArgumentParameter -> McNode(LoadVariableOp(variableNames[param.name]!!, param.paramType))
            is DefaultConstructedParameter -> generateDefaultConstructGraph(param.paramType)
            is SuppliedDefaultConstructedParameter -> McNode(
                LambdaOp(param.paramType, param.suppliedType, emptyList(), emptyList()),
                generateDefaultConstructGraph(param.suppliedType)
            )
            is FilledParameter -> generateFilledConstructGraph(param.paramType, param.fromRegistry)
        }
    }
    return McNode(
        FunctionCallOp(function.owner, function.name, function.parameters.map { it.paramType }, function.returnType, true),
        loadParams
    )
}

private fun generateDefaultConstructGraph(type: McType): McNode {

}

private fun generateDefaultConstructGraph(type: FieldType): McNode {

}

private fun generateFilledConstructGraph(type: McType, fromRegistry: FilledFromRegistry?): McNode {

}
