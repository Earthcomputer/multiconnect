package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.EnumInfo
import net.earthcomputer.multiconnect.compiler.IoOps
import net.earthcomputer.multiconnect.compiler.LengthInfo
import net.earthcomputer.multiconnect.compiler.McField
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.classInfo
import net.earthcomputer.multiconnect.compiler.classInfoOrNull
import net.earthcomputer.multiconnect.compiler.componentType
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.hasComponentType
import net.earthcomputer.multiconnect.compiler.hasName
import net.earthcomputer.multiconnect.compiler.isOptional
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CstBoolOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstNullOp
import net.earthcomputer.multiconnect.compiler.node.DeclareVariableOnlyStmtOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.ImplicitCastOp
import net.earthcomputer.multiconnect.compiler.node.LoadArrayOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.NewArrayOp
import net.earthcomputer.multiconnect.compiler.node.NewOp
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreArrayStmtOp
import net.earthcomputer.multiconnect.compiler.node.StoreFieldStmtOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.WhileStmtOp
import net.earthcomputer.multiconnect.compiler.node.createCstNode

internal fun ProtocolCompiler.generateMessageReadGraph(message: MessageVariantInfo, outerParamResolver: ParamResolver?, polymorphicBy: McNode? = null): McNode {
    return if (message.tailrec) {
        val messageType = message.toMcType()
        val nodes = mutableListOf<McNode>()
        val returnVar = VariableId.create()
        val lastMessageVar = VariableId.create()
        val currentMessageVar = VariableId.create()
        if (message.fields.last().type.realType.hasName(message.className)) {
            // the onlyIf variant of recursion
            val tailRecursionContinueVar = VariableId.create()
            nodes += McNode(StoreVariableStmtOp(tailRecursionContinueVar, McType.BOOLEAN, true), McNode(CstBoolOp(true)))
            nodes += McNode(StoreVariableStmtOp(returnVar, messageType, true), generateMessageReadGraphInner(message, polymorphicBy, tailRecursionContinueVar, null, outerParamResolver))
            nodes += McNode(StoreVariableStmtOp(currentMessageVar, messageType, true), McNode(LoadVariableOp(returnVar, messageType)))
            nodes += McNode(
                WhileStmtOp,
                McNode(LoadVariableOp(tailRecursionContinueVar, McType.BOOLEAN)),
                McNode(
                    StmtListOp,
                    McNode(StoreVariableStmtOp(lastMessageVar, messageType, true), McNode(LoadVariableOp(currentMessageVar, messageType))),
                    McNode(StoreVariableStmtOp(currentMessageVar, messageType, false), generateMessageReadGraphInner(message, null, tailRecursionContinueVar, null, outerParamResolver)),
                    McNode(
                        StoreFieldStmtOp(messageType, message.fields.last().name, messageType),
                        McNode(LoadVariableOp(lastMessageVar, messageType)),
                        McNode(LoadVariableOp(currentMessageVar, messageType))
                    )
                )
            )
        } else {
            // the polymorphic variant of recursion
            val methodHandleType = McType.DeclaredType(CommonClassNames.METHOD_HANDLE)
            val lastMethodHandle = VariableId.create()
            val currentMethodHandle = VariableId.create()
            nodes += McNode(DeclareVariableOnlyStmtOp(currentMethodHandle, methodHandleType))
            nodes += McNode(StoreVariableStmtOp(returnVar, messageType, true), generateMessageReadGraphInner(message, polymorphicBy, null, currentMethodHandle, outerParamResolver))
            nodes += McNode(StoreVariableStmtOp(currentMessageVar, messageType, true), McNode(LoadVariableOp(returnVar, messageType)))
            nodes += McNode(
                WhileStmtOp,
                McNode(
                    BinaryExpressionOp("!=", methodHandleType, methodHandleType),
                    McNode(LoadVariableOp(currentMethodHandle, methodHandleType)),
                    McNode(CstNullOp(methodHandleType))
                ),
                McNode(
                    StmtListOp,
                    McNode(StoreVariableStmtOp(lastMethodHandle, methodHandleType, true), McNode(LoadVariableOp(currentMethodHandle, methodHandleType))),
                    McNode(StoreVariableStmtOp(lastMessageVar, messageType, true), McNode(LoadVariableOp(currentMessageVar, messageType))),
                    McNode(StoreVariableStmtOp(currentMessageVar, messageType, false), generateMessageReadGraphInner(message, null, null, currentMethodHandle, outerParamResolver)),
                    McNode(
                        PopStmtOp,
                        McNode(
                            FunctionCallOp(CommonClassNames.METHOD_HANDLE, "invoke", listOf(methodHandleType, messageType, messageType), McType.VOID, true, isStatic = false, throwsException = true),
                            McNode(LoadVariableOp(lastMethodHandle, methodHandleType)),
                            McNode(LoadVariableOp(lastMessageVar, messageType)),
                            McNode(LoadVariableOp(currentMessageVar, messageType))
                        )
                    )
                )
            )
        }
        nodes += McNode(ReturnStmtOp(messageType), McNode(LoadVariableOp(returnVar, messageType)))
        McNode(StmtListOp, nodes)
    } else {
        generateMessageReadGraphInner(message, polymorphicBy, null, null, outerParamResolver)
    }
}

