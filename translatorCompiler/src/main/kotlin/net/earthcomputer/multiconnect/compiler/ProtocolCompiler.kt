package net.earthcomputer.multiconnect.compiler

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BITSET
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CommonClassNames.IDENTIFIER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LONG_ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LONG_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.METHOD_HANDLE
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL_INT
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL_LONG
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_INTRINSICS
import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet
import kotlin.math.min

fun checkMessages() {
    val usedByProtocol = mutableMapOf<Int, Set<String>>()
    for (protocol in protocols) {
        val used = mutableSetOf<String>()
        fun processUsed(message: String) {
            if (used.add(message)) {
                val info = getMessageVariantInfo(message)
                if ((info.minVersion != null && protocol.id < info.minVersion) || (info.maxVersion != null && protocol.id > info.maxVersion)) {
                    throw IllegalStateException("Protocol $protocol uses message variant $message, but is not supported by that message variant")
                }
                for (field in info.fields) {
                    val type = field.type.realType.deepComponentType()
                    if (type is McType.DeclaredType) {
                        when (getClassInfoOrNull(type.name)) {
                            is MessageInfo -> groups[type.name]?.let { group ->
                                val index = group.binarySearch {
                                    (getMessageVariantInfo(it).minVersion ?: -1).compareTo(protocol.id)
                                }
                                processUsed(group[if (index < 0) min(-index-1, group.lastIndex) else index])
                            }
                            is MessageVariantInfo -> processUsed(type.name)
                            else -> {}
                        }
                    }
                }
            }
        }
        for (packet in readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve("cpackets.csv"))) {
            if (getClassInfoOrNull(packet.clazz) !is MessageVariantInfo) {
                throw IllegalStateException("Packet class ${packet.clazz} is not a message variant")
            }
            processUsed(packet.clazz)
        }
        for (packet in readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve("spackets.csv"))) {
            if (getClassInfoOrNull(packet.clazz) !is MessageVariantInfo) {
                throw IllegalStateException("Packet class ${packet.clazz} is not a message variant")
            }
            processUsed(packet.clazz)
        }
        usedByProtocol[protocol.id] = used
    }

    for (group in groups.values) {
        var lastMaxVersion: Int? = null
        for ((messageIndex, message) in group.withIndex()) {
            val info = getMessageVariantInfo(message)
            if (messageIndex != 0 && (lastMaxVersion == null || info.minVersion == null || info.minVersion <= lastMaxVersion)) {
                throw IllegalStateException("Message $message has a lower min version than the max version of the previous message in the variant group")
            }
            val index = if (messageIndex == 0) protocols.lastIndex else protocols.binarySearch { info.minVersion!!.compareTo(it.id) }
            if (index < 0) {
                throw IllegalStateException("Message $message has a min version that is not in the protocol list")
            }
            if (info.maxVersion != null && protocols.binarySearch { info.maxVersion.compareTo(it.id) } < 0) {
                throw IllegalStateException("Message $message has a max version that is not in the protocol list")
            }
            if (messageIndex != 0) {
                assert(index != protocols.lastIndex)
                if (protocols[index + 1].id != lastMaxVersion) {
                    throw IllegalStateException("Message $message has a gap in the protocol versions before it")
                }
            }
            lastMaxVersion = info.maxVersion

            if (info.minVersion != null || info.maxVersion != null) {
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
}

class ProtocolCompiler(private val protocolName: String, private val protocolId: Int) {
    private val className = "net.earthcomputer.multiconnect.generated.Protocol_${protocolName.replace('.', '_')}"
    private val dataDir = FileLocations.dataDir.resolve(protocolName)
    private val latestDataDir = FileLocations.dataDir.resolve(protocols[0].name)
    private val cacheMembers = mutableMapOf<String, (Emitter) -> Unit>()

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

    private fun Registries.hasChanged(wireType: Types): Boolean {
        val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${this.name.lowercase()}.csv"))
        val newEntries = readCsv<RegistryEntry>(latestDataDir.resolve("${this.name.lowercase()}.csv"))
        return if (wireType.isIntegral) {
            oldEntries.any { newEntries.byId(it.id)?.name != it.name }
        } else {
            oldEntries.any { it.name != it.oldName }
        }
    }

    private fun Registries.getRawId(value: String): Int? {
        val entries = readCsv<RegistryEntry>(dataDir.resolve("${this.name.lowercase()}.csv"))
        return entries.byName(value)?.id
    }

    private fun generatePacketGraph(messageVariantInfo: MessageVariantInfo, clientbound: Boolean): McNode {
        val group = groups[messageVariantInfo.variantOf ?: messageVariantInfo.className]!!
        val packetChain = if (clientbound)
            group.asSequence().dropWhile { it != messageVariantInfo.className }.toList()
        else
            (group.asReversed().asSequence().takeWhile { it != messageVariantInfo.className } + sequenceOf(messageVariantInfo.className)).toList()

        return generateMessageReadGraph(getMessageVariantInfo(packetChain.first()))
    }

    private fun generatePolymorphicInstantiationGraph(
        message: MessageVariantInfo,
        type: FieldType,
        loadTypeField: McNode,
        paramResolver: (String, McType) -> McNode,
        postConstruct: ((MessageVariantInfo, VariableId) -> McNode)?
    ): McNode {
        message.polymorphic!!
        val messageType = McType.DeclaredType(message.className)

        fun construct(childMessage: MessageVariantInfo): McNode {
            val childType = McType.DeclaredType(childMessage.className)
            return if (postConstruct == null) {
                McNode(ImplicitCastOp(childType, messageType),
                    McNode(NewOp(childMessage.className, listOf()))
                )
            } else {
                val varId = VariableId.create()
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(varId, childType, true), McNode(NewOp(childMessage.className, listOf()))),
                    postConstruct(childMessage, varId),
                    McNode(ReturnStmtOp(messageType),
                        McNode(ImplicitCastOp(childType, messageType),
                            McNode(LoadVariableOp(varId, childType))
                        )
                    )
                )
            }
        }

        val children = polymorphicChildren[message.className]?.map { getMessageVariantInfo(it) } ?: emptyList()
        val useSwitchExpression = type.realType != McType.BOOLEAN
                && type.realType != McType.FLOAT
                && type.realType != McType.DOUBLE
                && children.any { it.polymorphic is Polymorphic.Constant<*> }
        val nonSwitchChildren: MutableList<MessageVariantInfo?> = if (useSwitchExpression) {
            children.filterTo(mutableListOf()) { it.polymorphic !is Polymorphic.Constant<*> }
        } else {
            children.toMutableList()
        }
        if (nonSwitchChildren.none { it?.polymorphic is Polymorphic.Otherwise }) {
            nonSwitchChildren += null
        }
        val defaultBranch = if (postConstruct == null && nonSwitchChildren.singleOrNull()?.polymorphic is Polymorphic.Otherwise) {
            construct(nonSwitchChildren.single()!!)
        }
        else {
            McNode(StmtListOp,
                nonSwitchChildren.sortedBy {
                    when (it?.polymorphic) {
                        is Polymorphic.Constant<*> -> 0
                        is Polymorphic.Condition -> 1
                        is Polymorphic.Otherwise -> 2
                        null -> 2
                    }
                }.mapTo(mutableListOf()) { childMessage ->
                    when (val polymorphic = childMessage?.polymorphic) {
                        is Polymorphic.Constant<*> -> McNode(IfStmtOp,
                            polymorphic.value.map {
                                McNode(BinaryExpressionOp("==", type.realType, type.realType),
                                    loadTypeField,
                                    McNode(createCstOp(it))
                                )
                            }.reduce { left, right ->
                                McNode(BinaryExpressionOp("||", McType.BOOLEAN, McType.BOOLEAN), left, right)
                            },
                            McNode(StmtListOp, McNode(ReturnStmtOp(type.realType), construct(childMessage)))
                        )
                        is Polymorphic.Condition -> McNode(IfStmtOp,
                            generateFunctionCallGraph(message.findFunction(polymorphic.value), loadTypeField, paramResolver = paramResolver),
                            McNode(StmtListOp, McNode(ReturnStmtOp(type.realType), construct(childMessage)))
                        )
                        is Polymorphic.Otherwise -> McNode(ReturnStmtOp(type.realType), construct(childMessage))
                        null -> McNode(ThrowStmtOp,
                            McNode(NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                                McNode(createCstOp("Could not select polymorphic child of \"${splitPackageClass(message.className).second}\""))
                            )
                        )
                    }
                }
            )
        }
        if (!useSwitchExpression) {
            return defaultBranch
        }

        fun <T: Comparable<T>> makeCases(): McNode {
            val (results, cases) = children.zip(
                children
                    .asSequence()
                    .map { it.polymorphic }
                    .filterIsInstance<Polymorphic.Constant<T>>()
                    .map { group ->
                        when {
                            type.realType.hasName(IDENTIFIER) ->
                                group.value.map { (it as String).normalizeIdentifier() }
                            type.realType.isIntegral && type.registry != null ->
                                group.value.filter { it !is String || type.registry.getRawId(it) != null }.map { case ->
                                    (case as? String)?.let {
                                        type.registry.getRawId(it)!!
                                    } ?: case
                                }
                            type.realType.classInfoOrNull is EnumInfo ->
                                group.value.map { SwitchOp.EnumCase(it as String) }
                            else -> group.value
                        }
                    }
                    .filter { it.isNotEmpty() }
                    .map { lst ->
                        @Suppress("UNCHECKED_CAST")
                        SwitchOp.GroupCase((lst as List<T>).toSortedSet())
                    }
                    .toList()
            ).sortedBy { (_, case) -> case }.unzip()
            return McNode(SwitchOp(
                cases.toSortedSet(),
                true,
                if (type.realType.hasName(IDENTIFIER)) { McType.STRING } else { type.realType },
                messageType
            ),
                (listOf(if (type.realType.hasName(IDENTIFIER)) {
                    McNode(FunctionCallOp(IDENTIFIER, "toString", listOf(type.realType), McType.STRING, false, isStatic = false),
                        loadTypeField
                    )
                } else {
                    loadTypeField
                }) + results.map {
                    McNode(ImplicitCastOp(McType.DeclaredType(it.className), messageType),
                        McNode(NewOp(it.className, listOf()))
                    )
                } + listOf(defaultBranch)).toMutableList()
            )
        }
        return makeCases<String>() // use String as placeholder type argument
    }

    private fun generateMessageReadGraph(message: MessageVariantInfo, polymorphicBy: McNode? = null): McNode {
        val parentInfo = message.polymorphicParent?.let(::getMessageVariantInfo)
        return if ((message.tailrec && message.fields.lastOrNull()?.type?.realType?.hasName(message.className) == true)
            || (parentInfo != null && parentInfo.tailrec && message.fields.lastOrNull()?.type?.realType?.hasName(parentInfo.className) == true)
        ) {
            val messageType = McType.DeclaredType(message.className)
            val nodes = mutableListOf<McNode>()
            val returnVar = VariableId.create()
            val lastMessageVar = VariableId.create()
            val currentMessageVar = VariableId.create()
            if (message.fields.last().type.realType.hasName(message.className)) {
                // the onlyIf variant of recursion
                val tailRecursionContinueVar = VariableId.create()
                nodes += McNode(StoreVariableStmtOp(tailRecursionContinueVar, McType.BOOLEAN, true), McNode(CstBoolOp(true)))
                nodes += McNode(StoreVariableStmtOp(returnVar, messageType, true), generateMessageReadGraphInner(message, polymorphicBy, tailRecursionContinueVar, null))
                nodes += McNode(StoreVariableStmtOp(currentMessageVar, messageType, true), McNode(LoadVariableOp(returnVar, messageType)))
                nodes += McNode(WhileStmtOp,
                    McNode(LoadVariableOp(tailRecursionContinueVar, McType.BOOLEAN)),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(lastMessageVar, messageType, true), McNode(LoadVariableOp(currentMessageVar, messageType))),
                        McNode(StoreVariableStmtOp(currentMessageVar, messageType, false), generateMessageReadGraphInner(message, null, tailRecursionContinueVar, null)),
                        McNode(StoreFieldStmtOp(messageType, message.fields.last().name, messageType),
                            McNode(LoadVariableOp(lastMessageVar, messageType)),
                            McNode(LoadVariableOp(currentMessageVar, messageType))
                        )
                    )
                )
            } else {
                // the polymorphic variant of recursion
                val methodHandleType = McType.DeclaredType(METHOD_HANDLE)
                val lastMethodHandle = VariableId.create()
                val currentMethodHandle = VariableId.create()
                nodes += McNode(DeclareVariableOnlyStmtOp(currentMethodHandle, methodHandleType))
                nodes += McNode(StoreVariableStmtOp(returnVar, messageType, true), generateMessageReadGraphInner(message, polymorphicBy, null, currentMethodHandle))
                nodes += McNode(StoreVariableStmtOp(currentMessageVar, messageType, true), McNode(LoadVariableOp(returnVar, messageType)))
                nodes += McNode(WhileStmtOp,
                    McNode(BinaryExpressionOp("!=", methodHandleType, methodHandleType),
                        McNode(LoadVariableOp(currentMethodHandle, methodHandleType)),
                        McNode(CstNullOp(methodHandleType))
                    ),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(lastMethodHandle, methodHandleType, true), McNode(LoadVariableOp(currentMethodHandle, methodHandleType))),
                        McNode(StoreVariableStmtOp(lastMessageVar, messageType, true), McNode(LoadVariableOp(currentMessageVar, messageType))),
                        McNode(StoreVariableStmtOp(currentMessageVar, messageType, false), generateMessageReadGraphInner(message, null, null, currentMethodHandle)),
                        McNode(FunctionCallOp(METHOD_HANDLE, "invoke", listOf(methodHandleType, messageType, messageType), McType.VOID, true, isStatic = false, throwsException = true),
                            McNode(LoadVariableOp(lastMethodHandle, methodHandleType)),
                            McNode(LoadVariableOp(lastMessageVar, messageType)),
                            McNode(LoadVariableOp(currentMessageVar, messageType))
                        )
                    )
                )
            }
            nodes += McNode(ReturnStmtOp(messageType), McNode(LoadVariableOp(returnVar, messageType)))
            McNode(StmtListOp, nodes)
        } else {
            generateMessageReadGraphInner(message, polymorphicBy, null, null)
        }
    }

    private fun generateMessageReadGraphInner(
        message: MessageVariantInfo,
        polymorphicBy: McNode?,
        tailRecursionContinueVar: VariableId?,
        recursivePolymorphicMethodHandle: VariableId?,
    ): McNode {
        val type = McType.DeclaredType(message.className)
        val varId = VariableId.create()
        val vars = mutableMapOf<String, VariableId>()
        val nodes = mutableListOf<McNode>()

        for ((index, field) in message.fields.withIndex()) {
            val fieldVarId = VariableId.create()
            vars[field.name] = fieldVarId
            nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true),
                if (index == 0 && polymorphicBy != null) {
                    polymorphicBy
                } else {
                    generateFieldReadGraph(
                        message,
                        field,
                        index == message.fields.lastIndex && (
                            (message.tailrec && field.type.realType.hasName(message.className))
                            || (
                                message.polymorphicParent != null
                                && getMessageVariantInfo(message.polymorphicParent).tailrec
                                && field.type.realType.hasName(message.polymorphicParent)
                            )
                        ),
                        tailRecursionContinueVar,
                        vars
                    )
                }
            )
        }

        nodes += if (message.polymorphic != null && message.polymorphicParent == null) {
            val typeField = message.fields.first()
            val fieldVarId = vars[typeField.name]!!
            McNode(StoreVariableStmtOp(varId, type, true),
                generatePolymorphicInstantiationGraph(
                    message,
                    typeField.type,
                    McNode(LoadVariableOp(fieldVarId, typeField.type.realType)),
                    { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) }
                ) { childMessage, childVarId ->
                    val childNodes = mutableListOf<McNode>()
                    for ((index, field) in childMessage.fields.withIndex()) {
                        val childType = McType.DeclaredType(childMessage.className)
                        val childFieldVarId = VariableId.create()
                        vars[field.name] = childFieldVarId
                        childNodes += McNode(StoreVariableStmtOp(childFieldVarId, field.type.realType, true),
                            generateFieldReadGraph(
                                message,
                                field,
                                index == childMessage.fields.lastIndex && (
                                    (message.tailrec && field.type.realType.hasName(message.className))
                                    || (childMessage.tailrec && field.type.realType.hasName(childMessage.className))
                                ),
                                null,
                                vars
                            )
                        )
                        childNodes += McNode(StoreFieldStmtOp(childType, field.name, field.type.realType),
                            McNode(LoadVariableOp(childVarId, childType)),
                            McNode(LoadVariableOp(childFieldVarId, field.type.realType))
                        )
                        if (recursivePolymorphicMethodHandle != null && index == childMessage.fields.lastIndex) {
                            childNodes += McNode(StoreVariableStmtOp(recursivePolymorphicMethodHandle, McType.DeclaredType(METHOD_HANDLE), false),
                                if (field.type.realType.hasName(message.className)) {
                                    val fieldName = "MH_${childMessage.className.replace('.', '_').uppercase()}_TAIL_RECURSE"
                                    cacheMembers[fieldName] = { emitter ->
                                        emitter.append("private static final ").appendClassName(METHOD_HANDLE).append(" ").append(fieldName)
                                            .append(" = ").appendClassName(PACKET_INTRINSICS).append(".findSetterHandle(")
                                            .appendClassName(childMessage.className).append(".class, ")
                                        McNode(createCstOp(field.name)).emit(emitter, Precedence.COMMA)
                                        emitter.append(", ").appendClassName(message.className).append(".class);")
                                    }
                                    McNode(LoadFieldOp(
                                        McType.DeclaredType(className),
                                        fieldName,
                                        McType.DeclaredType(METHOD_HANDLE),
                                        isStatic = true
                                    ))
                                } else {
                                    McNode(CstNullOp(McType.DeclaredType(METHOD_HANDLE)))
                                }
                            )
                        }
                    }
                    McNode(StmtListOp, childNodes)
                }
            )
        } else {
            McNode(StoreVariableStmtOp(varId, type, true), McNode(NewOp(message.className, listOf())))
        }

        for (field in message.fields) {
            val fieldVarId = vars[field.name]!!
            nodes += McNode(StoreFieldStmtOp(type, field.name, field.type.realType),
                McNode(LoadVariableOp(varId, type)),
                McNode(LoadVariableOp(fieldVarId, field.type.realType))
            )
        }
        nodes += McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))
        return McNode(StmtListOp, nodes)
    }

    private fun generateFieldReadGraph(
        message: MessageVariantInfo,
        field: McField,
        isTailRecursive: Boolean,
        tailRecursionContinueVar: VariableId?,
        vars: Map<String, VariableId>
    ): McNode {
        val readNode = if (isTailRecursive) {
            McNode(CstNullOp(field.type.realType))
        } else {
            generateTypeReadGraph(message, field.type.realType, field.type.wireType, field.type.lengthInfo) { name, type -> McNode(LoadVariableOp(vars[name]!!, type)) }
        }

        if (field.type.onlyIf != null) {
            val varId = VariableId.create()
            val elseBlock = mutableListOf<McNode>()
            elseBlock += McNode(StoreVariableStmtOp(varId, field.type.realType, false),
                generateDefaultConstructGraph(message, field.type, isTailRecursive) { name, type -> McNode(LoadVariableOp(vars[name]!!, type)) }
            )
            if (isTailRecursive && tailRecursionContinueVar != null) {
                elseBlock += McNode(StoreVariableStmtOp(tailRecursionContinueVar, McType.BOOLEAN, false), McNode(CstBoolOp(false)))
            }
            return McNode(StmtListOp,
                McNode(DeclareVariableOnlyStmtOp(varId, field.type.realType)),
                McNode(IfElseStmtOp,
                    generateFunctionCallGraph(message.findFunction(field.type.onlyIf)) { name, type -> McNode(LoadVariableOp(vars[name]!!, type)) },
                    McNode(StmtListOp, McNode(StoreVariableStmtOp(varId, field.type.realType, false), readNode)),
                    McNode(StmtListOp, elseBlock)
                ),
                McNode(ReturnStmtOp(field.type.realType), McNode(LoadVariableOp(varId, field.type.realType)))
            )
        }

        return readNode
    }

    private fun generateTypeReadGraph(contextMessage: MessageVariantInfo, realType: McType, wireType: Types, lengthInfo: LengthInfo?, paramResolver: (String, McType) -> McNode): McNode {
        // deal with arrays, lists, etc
        if (realType.hasComponentType) {
            val varId = VariableId.create()
            val lengthVar = VariableId.create()
            val lengthNode = when (lengthInfo?.computeInfo) {
                is LengthInfo.ComputeInfo.Constant -> McNode(createCstOp(lengthInfo.computeInfo.value))
                is LengthInfo.ComputeInfo.Compute -> generateFunctionCallGraph(contextMessage.findFunction(lengthInfo.computeInfo.value), paramResolver = paramResolver)
                is LengthInfo.ComputeInfo.RemainingBytes -> McNode(FunctionCallOp(BYTE_BUF, "readableBytes", listOf(McType.BYTE_BUF), McType.INT, true, isStatic = false),
                    McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF))
                )
                null -> McType.INT.cast(IoOps.readType(VariableId.immediate("buf"), lengthInfo?.type ?: Types.VAR_INT))
            }
            val arrayType = when (realType) {
                is McType.ArrayType -> realType
                is McType.DeclaredType -> when (realType.name) {
                    BITSET -> McType.LONG.arrayOf()
                    else -> null
                }
                else -> null
            }
            val loopVar = VariableId.create()
            val innerReader = generateTypeReadGraph(contextMessage, realType.componentType(), wireType, null, paramResolver)
            val listReader = McNode(StmtListOp,
                McNode(StoreVariableStmtOp(lengthVar, McType.INT, true), lengthNode),
                McNode(StoreVariableStmtOp(varId, realType, true), when {
                    arrayType != null -> McNode(NewArrayOp(arrayType.elementType),
                        McNode(LoadVariableOp(lengthVar, McType.INT))
                    )
                    realType is McType.DeclaredType -> {
                        val concreteType = when (realType.name) {
                            INT_LIST -> INT_ARRAY_LIST
                            LONG_LIST -> LONG_ARRAY_LIST
                            LIST -> ARRAY_LIST
                            else -> realType.name
                        }
                        McNode(ImplicitCastOp(McType.DeclaredType(concreteType), realType),
                            McNode(NewOp(concreteType, listOf(McType.INT)),
                                McNode(LoadVariableOp(lengthVar, McType.INT))
                            )
                        )
                    }
                    else -> throw IllegalStateException("Invalid type with length")
                }),
                McNode(StoreVariableStmtOp(loopVar, McType.INT, true), McNode(createCstOp(0))),
                McNode(WhileStmtOp,
                    McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                        McNode(LoadVariableOp(loopVar, McType.INT)),
                        McNode(LoadVariableOp(lengthVar, McType.INT))
                    ),
                    McNode(StmtListOp,
                        if (arrayType != null) {
                            McNode(StoreArrayStmtOp(arrayType.elementType),
                                McNode(LoadVariableOp(varId, arrayType)),
                                McNode(LoadVariableOp(loopVar, McType.INT)),
                                innerReader
                            )
                        } else {
                            realType as McType.DeclaredType
                            McNode(PopStmtOp,
                                McNode(FunctionCallOp(realType.name, "add", listOf(realType, realType.componentType()), McType.VOID, true, isStatic = false),
                                    McNode(LoadVariableOp(varId, realType)),
                                    innerReader
                                )
                            )
                        },
                        McNode(StoreVariableStmtOp(loopVar, McType.INT, false, operator = "+="),
                            McNode(createCstOp(1))
                        )
                    )
                ),
                McNode(ReturnStmtOp(realType), McNode(LoadVariableOp(varId, realType)))
            )
            return if (realType.hasName(BITSET)) {
                McNode(FunctionCallOp(BITSET, "valueOf", listOf(McType.LONG.arrayOf()), McType.DeclaredType(BITSET), false),
                    listReader
                )
            } else {
                listReader
            }
        }

        // message types have special handling
        if (wireType == Types.MESSAGE) {
            return when (val classInfo = realType.classInfo) {
                is MessageVariantInfo -> generateMessageReadGraph(classInfo)
                is MessageInfo -> {
                    val group = groups[(realType as McType.DeclaredType).name]!!
                    val index = group.binarySearch { (getMessageVariantInfo(it).minVersion ?: -1).compareTo(protocolId) }
                    generateMessageReadGraph(getMessageVariantInfo(group[index]))
                }
                else -> throw IllegalStateException("Invalid real type for message type")
            }
        }

        // enums are done by ordinal
        if (realType.classInfoOrNull is EnumInfo) {
            val enumName = (realType as McType.DeclaredType).name
            val fieldName = "ENUM_VALUES_${enumName.uppercase().replace('.', '_')}"
            cacheMembers[fieldName] = { emitter ->
                emitter.append("private static final ").appendClassName(enumName).append("[] ").append(fieldName)
                    .append(" = ").appendClassName(enumName).append(".values();")
            }
            return McNode(LoadArrayOp(realType),
                McNode(LoadFieldOp(McType.DeclaredType(className), fieldName, realType.arrayOf(), isStatic = true)),
                McType.INT.cast(IoOps.readType(VariableId.immediate("buf"), wireType))
            )
        }

        // direct read, easy, but cast primitives if necessary
        return if (realType is McType.PrimitiveType) {
            realType.cast(IoOps.readType(VariableId.immediate("buf"), wireType))
        } else {
            IoOps.readType(VariableId.immediate("buf"), wireType)
        }
    }

    private fun generateFunctionCallGraph(function: McFunction, vararg positionalArguments: McNode, paramResolver: (String, McType) -> McNode): McNode {
        val loadParams = function.parameters.mapTo(mutableListOf()) { param ->
            when (param) {
                is ArgumentParameter -> paramResolver(param.name, param.paramType)
                is DefaultConstructedParameter -> generateDefaultConstructGraph(param.paramType)
                is SuppliedDefaultConstructedParameter -> McNode(
                    LambdaOp(param.paramType, param.suppliedType, emptyList(), emptyList()),
                    generateDefaultConstructGraph(param.suppliedType)
                )
                is FilledParameter -> generateFilledConstructGraph(param.paramType, param.fromRegistry)
            }
        }
        val params = positionalArguments.toMutableList()
        params += loadParams
        return McNode(
            FunctionCallOp(function.owner, function.name, function.positionalParameters + function.parameters.map { it.paramType }, function.returnType, true),
            params
        )
    }

    private fun generateDefaultConstructGraph(classInfo: MessageVariantInfo): McNode {
        val type = McType.DeclaredType(classInfo.className)

        val varId = VariableId.create()
        val vars = mutableMapOf<String, VariableId>()
        val nodes = mutableListOf<McNode>()
        nodes += McNode(StoreVariableStmtOp(varId, type, true), McNode(NewOp(classInfo.className, listOf())))

        var parentInfo: MessageVariantInfo? = null

        if (classInfo.polymorphic != null) {
            if (classInfo.polymorphicParent == null) {
                return if (classInfo.defaultConstruct is DefaultConstruct.SubType) {
                    val subclassInfo = classInfo.defaultConstruct.value.messageVariantInfo
                    McNode(ImplicitCastOp(classInfo.defaultConstruct.value, type),
                        generateDefaultConstructGraph(subclassInfo)
                    )
                } else {
                    type.defaultValue()
                }
            } else {
                parentInfo = getMessageVariantInfo(classInfo.polymorphicParent)
                for ((index, field) in parentInfo.fields.withIndex()) {
                    val fieldVarId = VariableId.create()
                    vars[field.name] = fieldVarId
                    val valueNode = if (index == 0 && classInfo.polymorphic is Polymorphic.Constant<*>) {
                        generateConstantGraph(field.type.realType, classInfo.polymorphic.value.first(), field.type.registry)
                    } else {
                        generateDefaultConstructGraph(parentInfo, field.type, false) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) }
                    }
                    nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true), valueNode)
                    nodes += McNode(StoreFieldStmtOp(type, field.name, field.type.realType),
                        McNode(LoadVariableOp(varId, type)),
                        McNode(LoadVariableOp(fieldVarId, field.type.realType))
                    )
                }
            }
        }

        for ((index, field) in classInfo.fields.withIndex()) {
            var isTailRecursive = false
            if (index == classInfo.fields.lastIndex) {
                if (classInfo.tailrec && field.type.realType.hasName(classInfo.className)) {
                    isTailRecursive = true
                } else if (parentInfo?.tailrec == true && field.type.realType.hasName(parentInfo.className)) {
                    isTailRecursive = true
                }
            }

            val fieldVarId = VariableId.create()
            vars[field.name] = fieldVarId
            val valueNode = generateDefaultConstructGraph(classInfo, field.type, isTailRecursive) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) }
            nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true), valueNode)
            nodes += McNode(StoreFieldStmtOp(type, field.name, field.type.realType),
                McNode(LoadVariableOp(varId, type)),
                McNode(LoadVariableOp(fieldVarId, field.type.realType))
            )
        }

        nodes += McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))

        return McNode(StmtListOp, nodes)
    }

    private fun generateDefaultConstructGraph(type: McType): McNode {
        return when (val classInfo = type.classInfoOrNull) {
            is MessageVariantInfo -> generateDefaultConstructGraph(classInfo)
            is MessageInfo -> {
                val name = (type as McType.DeclaredType).name
                val group = groups[name]!!
                val index = group.binarySearch { (getMessageVariantInfo(it).minVersion ?: -1).compareTo(protocolId) }
                McNode(ImplicitCastOp(McType.DeclaredType(group[index]), McType.DeclaredType(name)),
                    generateDefaultConstructGraph(getMessageVariantInfo(group[index]))
                )
            }
            is EnumInfo -> McNode(LoadFieldOp(type, classInfo.values.first(), type, isStatic = true))
            else -> when((type as? McType.DeclaredType)?.name) {
                OPTIONAL, OPTIONAL_INT, OPTIONAL_LONG -> {
                    McNode(FunctionCallOp(type.name, "empty", listOf(), type, false))
                }
                LIST -> {
                    McNode(ImplicitCastOp(McType.DeclaredType(ARRAY_LIST), type), McNode(NewOp(ARRAY_LIST, listOf())))
                }
                INT_LIST -> {
                    McNode(ImplicitCastOp(McType.DeclaredType(INT_ARRAY_LIST), type), McNode(NewOp(INT_ARRAY_LIST, listOf())))
                }
                LONG_ARRAY_LIST -> {
                    McNode(ImplicitCastOp(McType.DeclaredType(LONG_ARRAY_LIST), type), McNode(NewOp(LONG_ARRAY_LIST, listOf())))
                }
                BITSET -> McNode(NewOp(type.name, listOf()))
                else -> {
                    if (type is McType.ArrayType) {
                        McNode(NewArrayOp(type.elementType), McNode(createCstOp(0)))
                    } else {
                        type.defaultValue()
                    }
                }
            }
        }
    }

    private fun generateDefaultConstructGraph(
        message: MessageVariantInfo,
        type: FieldType,
        isTailRecursive: Boolean,
        paramResolver: (String, McType) -> McNode
    ): McNode {
        return when (type.defaultConstructInfo) {
            is DefaultConstruct.SubType -> {
                if (isTailRecursive) {
                    type.realType.defaultValue()
                } else {
                    McNode(ImplicitCastOp(type.defaultConstructInfo.value, type.realType), generateDefaultConstructGraph(type.defaultConstructInfo.value))
                }
            }
            is DefaultConstruct.Constant<*> -> generateConstantGraph(type.realType, type.defaultConstructInfo.value, type.registry)
            is DefaultConstruct.Compute -> generateFunctionCallGraph(message.findFunction(type.defaultConstructInfo.value), paramResolver = paramResolver)
            null -> {
                if (isTailRecursive) {
                    type.realType.defaultValue()
                } else {
                    generateDefaultConstructGraph(type.realType)
                }
            }
        }
    }

    private fun generateConstantGraph(targetType: McType, value: Any, registry: Registries? = null): McNode {
        return if (targetType.isIntegral && registry != null && value is String) {
            val rawId = registry.getRawId(value) ?: throw CompileException("Unknown value \"$value\" in registry $registry")
            McNode(createCstOp(rawId))
        } else if (targetType.hasName(IDENTIFIER)) {
            val (namespace, name) = (value as String).normalizeIdentifier().split(':', limit = 2)
            McNode(NewOp(IDENTIFIER, listOf(McType.STRING, McType.STRING)),
                McNode(createCstOp(namespace)),
                McNode(createCstOp(name))
            )
        } else {
            val enumInfo = targetType.classInfoOrNull as? EnumInfo
            if (enumInfo != null) {
                McNode(LoadFieldOp(targetType, value as String, targetType, isStatic = true))
            } else {
                McNode(createCstOp(value))
            }
        }
    }

    private fun generateFilledConstructGraph(type: McType, fromRegistry: FilledFromRegistry?): McNode {
        // TODO: filled construct
        return type.defaultValue()
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
