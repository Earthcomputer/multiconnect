package net.earthcomputer.multiconnect.compiler

import net.earthcomputer.multiconnect.ap.Types

abstract class MessageRep(val messageVariantInfo: MessageVariantInfo, val parentRep: MessageRep?) {
    abstract fun loadField(name: String): McNode
    abstract fun loadMessageField(name: String, fieldType: MessageVariantInfo): MessageRep
    abstract fun storeField(name: String, value: McNode): McNode
    abstract fun cast(subType: MessageVariantInfo): MessageRep
}

abstract class DelegatingMessageRep(messageVariantInfo: MessageVariantInfo, parentRep: MessageRep?, val source: MessageRep) : MessageRep(messageVariantInfo, parentRep) {
}

class BoxedMessageRep(
    messageVariantInfo: MessageVariantInfo,
    parentRep: MessageRep?,
    private val instance: McNode
) : MessageRep(messageVariantInfo, parentRep) {
    override fun loadField(name: String): McNode {
        val variableType = McType.DeclaredType(messageVariantInfo.className)
        return McNode(
            LoadFieldOp(variableType, name, messageVariantInfo.findField(name).type.realType),
            instance
        )
    }

    override fun loadMessageField(name: String, fieldType: MessageVariantInfo): MessageRep {
        return BoxedMessageRep(fieldType, null, loadField(name))
    }

    override fun storeField(name: String, value: McNode): McNode {
        val variableType = McType.DeclaredType(messageVariantInfo.className)
        return McNode(
            StoreFieldStmtOp(variableType, name, messageVariantInfo.findField(name).type.realType),
            instance,
            value
        )
    }

    override fun cast(subType: MessageVariantInfo): MessageRep {
        val castedInstance = McNode(
            CastOp(McType.DeclaredType(messageVariantInfo.className), McType.DeclaredType(subType.className)),
            instance
        )
        return BoxedMessageRep(subType, this, castedInstance)
    }
}

class ReadBufferMessageRep(
    messageVariantInfo: MessageVariantInfo,
    parentRep: MessageRep?,
    private val bufferVariable: VariableId,
) : MessageRep(messageVariantInfo, parentRep) {
    class CopyInfo {
        var currentBlock: CopyBlock? = null
        var constantSize: Int = 0
        var variableSizeVariable: VariableId? = null
        var depth = 0

        fun consumeConstantSize(bufferVariable: VariableId): McNode {
            val size = constantSize
            constantSize = 0
            return McNode(
                PopStmtOp,
                McNode(
                    FunctionCallOp(CommonClassNames.BYTE_BUF, "skipBytes", listOf(McType.BYTE_BUF, McType.INT), McType.VOID, true, isStatic = false),
                    McNode(LoadVariableOp(bufferVariable, McType.BYTE_BUF)),
                    McNode(CstIntOp(size))
                )
            )
        }
    }

    class CopyBlock(val names: List<String>, val variable: VariableId)

    private val interpretedFields = mutableMapOf<String, VariableId>()
    private val loadedMessageFields = mutableMapOf<String, ReadBufferMessageRep>()
    private val copyBlocks = mutableMapOf<String, CopyBlock>()

    override fun loadField(name: String): McNode {
        val variable = interpretedFields.computeIfAbsent(name) { VariableId.create() }
        return McNode(LoadVariableOp(variable, messageVariantInfo.findField(name).type.realType))
    }

    override fun loadMessageField(name: String, fieldType: MessageVariantInfo): ReadBufferMessageRep {
        return loadedMessageFields.computeIfAbsent(name) { ReadBufferMessageRep(fieldType, null, bufferVariable) }
    }

    override fun storeField(name: String, value: McNode): McNode {
        throw UnsupportedOperationException("Cannot store to a ReadBufferMessageRep")
    }

    fun readAsCopyBlock(names: List<String>, variable: VariableId) {
        val copyBlock = CopyBlock(names, variable)
        for (name in names) {
            copyBlocks[name] = copyBlock
        }
    }

    fun getInitializer(copyInfo: CopyInfo = CopyInfo()): McNode {
        val fieldVariables = interpretedFields.toMutableMap()
        if (parentRep != null) {
            fieldVariables += (parentRep as ReadBufferMessageRep).interpretedFields
        }

        val statements = mutableListOf<McNode>()
        for (field in messageVariantInfo.fields) {
            var startConstantSize = copyInfo.constantSize
            copyInfo.depth++
            val innerStatements: MutableList<McNode> = if (field.type.wireType == Types.MESSAGE) {
                val messageRep = loadedMessageFields[field.name]
                 if (messageRep != null) {
                    mutableListOf(messageRep.getInitializer(copyInfo))
                } else {
                    val subInfo = getClassInfo((field.type.realType.deepComponentType() as McType.DeclaredType).name) as MessageVariantInfo
                    if (copyBlocks[field.name] != null) {
                        mutableListOf(loadMessageField(field.name, subInfo).readToCopyBlock(copyInfo))
                    } else {
                        mutableListOf(loadMessageField(field.name, subInfo).skipOver(copyInfo))
                    }
                }
            } else {
                if (field.name in interpretedFields) {
                    mutableListOf(IoOps.readType(bufferVariable, field.type.wireType))
                } else {
                    val skipLength = field.type.wireType.skipLength
                    if (skipLength != 0) {
                        copyInfo.constantSize += skipLength
                        mutableListOf()
                    } else {
                        if (copyInfo.constantSize != 0) {
                            statements += copyInfo.consumeConstantSize(bufferVariable)
                        }
                        mutableListOf(IoOps.skipOver(bufferVariable, field.type.wireType))
                    }
                }
            }
            copyInfo.depth--

            var statement = McNode(StmtListOp, innerStatements)
            var shouldApplyConstantSizeAtStart = false

            val containerTypes = generateSequence(field.type.realType) { it.componentTypeOrNull() }.toList()
            for (i in containerTypes.size - 2 downTo 0) {
                if (containerTypes[i].isOptional) {
                    shouldApplyConstantSizeAtStart = true
                    statement = McNode(
                        IfStmtOp,
                        IoOps.readType(bufferVariable, Types.BOOLEAN),
                        statement
                    )
                } else {
                    when (val computeInfo = field.type.lengthInfo?.computeInfo) {
                        is LengthInfo.ComputeInfo.Constant -> {
                            if (copyInfo.constantSize != startConstantSize) {
                                copyInfo.constantSize += (copyInfo.constantSize - startConstantSize) * (computeInfo.value - 1)
                            }
                        }
                    }
                }
            }

            if (field.type.onlyIf != null) {
                shouldApplyConstantSizeAtStart = true
                statement = McNode(
                    IfStmtOp,
                    generateFunctionCallGraph(messageVariantInfo.findFunction(field.type.onlyIf), fieldVariables),
                    statement
                )
            }
        }
        return McNode(StmtListOp, statements)
    }

    fun readToCopyBlock(copyInfo: CopyInfo): McNode {

    }

    fun skipOver(copyInfo: CopyInfo): McNode {

    }

    override fun cast(subType: MessageVariantInfo): MessageRep {
        return ReadBufferMessageRep(subType, this, bufferVariable)
    }
}
