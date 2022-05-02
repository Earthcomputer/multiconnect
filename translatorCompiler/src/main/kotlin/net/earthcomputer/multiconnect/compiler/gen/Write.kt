package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames
import net.earthcomputer.multiconnect.compiler.CommonClassNames.BYTE_BUF
import net.earthcomputer.multiconnect.compiler.CommonClassNames.UNPOOLED
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
import net.earthcomputer.multiconnect.compiler.node.CastOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstNullOp
import net.earthcomputer.multiconnect.compiler.node.CstStringOp
import net.earthcomputer.multiconnect.compiler.node.DoWhileStmtOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.IfStmtOp
import net.earthcomputer.multiconnect.compiler.node.InstanceOfOp
import net.earthcomputer.multiconnect.compiler.node.LoadArrayOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.NewOp
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.ThrowStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.WhileStmtOp
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.splitPackageClass

internal fun ProtocolCompiler.generateWriteGraph(
    messageInfo: MessageInfo,
    messageVar: VariableId,
    isPolymorphicBy: Boolean,
    bufVar: VariableId,
    outerParamResolver: ParamResolver?,
): McNode {
    val variant = messageInfo.getVariant(currentProtocolId)!!
    val newVar = VariableId.create()
    return McNode(StmtListOp,
        McNode(StoreVariableStmtOp(newVar, variant.toMcType(), true),
            McNode(CastOp(messageInfo.toMcType(), variant.toMcType()),
                McNode(LoadVariableOp(messageVar, messageInfo.toMcType()))
            )
        ),
        generateWriteGraph(variant, newVar, isPolymorphicBy, bufVar, outerParamResolver)
    )
}

internal fun ProtocolCompiler.generateWriteGraph(
    messageInfo: MessageVariantInfo,
    messageVar: VariableId,
    isPolymorphicBy: Boolean,
    bufVar: VariableId,
    outerParamResolver: ParamResolver?,
): McNode {
    return if (messageInfo.tailrec) {
        val messageType = messageInfo.toMcType()
        val nodes = mutableListOf<McNode>()
        val currentMessageVar = VariableId.create()

        nodes += McNode(StoreVariableStmtOp(currentMessageVar, messageType, true),
            McNode(LoadVariableOp(messageVar, messageType))
        )

        val loopBody = if (messageInfo.fields.last().type.realType.hasName(messageInfo.className)) {
            // the onlyIf variant of recursion
            McNode(StmtListOp,
                generateWriteGraphInner(messageInfo, currentMessageVar, bufVar, isPolymorphicBy, null, outerParamResolver),
                McNode(StoreVariableStmtOp(currentMessageVar, messageType, false),
                    McNode(LoadFieldOp(messageType, messageInfo.fields.last().name, messageType),
                        McNode(LoadVariableOp(messageVar, messageType))
                    )
                )
            )
        } else {
            // the polymorphic variant of recursion
            McNode(StmtListOp,
                generateWriteGraphInner(messageInfo, currentMessageVar, bufVar, isPolymorphicBy, currentMessageVar, outerParamResolver)
            )
        }

        nodes += McNode(DoWhileStmtOp,
            loopBody,
            McNode(BinaryExpressionOp("!=", messageType, messageType),
                McNode(LoadVariableOp(currentMessageVar, messageType)),
                McNode(CstNullOp(messageType))
            )
        )

        McNode(StmtListOp, nodes)
    } else {
        generateWriteGraphInner(messageInfo, messageVar, bufVar, isPolymorphicBy, null, outerParamResolver)
    }
}