private fun ProtocolCompiler.generateMessageReadGraphInner(
    message: MessageVariantInfo,
    polymorphicBy: McNode?,
    tailRecursionContinueVar: VariableId?,
    recursivePolymorphicMethodHandle: VariableId?,
    outerParamResolver: ParamResolver?,
): McNode {
    val type = message.toMcType()
    val varId = VariableId.create()
    val vars = mutableMapOf<String, VariableId>()
    val nodes = mutableListOf<McNode>()

    if (message.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(message.polymorphicParent)
        for (field in parentInfo.fields) {
            val fieldVarId = VariableId.create()
            vars[field.name] = fieldVarId
            nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true),
                generateFieldReadGraph(parentInfo, field, false, null, combineParamResolvers(outerParamResolver) { name, type1 ->
                    McNode(LoadVariableOp(vars[name]!!, type1))
                })
            )
        }
    }

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
                    combineParamResolvers(outerParamResolver) { name, type1 ->
                        McNode(LoadVariableOp(vars[name]!!, type1))
                    }
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
                combineParamResolvers(outerParamResolver) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) }
            ) { childMessage, childVarId ->
                val childNodes = mutableListOf<McNode>()
                val childVars = vars.toMutableMap()
                for ((index, field) in childMessage.fields.withIndex()) {
                    val childType = childMessage.toMcType()
                    val childFieldVarId = VariableId.create()
                    childVars[field.name] = childFieldVarId
                    childNodes += McNode(StoreVariableStmtOp(childFieldVarId, field.type.realType, true),
                        generateFieldReadGraph(
                            childMessage,
                            field,
                            index == childMessage.fields.lastIndex && (
                                    (message.tailrec && field.type.realType.hasName(message.className))
                                            || (childMessage.tailrec && field.type.realType.hasName(childMessage.className))
                                    ),
                            null,
                            combineParamResolvers(outerParamResolver) { name, type ->
                                McNode(LoadVariableOp(childVars[name]!!, type))
                            }
                        )
                    )
                    childNodes += McNode(StoreFieldStmtOp(childType, field.name, field.type.realType),
                        McNode(LoadVariableOp(childVarId, childType)),
                        McNode(LoadVariableOp(childFieldVarId, field.type.realType))
                    )
                    if (recursivePolymorphicMethodHandle != null && index == childMessage.fields.lastIndex) {
                        childNodes += McNode(StoreVariableStmtOp(recursivePolymorphicMethodHandle, McType.DeclaredType(
                            CommonClassNames.METHOD_HANDLE
                        ), false),
                            if (field.type.realType.hasName(message.className)) {
                                val fieldName = createTailRecurseField(childMessage.className, field.name, message.className)
                                McNode(LoadFieldOp(
                                        McType.DeclaredType(className),
                                        fieldName,
                                        McType.DeclaredType(CommonClassNames.METHOD_HANDLE),
                                        isStatic = true
                                    )
                                )
                            } else {
                                McNode(CstNullOp(McType.DeclaredType(CommonClassNames.METHOD_HANDLE)))
                            }
                        )
                    }
                }
                if (recursivePolymorphicMethodHandle != null && childMessage.fields.isEmpty()) {
                    childNodes += McNode(StoreVariableStmtOp(recursivePolymorphicMethodHandle, McType.DeclaredType(
                        CommonClassNames.METHOD_HANDLE
                    ), false),
                        McNode(CstNullOp(McType.DeclaredType(CommonClassNames.METHOD_HANDLE)))
                    )
                }
                McNode(StmtListOp, childNodes)
            }
        )
    } else {
        McNode(StoreVariableStmtOp(varId, type, true), McNode(NewOp(message.className, listOf())))
    }

    if (message.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(message.polymorphicParent)
        for (field in parentInfo.fields) {
            val fieldVarId = vars[field.name]!!
            nodes += McNode(StoreFieldStmtOp(type, field.name, field.type.realType),
                McNode(LoadVariableOp(varId, type)),
                McNode(LoadVariableOp(fieldVarId, field.type.realType))
            )
        }
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

private fun ProtocolCompiler.generateFieldReadGraph(
    message: MessageVariantInfo,
    field: McField,
    isTailRecursive: Boolean,
    tailRecursionContinueVar: VariableId?,
    paramResolver: ParamResolver
): McNode {
    val readNode = if (isTailRecursive) {
        McNode(CstNullOp(field.type.realType))
    } else {
        val polymorphicBy = field.type.polymorphicBy?.let { polymorphicBy ->
            val typeField = when (val classInfo = field.type.realType.classInfo) {
                is MessageVariantInfo -> classInfo.fields.firstOrNull()
                is MessageInfo -> classInfo.getVariant(currentProtocolId)?.fields?.firstOrNull()
                else -> throw CompileException("Invalid polymorphicBy field type")
            } ?: return@let null
            paramResolver(polymorphicBy, typeField.type.realType)
        }
        generateTypeReadGraph(message, field.type.realType, field.type.wireType, field.type.lengthInfo, paramResolver, polymorphicBy)
    }

    if (field.type.onlyIf != null) {
        val varId = VariableId.create()
        val elseBlock = mutableListOf<McNode>()
        elseBlock += McNode(StoreVariableStmtOp(varId, field.type.realType, false),
            generateDefaultConstructGraph(message, field.type, isTailRecursive, paramResolver)
        )
        if (isTailRecursive && tailRecursionContinueVar != null) {
            elseBlock += McNode(StoreVariableStmtOp(tailRecursionContinueVar, McType.BOOLEAN, false), McNode(CstBoolOp(false)))
        }
        return McNode(StmtListOp,
            McNode(DeclareVariableOnlyStmtOp(varId, field.type.realType)),
            McNode(
                IfElseStmtOp,
                generateFunctionCallGraph(message.findFunction(field.type.onlyIf), paramResolver = paramResolver),
                McNode(StmtListOp, McNode(StoreVariableStmtOp(varId, field.type.realType, false), readNode)),
                McNode(StmtListOp, elseBlock)
            ),
            McNode(ReturnStmtOp(field.type.realType), McNode(LoadVariableOp(varId, field.type.realType)))
        )
    }

    return readNode
}

private fun ProtocolCompiler.generateTypeReadGraph(
    contextMessage: MessageVariantInfo,
    realType: McType,
    wireType: Types,
    lengthInfo: LengthInfo?,
    paramResolver: ParamResolver,
    polymorphicBy: McNode? = null
): McNode {
    // deal with arrays, lists, etc
    if (realType.hasComponentType || lengthInfo?.computeInfo is LengthInfo.ComputeInfo.Raw) {
        if (realType.isOptional && lengthInfo?.computeInfo !is LengthInfo.ComputeInfo.Raw) {
            val varId = VariableId.create()
            val componentType = realType.componentType()
            return McNode(StmtListOp,
                McNode(DeclareVariableOnlyStmtOp(varId, realType)),
                McNode(
                    IfElseStmtOp,
                    IoOps.readType(VariableId.immediate("buf"), Types.BOOLEAN),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(varId, realType, false),
                            McNode(FunctionCallOp((realType as McType.DeclaredType).name, "of", listOf(componentType), realType, false),
                                generateTypeReadGraph(contextMessage, componentType, wireType, lengthInfo, paramResolver, polymorphicBy)
                            )
                        )
                    ),
                    McNode(StmtListOp, McNode(StoreVariableStmtOp(varId, realType, false),
                        generateDefaultConstructGraph(realType, paramResolver)
                    ))
                ),
                McNode(ReturnStmtOp(realType), McNode(LoadVariableOp(varId, realType)))
            )
        }

        val varId = VariableId.create()
        val lengthVar = VariableId.create()
        val lengthNode = when (lengthInfo?.computeInfo) {
            is LengthInfo.ComputeInfo.Constant -> createCstNode(lengthInfo.computeInfo.value)
            is LengthInfo.ComputeInfo.Compute -> generateFunctionCallGraph(contextMessage.findFunction(lengthInfo.computeInfo.value), paramResolver = paramResolver)
            is LengthInfo.ComputeInfo.RemainingBytes -> McNode(FunctionCallOp(BYTE_BUF, "readableBytes", listOf(McType.BYTE_BUF), McType.INT, true, isStatic = false),
                McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF))
            )
            is LengthInfo.ComputeInfo.Raw,
            null -> McType.INT.cast(IoOps.readType(VariableId.immediate("buf"), lengthInfo?.type ?: Types.VAR_INT))
        }

        if (lengthInfo?.computeInfo is LengthInfo.ComputeInfo.Raw) {
            // save the length offset and restore it after the read
            val resultVar = VariableId.create()
            return McNode(StmtListOp,
                McNode(StoreVariableStmtOp(lengthVar, McType.INT, true), lengthNode),
                McNode(StoreVariableStmtOp(lengthVar, McType.INT, false, "+="),
                    McNode(FunctionCallOp(BYTE_BUF, "readerIndex", listOf(McType.BYTE_BUF), McType.INT, true, isStatic = false),
                        McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF))
                    )
                ),
                McNode(StoreVariableStmtOp(resultVar, realType, true),
                    generateTypeReadGraph(contextMessage, realType, wireType, null, paramResolver, polymorphicBy)
                ),
                McNode(PopStmtOp,
                    McNode(FunctionCallOp(BYTE_BUF, "readerIndex", listOf(McType.BYTE_BUF, McType.INT), McType.VOID, true, isStatic = false),
                        McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF)),
                        McNode(LoadVariableOp(lengthVar, McType.INT))
                    )
                ),
                McNode(ReturnStmtOp(realType), McNode(LoadVariableOp(resultVar, realType)))
            )
        }

        // byte array optimization: can read directly to it
        if (realType == McType.BYTE.arrayOf()) {
            return McNode(StmtListOp,
                McNode(StoreVariableStmtOp(varId, realType, true),
                    McNode(NewArrayOp(McType.BYTE), lengthNode)
                ),
                McNode(PopStmtOp,
                    McNode(FunctionCallOp(BYTE_BUF, "readBytes", listOf(McType.BYTE_BUF, realType), McType.BYTE_BUF, true, isStatic = false),
                        McNode(LoadVariableOp(VariableId.immediate("buf"), McType.BYTE_BUF)),
                        McNode(LoadVariableOp(varId, realType))
                    )
                ),
                McNode(ReturnStmtOp(realType), McNode(LoadVariableOp(varId, realType)))
            )
        }

        val arrayType = when (realType) {
            is McType.ArrayType -> realType
            is McType.DeclaredType -> when (realType.name) {
                CommonClassNames.BITSET -> McType.LONG.arrayOf()
                else -> null
            }
            else -> null
        }
        val loopVar = VariableId.create()
        val innerReader = generateTypeReadGraph(contextMessage, realType.componentType(), wireType, null, paramResolver)
        val listReader = McNode(StmtListOp,
            McNode(StoreVariableStmtOp(lengthVar, McType.INT, true), lengthNode),
            McNode(StoreVariableStmtOp(varId, realType, true), when {
                arrayType != null -> McNode(
                    NewArrayOp(arrayType.elementType),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                )
                realType is McType.DeclaredType -> {
                    val concreteType = when (realType.name) {
                        CommonClassNames.INT_LIST -> CommonClassNames.INT_ARRAY_LIST
                        CommonClassNames.LONG_LIST -> CommonClassNames.LONG_ARRAY_LIST
                        CommonClassNames.LIST -> CommonClassNames.ARRAY_LIST
                        else -> realType.name
                    }
                    McNode(
                        ImplicitCastOp(McType.DeclaredType(concreteType), realType),
                        McNode(
                            NewOp(concreteType, listOf(McType.INT)),
                            McNode(LoadVariableOp(lengthVar, McType.INT))
                        )
                    )
                }
                else -> throw IllegalStateException("Invalid type with length")
            }),
            McNode(StoreVariableStmtOp(loopVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(loopVar, McType.INT)),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                ),
                McNode(StmtListOp,
                    if (arrayType != null) {
                        McNode(
                            StoreArrayStmtOp(arrayType.elementType),
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
                        McNode(CstIntOp(1))
                    )
                )
            ),
            McNode(ReturnStmtOp(realType), McNode(LoadVariableOp(varId, realType)))
        )
        return if (realType.hasName(CommonClassNames.BITSET)) {
            McNode(FunctionCallOp(
                CommonClassNames.BITSET, "valueOf", listOf(McType.LONG.arrayOf()), McType.DeclaredType(
                    CommonClassNames.BITSET
                ), false),
                listReader
            )
        } else {
            listReader
        }
    }

    // message types have special handling
    if (wireType == Types.MESSAGE) {
        return when (val classInfo = realType.classInfo) {
            is MessageVariantInfo -> generateMessageReadGraph(classInfo, paramResolver, polymorphicBy)
            is MessageInfo -> {
                val variant = classInfo.getVariant(currentProtocolId)!!
                McNode(ImplicitCastOp(variant.toMcType(), realType),
                    generateMessageReadGraph(variant, paramResolver, polymorphicBy)
                )
            }
            else -> throw IllegalStateException("Invalid real type for message type")
        }
    }

    // enums are done by ordinal
    if (realType.classInfoOrNull is EnumInfo) {
        val enumName = (realType as McType.DeclaredType).name
        val fieldName = "ENUM_VALUES_${enumName.uppercase().replace('.', '_')}"
        addMember(fieldName) { emitter ->
            emitter.append("private static final ").appendClassName(enumName).append("[] ").append(fieldName)
                .append(" = ").appendClassName(enumName).append(".values();")
        }
        return McNode(
            LoadArrayOp(realType),
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