private fun ProtocolCompiler.generateWriteGraphInner(
    messageInfo: MessageVariantInfo,
    messageVar: VariableId,
    bufVar: VariableId,
    isPolymorphicBy: Boolean,
    outPolymorphicNext: VariableId?,
    outerParamResolver: ParamResolver?,
): McNode {
    val type = messageInfo.toMcType()
    val vars = mutableMapOf<String, VariableId>()
    val nodes = mutableListOf<McNode>()

    if (messageInfo.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(messageInfo.polymorphicParent)
        for (field in parentInfo.fields) {
            val fieldVarId = VariableId.create()
            vars[field.name] = fieldVarId
            nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true),
                McNode(LoadFieldOp(type, field.name, field.type.realType),
                    McNode(LoadVariableOp(messageVar, type))
                )
            )
            nodes += generateFieldWriteGraph(
                parentInfo,
                field,
                fieldVarId,
                bufVar,
                combineParamResolvers(outerParamResolver) { name, type1 -> McNode(LoadVariableOp(vars[name]!!, type1)) }
            )
        }
    }

    for ((index, field) in messageInfo.fields.withIndex()) {
        if (isPolymorphicBy && index == 0) {
            continue
        }
        if (messageInfo.tailrec && field.type.realType.hasName(messageInfo.className)) {
            continue
        }
        val fieldVarId = VariableId.create()
        vars[field.name] = fieldVarId
        nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true),
            McNode(LoadFieldOp(type, field.name, field.type.realType),
                McNode(LoadVariableOp(messageVar, type))
            )
        )
        nodes += generateFieldWriteGraph(
            messageInfo,
            field,
            fieldVarId,
            bufVar,
            combineParamResolvers(outerParamResolver) { name, type1 -> McNode(LoadVariableOp(vars[name]!!, type1)) }
        )
    }

    if (messageInfo.polymorphic != null && messageInfo.polymorphicParent == null) {
        var ifElseChain = McNode(ThrowStmtOp,
            McNode(NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                McNode(CstStringOp("Polymorphic subclass of \"${splitPackageClass(messageInfo.className).second}\" has instance of illegal type"))
            )
        )
        for (childName in polymorphicChildren[messageInfo.className]!!.asReversed()) {
            val childInfo = getMessageVariantInfo(childName)
            val childType = childInfo.toMcType()
            val ifBlock = mutableListOf<McNode>()
            val childVarId = VariableId.create()
            val childVars = vars.toMutableMap()

            ifBlock += McNode(StoreVariableStmtOp(childVarId, childType, true),
                McNode(CastOp(type, childType),
                    McNode(LoadVariableOp(messageVar, type))
                )
            )

            for (field in childInfo.fields) {
                if (messageInfo.tailrec && field.type.realType.hasName(messageInfo.className)) {
                    continue
                }
                val fieldVarId = VariableId.create()
                childVars[field.name] = fieldVarId
                ifBlock += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true),
                    McNode(LoadFieldOp(childType, field.name, field.type.realType),
                        McNode(LoadVariableOp(childVarId, childType))
                    )
                )
                ifBlock += generateFieldWriteGraph(
                    childInfo,
                    field,
                    fieldVarId,
                    bufVar,
                    combineParamResolvers(outerParamResolver) { name, type1 -> McNode(LoadVariableOp(childVars[name]!!, type1)) }
                )
            }

            if (messageInfo.tailrec && outPolymorphicNext != null) {
                ifBlock += if (childInfo.fields.lastOrNull()?.type?.realType?.hasName(messageInfo.className) == true) {
                    McNode(StoreVariableStmtOp(outPolymorphicNext, type, false),
                        McNode(LoadFieldOp(childType, childInfo.fields.last().name, type),
                            McNode(LoadVariableOp(childVarId, childType))
                        )
                    )
                } else {
                    McNode(StoreVariableStmtOp(outPolymorphicNext, type, false),
                        McNode(CstNullOp(type))
                    )
                }
            }

            ifElseChain = McNode(IfElseStmtOp,
                McNode(InstanceOfOp(type, childType),
                    McNode(LoadVariableOp(messageVar, type))
                ),
                McNode(StmtListOp, ifBlock),
                McNode(StmtListOp, ifElseChain)
            )
        }

        nodes += ifElseChain
    }

    return McNode(StmtListOp, nodes)
}

private fun ProtocolCompiler.generateTypeWriteGraph(
    realType: McType,
    wireType: Types,
    lengthInfo: LengthInfo?,
    varId: VariableId,
    bufVar: VariableId,
    isPolymorphicBy: Boolean,
    outerParamResolver: ParamResolver?
): McNode {
    if (lengthInfo?.computeInfo is LengthInfo.ComputeInfo.Raw) {
        val innerBufVar = VariableId.create()
        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(innerBufVar, McType.BYTE_BUF, true),
                McNode(FunctionCallOp(UNPOOLED, "buffer", listOf(), McType.BYTE_BUF, false))
            ),
            generateTypeWriteGraph(realType, wireType, null, varId, innerBufVar, isPolymorphicBy, outerParamResolver),
            IoOps.writeType(bufVar, lengthInfo.type, McNode(FunctionCallOp(BYTE_BUF, "writerIndex", listOf(McType.BYTE_BUF), McType.INT, false, isStatic = false),
                McNode(LoadVariableOp(innerBufVar, McType.BYTE_BUF))
            )),
            McNode(PopStmtOp,
                McNode(FunctionCallOp(BYTE_BUF, "writeBytes", listOf(McType.BYTE_BUF, McType.BYTE_BUF), McType.VOID, true, isStatic = false),
                    McNode(LoadVariableOp(bufVar, McType.BYTE_BUF)),
                    McNode(LoadVariableOp(innerBufVar, McType.BYTE_BUF))
                )
            )
        )
    }

    // deal with arrays, lists, etc
    if (realType.hasComponentType) {
        if (realType.isOptional) {
            realType as McType.DeclaredType
            val isPresentVar = VariableId.create()
            val elementVar = VariableId.create()
            val getName = when (realType.name) {
                CommonClassNames.OPTIONAL -> "get"
                CommonClassNames.OPTIONAL_INT -> "getAsInt"
                CommonClassNames.OPTIONAL_LONG -> "getAsLong"
                else -> throw CompileException("Invalid optional")
            }
            return McNode(StmtListOp,
                McNode(StoreVariableStmtOp(isPresentVar, McType.BOOLEAN, true),
                    McNode(FunctionCallOp(realType.name, "isPresent", listOf(realType), McType.BOOLEAN, false, isStatic = false),
                        McNode(LoadVariableOp(varId, realType))
                    )
                ),
                IoOps.writeType(bufVar, Types.BOOLEAN, McNode(LoadVariableOp(isPresentVar, McType.BOOLEAN))),
                McNode(IfStmtOp,
                    McNode(LoadVariableOp(isPresentVar, McType.BOOLEAN)),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(elementVar, realType.componentType(), true),
                            McNode(FunctionCallOp(realType.name, getName, listOf(realType), realType.componentType(), false, isStatic = false),
                                McNode(LoadVariableOp(varId, realType))
                            )
                        ),
                        generateTypeWriteGraph(realType.componentType(), wireType, lengthInfo, elementVar, bufVar, isPolymorphicBy, outerParamResolver)
                    )
                )
            )
        }

        val lengthNode = if (realType is McType.ArrayType) {
            McNode(LoadFieldOp(realType, "length", McType.INT),
                McNode(LoadVariableOp(varId, realType))
            )
        } else {
            realType as McType.DeclaredType
            McNode(FunctionCallOp(realType.name, "size", listOf(realType), McType.INT, false, isStatic = false),
                McNode(LoadVariableOp(varId, realType))
            )
        }

        val lengthWriteNode = if (lengthInfo?.computeInfo == null) {
            IoOps.writeType(bufVar, lengthInfo?.type ?: Types.VAR_INT, lengthNode)
        } else {
            McNode(StmtListOp)
        }

        // byte array optimization: can write directly from it
        if (realType == McType.BYTE.arrayOf()) {
            return McNode(StmtListOp,
                lengthWriteNode,
                McNode(PopStmtOp,
                    McNode(FunctionCallOp(BYTE_BUF, "writeBytes", listOf(McType.BYTE_BUF, realType), McType.VOID, true, isStatic = false),
                        McNode(LoadVariableOp(bufVar, McType.BYTE_BUF)),
                        McNode(LoadVariableOp(varId, realType))
                    )
                )
            )
        }

        val indexVar = VariableId.create()
        val elementVar = VariableId.create()

        val getNode = if (realType is McType.ArrayType) {
            McNode(LoadArrayOp(realType.elementType),
                McNode(LoadVariableOp(varId, realType)),
                McNode(LoadVariableOp(indexVar, McType.INT))
            )
        } else {
            realType as McType.DeclaredType
            val funcName = when (realType.name) {
                CommonClassNames.LIST -> "get"
                CommonClassNames.INT_LIST -> "getInt"
                CommonClassNames.LONG_LIST -> "getLong"
                else -> throw CompileException("Invalid list")
            }
            McNode(FunctionCallOp(realType.name, funcName, listOf(realType, McType.INT), realType.componentType(), false, isStatic = false),
                McNode(LoadVariableOp(varId, realType)),
                McNode(LoadVariableOp(indexVar, McType.INT))
            )
        }

        return McNode(StmtListOp,
            lengthWriteNode,
            McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    lengthNode
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(elementVar, realType.componentType(), true), getNode),
                    generateTypeWriteGraph(realType.componentType(), wireType, null, elementVar, bufVar, false, outerParamResolver),
                    McNode(StoreVariableStmtOp(indexVar, McType.INT, false, "+="), McNode(CstIntOp(1)))
                )
            )
        )
    }

    // message types have special handling
    if (wireType == Types.MESSAGE) {
        return when (val classInfo = realType.classInfo) {
            is MessageVariantInfo -> generateWriteGraph(classInfo, varId, isPolymorphicBy, bufVar, outerParamResolver)
            is MessageInfo -> generateWriteGraph(classInfo, varId, isPolymorphicBy, bufVar, outerParamResolver)
            else -> throw IllegalStateException("Invalid real type for message type")
        }
    }

    // enums are done by ordinal
    if (realType.classInfoOrNull is EnumInfo) {
        return IoOps.writeType(
            bufVar,
            wireType,
            McNode(FunctionCallOp((realType as McType.DeclaredType).name, "ordinal", listOf(realType), McType.INT, false, isStatic = false),
                McNode(LoadVariableOp(varId, realType))
            )
        )
    }

    // direct write, easy
    return IoOps.writeType(bufVar, wireType, McNode(LoadVariableOp(varId, realType)))
}

private fun ProtocolCompiler.generateFieldWriteGraph(messageInfo: MessageVariantInfo, field: McField, fieldVar: VariableId, bufVar: VariableId, paramResolver: ParamResolver): McNode {
    val isPolymorphicBy = field.type.polymorphicBy != null
    return if (field.type.onlyIf != null) {
        McNode(IfStmtOp,
            generateFunctionCallGraph(messageInfo.findFunction(field.type.onlyIf), paramResolver = paramResolver),
            McNode(StmtListOp,
                generateTypeWriteGraph(field.type.realType, field.type.wireType, field.type.lengthInfo, fieldVar, bufVar, isPolymorphicBy, paramResolver)
            )
        )
    } else {
        generateTypeWriteGraph(field.type.realType, field.type.wireType, field.type.lengthInfo, fieldVar, bufVar, isPolymorphicBy, paramResolver)
    }
}
