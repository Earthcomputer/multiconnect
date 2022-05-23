package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.compiler.CommonClassNames
import net.earthcomputer.multiconnect.compiler.CommonClassNames.ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LONG_ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LONG_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL_INT
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL_LONG
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.IntroduceInfo
import net.earthcomputer.multiconnect.compiler.McField
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.classInfoOrNull
import net.earthcomputer.multiconnect.compiler.componentType
import net.earthcomputer.multiconnect.compiler.componentTypeOrNull
import net.earthcomputer.multiconnect.compiler.deepComponentType
import net.earthcomputer.multiconnect.compiler.getClassInfo
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.hasName
import net.earthcomputer.multiconnect.compiler.isList
import net.earthcomputer.multiconnect.compiler.isOptional
import net.earthcomputer.multiconnect.compiler.messageVariantInfo
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CastOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstNullOp
import net.earthcomputer.multiconnect.compiler.node.DeclareVariableOnlyStmtOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.ImplicitCastOp
import net.earthcomputer.multiconnect.compiler.node.InstanceOfOp
import net.earthcomputer.multiconnect.compiler.node.LambdaOp
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
import net.earthcomputer.multiconnect.compiler.node.ThrowStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.WhileStmtOp
import net.earthcomputer.multiconnect.compiler.node.createCstNode
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.splitPackageClass

private fun MessageInfo.needsTranslation(versionA: Int, versionB: Int): Boolean {
    val variantA = getVariant(versionA) ?: throw CompileException("No variants of $className found for protocol $versionA")
    val variantB = getVariant(versionB) ?: throw CompileException("No variants of $className found for protocol $versionB")

    if (variantA.className != variantB.className) {
        return true
    }

    return variantA.needsTranslation(versionA, versionB)
}

private fun MessageVariantInfo.needsTranslation(versionA: Int, versionB: Int): Boolean {
    fun fieldNeedsTranslation(field: McField): Boolean {
        val type = field.type.realType.deepComponentType().classInfoOrNull
        if (type is MessageInfo) {
            return type.needsTranslation(versionA, versionB)
        } else if (type is MessageVariantInfo) {
            return type.needsTranslation(versionA, versionB)
        }
        return false
    }

    for (field in fields) {
        if (tailrec && field.type.realType.hasName(className)) {
            continue
        }
        if (polymorphicParent != null && field.type.realType.hasName(polymorphicParent)) {
            continue
        }
        if (fieldNeedsTranslation(field)) {
            return true
        }
    }

    if (polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(polymorphicParent)
        for (field in parentInfo.fields) {
            if (parentInfo.tailrec && field.type.realType.hasName(polymorphicParent)) {
                continue
            }
            if (fieldNeedsTranslation(field)) {
                return true
            }
        }
    } else if (polymorphic != null) {
        for (child in polymorphicChildren[className]!!) {
            val childInfo = getMessageVariantInfo(child)
            for (field in childInfo.fields) {
                if (childInfo.tailrec && field.type.realType.hasName(child)) {
                    continue
                }
                if (tailrec && field.type.realType.hasName(className)) {
                    continue
                }
                if (fieldNeedsTranslation(field)) {
                    return true
                }
            }
        }
    }

    return false
}

internal fun ProtocolCompiler.translate(
    message: MessageVariantInfo,
    fromVersion: Int,
    toVersion: Int,
    fromVarId: VariableId,
    outerParamResolver: ParamResolver?
): McNode {
    val messageType = message.toMcType()

    if (!message.needsTranslation(fromVersion, toVersion)) {
        return McNode(LoadVariableOp(fromVarId, messageType))
    }

    return if (message.tailrec) {
        val nodes = mutableListOf<McNode>()
        val toVarId = VariableId.create()
        val curFromVarId = VariableId.create()
        val curToVarId = VariableId.create()
        val nextToVarId = VariableId.create()
        if (message.fields.last().type.realType.hasName(message.className)) {
            // the onlyIf variant of recursion
            nodes += McNode(StoreVariableStmtOp(toVarId, messageType, true),
                translateInner(message, fromVersion, toVersion, fromVarId, outerParamResolver, null)
            )
            nodes += McNode(StoreVariableStmtOp(curFromVarId, messageType, true),
                McNode(LoadVariableOp(fromVarId, messageType))
            )
            nodes += McNode(StoreVariableStmtOp(curToVarId, messageType, true),
                McNode(LoadVariableOp(toVarId, messageType))
            )
            nodes += McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("!=", messageType, messageType),
                    McNode(LoadFieldOp(messageType, message.fields.last().name, messageType),
                        McNode(LoadVariableOp(curFromVarId, messageType))
                    ),
                    McNode(CstNullOp(messageType))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(curFromVarId, messageType, false),
                        McNode(LoadFieldOp(messageType, message.fields.last().name, messageType),
                            McNode(LoadVariableOp(curFromVarId, messageType))
                        ),
                    ),
                    McNode(StoreVariableStmtOp(nextToVarId, messageType, true),
                        translateInner(message, fromVersion, toVersion, curFromVarId, outerParamResolver, null)
                    ),
                    McNode(StoreFieldStmtOp(messageType, message.fields.last().name, messageType),
                        McNode(LoadVariableOp(curToVarId, messageType)),
                        McNode(LoadVariableOp(nextToVarId, messageType))
                    ),
                    McNode(StoreVariableStmtOp(curToVarId, messageType, false),
                        McNode(LoadVariableOp(nextToVarId, messageType))
                    )
                )
            )
        } else {
            // the polymorphic variant of tail recursion
            val methodHandleType = McType.DeclaredType(CommonClassNames.METHOD_HANDLE)
            val currentMethodHandle = VariableId.create()
            val nextMethodHandle = VariableId.create()
            val nextFromVarId = VariableId.create()

            fun handleSubtype(subtype: MessageVariantInfo, subtypeFromVarId: VariableId): McNode {
                return if (subtype.fields.lastOrNull()?.type?.realType?.hasName(message.className) == true) {
                    val subtypeType = subtype.toMcType()
                    val tailRecurseField = createTailRecurseField(subtype.className, subtype.fields.last().name, message.className)
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(nextFromVarId, messageType, false),
                            McNode(LoadFieldOp(subtypeType, subtype.fields.last().name, messageType),
                                McNode(LoadVariableOp(subtypeFromVarId, subtypeType))
                            )
                        ),
                        McNode(StoreVariableStmtOp(nextMethodHandle, methodHandleType, false),
                            McNode(LoadFieldOp(McType.DeclaredType(className), tailRecurseField, methodHandleType, isStatic = true))
                        )
                    )
                } else {
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(nextFromVarId, messageType, false),
                            McNode(CstNullOp(messageType))
                        )
                    )
                }
            }

            nodes += McNode(StoreVariableStmtOp(nextMethodHandle, methodHandleType, true),
                McNode(CstNullOp(methodHandleType))
            )
            nodes += McNode(DeclareVariableOnlyStmtOp(nextFromVarId, messageType))
            nodes += McNode(StoreVariableStmtOp(toVarId, messageType, true),
                translateInner(message, fromVersion, toVersion, fromVarId, outerParamResolver, ::handleSubtype)
            )
            nodes += McNode(StoreVariableStmtOp(curToVarId, messageType, true),
                McNode(LoadVariableOp(toVarId, messageType))
            )
            nodes += McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("!=", messageType, messageType),
                    McNode(LoadVariableOp(nextFromVarId, messageType)),
                    McNode(CstNullOp(messageType))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(curFromVarId, messageType, true),
                        McNode(LoadVariableOp(nextFromVarId, messageType))
                    ),
                    McNode(StoreVariableStmtOp(currentMethodHandle, methodHandleType, true),
                        McNode(LoadVariableOp(nextMethodHandle, methodHandleType))
                    ),
                    McNode(StoreVariableStmtOp(nextToVarId, messageType, true),
                        translateInner(message, fromVersion, toVersion, curFromVarId, outerParamResolver, ::handleSubtype)
                    ),
                    McNode(PopStmtOp,
                        McNode(FunctionCallOp(CommonClassNames.METHOD_HANDLE, "invoke", listOf(methodHandleType, messageType, messageType), McType.VOID, true, isStatic = false, throwsException = true),
                            McNode(LoadVariableOp(currentMethodHandle, methodHandleType)),
                            McNode(LoadVariableOp(curToVarId, messageType)),
                            McNode(LoadVariableOp(nextToVarId, messageType))
                        )
                    )
                )
            )
        }
        nodes += McNode(ReturnStmtOp(messageType), McNode(LoadVariableOp(toVarId, messageType)))
        McNode(StmtListOp, nodes)
    } else {
        translateInner(message, fromVersion, toVersion, fromVarId, outerParamResolver, null)
    }
}

private fun ProtocolCompiler.wrapContainerTranslation(type: McType, varId: VariableId, innerTranslator: (VariableId) -> McNode): McNode {
    val componentType = type.componentTypeOrNull() ?: return innerTranslator(varId)

    if (type.hasName(OPTIONAL)) {
        val functionType = McType.DeclaredType("java.util.function.Function")
        val lambdaParam = VariableId.create()
        return McNode(FunctionCallOp(OPTIONAL, "map", listOf(type, functionType), type, false, isStatic = false),
            McNode(LoadVariableOp(varId, type)),
            McNode(LambdaOp(functionType, componentType, listOf(componentType), listOf(lambdaParam)),
                wrapContainerTranslation(componentType, lambdaParam, innerTranslator)
            )
        )
    } else if (type.hasName(LIST)) {
        val resultVar = VariableId.create()
        val lengthVar = VariableId.create()
        val indexVar = VariableId.create()
        val innerVar = VariableId.create()
        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(lengthVar, McType.INT, true),
                McNode(FunctionCallOp(LIST, "size", listOf(type), McType.INT, false, isStatic = false),
                    McNode(LoadVariableOp(varId, type))
                )
            ),
            McNode(StoreVariableStmtOp(resultVar, type, true),
                McNode(ImplicitCastOp(McType.DeclaredType(ARRAY_LIST), type),
                    McNode(NewOp(ARRAY_LIST, listOf(McType.INT)),
                        McNode(LoadVariableOp(lengthVar, McType.INT))
                    )
                )
            ),
            McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(innerVar, componentType, true),
                        McNode(FunctionCallOp(LIST, "get", listOf(type, McType.INT), componentType, false, isStatic = false),
                            McNode(LoadVariableOp(varId, type)),
                            McNode(LoadVariableOp(indexVar, McType.INT))
                        )
                    ),
                    McNode(PopStmtOp,
                        McNode(FunctionCallOp(LIST, "add", listOf(type, componentType), McType.VOID, true, isStatic = false),
                            McNode(LoadVariableOp(resultVar, type)),
                            wrapContainerTranslation(componentType, innerVar, innerTranslator)
                        )
                    ),
                    McNode(StoreVariableStmtOp(indexVar, McType.INT, false, "+="), McNode(CstIntOp(1)))
                )
            ),
            McNode(ReturnStmtOp(type), McNode(LoadVariableOp(resultVar, type)))
        )
    } else if (type is McType.ArrayType) {
        val resultVar = VariableId.create()
        val lengthVar = VariableId.create()
        val indexVar = VariableId.create()
        val innerVar = VariableId.create()
        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(lengthVar, McType.INT, true),
                McNode(LoadFieldOp(type, "length", McType.INT),
                    McNode(LoadVariableOp(varId, type))
                )
            ),
            McNode(StoreVariableStmtOp(resultVar, type, true),
                McNode(NewArrayOp(componentType), McNode(LoadVariableOp(lengthVar, McType.INT)))
            ),
            McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(innerVar, componentType, true),
                        McNode(LoadArrayOp(componentType),
                            McNode(LoadVariableOp(varId, type)),
                            McNode(LoadVariableOp(indexVar, McType.INT))
                        )
                    ),
                    McNode(StoreArrayStmtOp(componentType),
                        McNode(LoadVariableOp(resultVar, type)),
                        McNode(LoadVariableOp(indexVar, McType.INT)),
                        wrapContainerTranslation(componentType, innerVar, innerTranslator)
                    ),
                    McNode(StoreVariableStmtOp(indexVar, McType.INT, false, "+="), McNode(CstIntOp(1)))
                )
            ),
            McNode(ReturnStmtOp(type), McNode(LoadVariableOp(resultVar, type)))
        )
    }

    return innerTranslator(varId)
}

private fun ProtocolCompiler.translateInner(
    message: MessageVariantInfo,
    fromVersion: Int,
    toVersion: Int,
    fromVarId: VariableId,
    outerParamResolver: ParamResolver?,
    extraSubtypeInstructions: ((MessageVariantInfo, VariableId) -> McNode)?
): McNode {
    val messageType = message.toMcType()
    val nodes = mutableListOf<McNode>()
    val vars = mutableMapOf<String, Pair<McType, VariableId>>()

    fun processField(field: McField) {
        val fromFieldVarId = VariableId.create()
        val toFieldVarId = VariableId.create()
        vars[field.name] = Pair(field.type.realType, toFieldVarId)
        nodes += McNode(StoreVariableStmtOp(fromFieldVarId, field.type.realType, true),
            McNode(LoadFieldOp(messageType, field.name, field.type.realType),
                McNode(LoadVariableOp(fromVarId, messageType))
            )
        )
        val translateNode = when (val fieldTypeInfo = field.type.realType.deepComponentType().classInfoOrNull) {
            is MessageInfo -> wrapContainerTranslation(field.type.realType, fromFieldVarId) { varId ->
                McNode(ImplicitCastOp(fieldTypeInfo.getVariant(toVersion)!!.toMcType(), fieldTypeInfo.toMcType()), translate(
                    fieldTypeInfo,
                    fromVersion,
                    toVersion,
                    varId,
                    combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!.second, type)) }
                ))
            }
            is MessageVariantInfo -> wrapContainerTranslation(field.type.realType, fromFieldVarId) { varId ->
                translate(
                    fieldTypeInfo,
                    fromVersion,
                    toVersion,
                    varId,
                    combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!.second, type)) }
                )
            }
            else -> McNode(LoadVariableOp(fromFieldVarId, field.type.realType))
        }
        nodes += McNode(StoreVariableStmtOp(toFieldVarId, field.type.realType, true), translateNode)
    }

    if (message.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(message.polymorphicParent)
        for (field in parentInfo.fields) {
            processField(field)
        }
    }
    for (field in message.fields) {
        if (!message.tailrec || !field.type.realType.hasName(message.className)) {
            processField(field)
        }
    }

    val toVarId = VariableId.create()

    if (message.polymorphic != null && message.polymorphicParent == null) {
        var polymorphicChildHandler = McNode(ThrowStmtOp,
            McNode(NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                createCstNode("Polymorphic subclass of \"${splitPackageClass(message.className).second}\" has instance of illegal type")
            )
        )
        for (childName in polymorphicChildren[message.className]!!.asReversed()) {
            val childType = McType.DeclaredType(childName)
            val childInfo = childType.messageVariantInfo
            val childNodes = mutableListOf<McNode>()
            val childFromVarId = VariableId.create()
            val childToVarId = VariableId.create()
            childNodes += McNode(StoreVariableStmtOp(childFromVarId, childType, true),
                McNode(CastOp(messageType, childType), McNode(LoadVariableOp(fromVarId, messageType)))
            )
            childNodes += McNode(StoreVariableStmtOp(childToVarId, childType, true),
                McNode(NewOp(childName, listOf()))
            )
            for (field in childInfo.fields) {
                if (!message.tailrec || !field.type.realType.hasName(message.className)) {
                    val fromFieldVarId = VariableId.create()
                    childNodes += McNode(StoreVariableStmtOp(fromFieldVarId, field.type.realType, true),
                        McNode(LoadFieldOp(childType, field.name, field.type.realType),
                            McNode(LoadVariableOp(childFromVarId, childType))
                        )
                    )
                    val translateNode = when (val fieldTypeInfo = field.type.realType.deepComponentType().classInfoOrNull) {
                        is MessageInfo -> wrapContainerTranslation(field.type.realType, fromFieldVarId) { varId ->
                            McNode(ImplicitCastOp(fieldTypeInfo.getVariant(toVersion)!!.toMcType(), fieldTypeInfo.toMcType()), translate(
                                fieldTypeInfo,
                                fromVersion,
                                toVersion,
                                varId,
                                combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!.second, type)) }
                            ))
                        }
                        is MessageVariantInfo -> wrapContainerTranslation(field.type.realType, fromFieldVarId) { varId ->
                            translate(
                                fieldTypeInfo,
                                fromVersion,
                                toVersion,
                                varId,
                                combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!.second, type)) }
                            )
                        }
                        else -> McNode(LoadVariableOp(fromFieldVarId, field.type.realType))
                    }
                    childNodes += McNode(StoreFieldStmtOp(childType, field.name, field.type.realType),
                        McNode(LoadVariableOp(childToVarId, childType)),
                        translateNode
                    )
                }
            }
            if (extraSubtypeInstructions != null) {
                childNodes += extraSubtypeInstructions(childInfo, childFromVarId)
            }
            childNodes += McNode(StoreVariableStmtOp(toVarId, messageType, false),
                McNode(ImplicitCastOp(childType, messageType),
                    McNode(LoadVariableOp(childToVarId, childType))
                )
            )

            polymorphicChildHandler = McNode(IfElseStmtOp,
                McNode(InstanceOfOp(messageType, childType),
                    McNode(LoadVariableOp(fromVarId, messageType))
                ),
                McNode(StmtListOp, childNodes),
                McNode(StmtListOp, polymorphicChildHandler)
            )
        }

        nodes += McNode(DeclareVariableOnlyStmtOp(toVarId, messageType))
        nodes += polymorphicChildHandler
    } else {
        nodes += McNode(StoreVariableStmtOp(toVarId, messageType, true),
            McNode(NewOp(message.className, listOf()))
        )
    }

    for ((name, varIdAndType) in vars) {
        val (type, varId) = varIdAndType
        nodes += McNode(StoreFieldStmtOp(messageType, name, type),
            McNode(LoadVariableOp(toVarId, messageType)),
            McNode(LoadVariableOp(varId, type))
        )
    }

    nodes += McNode(ReturnStmtOp(messageType), McNode(LoadVariableOp(toVarId, messageType)))

    return McNode(StmtListOp, nodes)
}

internal fun ProtocolCompiler.translate(
    message: MessageInfo,
    fromVersion: Int,
    toVersion: Int,
    fromVarId: VariableId,
    outerParamResolver: ParamResolver?
): McNode {
    val messageType = message.toMcType()

    if (!message.needsTranslation(fromVersion, toVersion)) {
        return McNode(CastOp(messageType, message.getVariant(toVersion)!!.toMcType()),
            McNode(LoadVariableOp(fromVarId, messageType))
        )
    }

    val fromVariant = message.getVariant(fromVersion)!!
    val fromVariantType = fromVariant.toMcType()
    val toVariant = message.getVariant(toVersion)!!
    val toVariantType = toVariant.toMcType()

    return if (toVariant.tailrec) {
        if (!fromVariant.tailrec) {
            throw CompileException("Cannot translate to tail recursive variant ${toVariant.className} from non-tail-recursive variant ${fromVariant.className}")
        }
        val nodes = mutableListOf<McNode>()
        val toVarId = VariableId.create()
        val curFromVarId = VariableId.create()
        val curToVarId = VariableId.create()
        val nextToVarId = VariableId.create()
        if (toVariant.fields.last().type.realType.hasName(toVariant.className)) {
            // the onlyIf variant of recursion
            if (!fromVariant.fields.last().type.realType.hasName(fromVariant.className)) {
                throw CompileException("Cannot translate to @OnlyIf tail recursive variant ${toVariant.className} from tail recursive non-@OnlyIf variant ${fromVariant.className}")
            }
            if (fromVariant.fields.last().name != toVariant.fields.last().name) {
                throw CompileException("Tail recursive field names do not match between ${fromVariant.className} and ${toVariant.className}")
            }

            nodes += McNode(StoreVariableStmtOp(toVarId, toVariantType, true),
                translateInner(message, fromVersion, toVersion, fromVarId, outerParamResolver, null)
            )
            nodes += McNode(StoreVariableStmtOp(curFromVarId, fromVariantType, true),
                McNode(LoadVariableOp(fromVarId, fromVariantType))
            )
            nodes += McNode(StoreVariableStmtOp(curToVarId, toVariantType, true),
                McNode(LoadVariableOp(toVarId, toVariantType))
            )
            nodes += McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("!=", fromVariantType, fromVariantType),
                    McNode(LoadFieldOp(fromVariantType, fromVariant.fields.last().name, fromVariantType),
                        McNode(LoadVariableOp(curFromVarId, fromVariantType))
                    ),
                    McNode(CstNullOp(fromVariantType))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(curFromVarId, fromVariantType, false),
                        McNode(LoadFieldOp(fromVariantType, fromVariant.fields.last().name, fromVariantType),
                            McNode(LoadVariableOp(curFromVarId, fromVariantType))
                        ),
                    ),
                    McNode(StoreVariableStmtOp(nextToVarId, toVariantType, true),
                        translateInner(message, fromVersion, toVersion, curFromVarId, outerParamResolver, null)
                    ),
                    McNode(StoreFieldStmtOp(toVariantType, toVariant.fields.last().name, toVariantType),
                        McNode(LoadVariableOp(curToVarId, toVariantType)),
                        McNode(LoadVariableOp(nextToVarId, toVariantType))
                    ),
                    McNode(StoreVariableStmtOp(curToVarId, toVariantType, false),
                        McNode(LoadVariableOp(nextToVarId, toVariantType))
                    )
                )
            )
        } else {
            // the polymorphic variant of tail recursion
            val methodHandleType = McType.DeclaredType(CommonClassNames.METHOD_HANDLE)
            val currentMethodHandle = VariableId.create()
            val nextMethodHandle = VariableId.create()
            val nextFromVarId = VariableId.create()

            fun handleSubtype(fromSubtype: MessageVariantInfo, toSubtype: MessageVariantInfo, subtypeFromVarId: VariableId): McNode {
                return if (toSubtype.fields.lastOrNull()?.type?.realType?.hasName(toVariant.className) == true) {
                    if (fromSubtype.fields.lastOrNull()?.type?.realType?.hasName(fromVariant.className) != true) {
                        throw CompileException("Polymorphic tail recursive child ${fromSubtype.className} is not recursive, but is required to be by ${toSubtype.className}")
                    }
                    if (fromSubtype.fields.last().name != toSubtype.fields.last().name) {
                        throw CompileException("Polymorphic tail recursive child ${fromSubtype.className} does not have the same recursive field name as ${toSubtype.className}")
                    }

                    val fromSubtypeType = fromSubtype.toMcType()
                    val tailRecurseField = createTailRecurseField(toSubtype.className, toSubtype.fields.last().name, toVariant.className)
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(nextFromVarId, fromVariantType, false),
                            McNode(LoadFieldOp(fromSubtypeType, fromSubtype.fields.last().name, fromVariantType),
                                McNode(LoadVariableOp(subtypeFromVarId, fromSubtypeType))
                            )
                        ),
                        McNode(StoreVariableStmtOp(nextMethodHandle, methodHandleType, false),
                            McNode(LoadFieldOp(McType.DeclaredType(className), tailRecurseField, methodHandleType, isStatic = true))
                        )
                    )
                } else {
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(nextFromVarId, fromVariantType, false),
                            McNode(CstNullOp(fromVariantType))
                        )
                    )
                }
            }

            nodes += McNode(StoreVariableStmtOp(nextMethodHandle, methodHandleType, true),
                McNode(CstNullOp(methodHandleType))
            )
            nodes += McNode(DeclareVariableOnlyStmtOp(nextFromVarId, fromVariantType))
            nodes += McNode(StoreVariableStmtOp(toVarId, toVariantType, true),
                translateInner(message, fromVersion, toVersion, fromVarId, outerParamResolver, ::handleSubtype)
            )
            nodes += McNode(StoreVariableStmtOp(curToVarId, toVariantType, true),
                McNode(LoadVariableOp(toVarId, toVariantType))
            )
            nodes += McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("!=", fromVariantType, fromVariantType),
                    McNode(LoadVariableOp(nextFromVarId, fromVariantType)),
                    McNode(CstNullOp(fromVariantType))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(curFromVarId, fromVariantType, true),
                        McNode(LoadVariableOp(nextFromVarId, fromVariantType))
                    ),
                    McNode(StoreVariableStmtOp(currentMethodHandle, methodHandleType, true),
                        McNode(LoadVariableOp(nextMethodHandle, methodHandleType))
                    ),
                    McNode(StoreVariableStmtOp(nextToVarId, toVariantType, true),
                        translateInner(message, fromVersion, toVersion, curFromVarId, outerParamResolver, ::handleSubtype)
                    ),
                    McNode(PopStmtOp,
                        McNode(FunctionCallOp(CommonClassNames.METHOD_HANDLE, "invoke", listOf(methodHandleType, toVariantType, toVariantType), McType.VOID, true, isStatic = false, throwsException = true),
                            McNode(LoadVariableOp(currentMethodHandle, methodHandleType)),
                            McNode(LoadVariableOp(curToVarId, toVariantType)),
                            McNode(LoadVariableOp(nextToVarId, toVariantType))
                        )
                    )
                )
            )
        }
        nodes += McNode(ReturnStmtOp(toVariantType), McNode(LoadVariableOp(toVarId, toVariantType)))
        McNode(StmtListOp, nodes)
    } else {
        translateInner(message, fromVersion, toVersion, fromVarId, outerParamResolver, null)
    }
}

private fun ProtocolCompiler.translateInner(
    message: MessageInfo,
    fromVersion: Int,
    toVersion: Int,
    fromVarId: VariableId,
    outerParamResolver: ParamResolver?,
    extraSubtypeInstructions: ((MessageVariantInfo, MessageVariantInfo, VariableId) -> McNode)?
): McNode {
    val messageType = message.toMcType()

    val clientbound = toVersion > fromVersion
    val fromVariant = message.getVariant(fromVersion)!!
    val fromVariantType = fromVariant.toMcType()
    val toVariant = message.getVariant(toVersion)!!

    val castedFromVarId = VariableId.create()
    val nodes = mutableListOf<McNode>()
    nodes += McNode(StoreVariableStmtOp(castedFromVarId, fromVariantType, true),
        McNode(CastOp(messageType, fromVariantType),
            McNode(LoadVariableOp(fromVarId, messageType))
        )
    )

    val vars = mutableMapOf<String, VariableId>()

    fun handleField(
        field: McField,
        containingMessage: MessageVariantInfo,
        fieldFromVariant: MessageVariantInfo,
        fieldFromVarId: VariableId,
        isTailRecursive: Boolean,
        vars: MutableMap<String, VariableId>,
    ): McNode {
        val fieldFromVariantType = fieldFromVariant.toMcType()
        val fieldVarId = VariableId.create()
        vars[field.name] = fieldVarId
        val introduceInfo: IntroduceInfo? = if (fromVariant.className == toVariant.className) {
            null
        } else {
            field.type.getIntroduceInfo(clientbound)
        }
        val fieldCreationNode = when (introduceInfo) {
            is IntroduceInfo.Constant<*> -> generateConstantGraph(field.type.realType, introduceInfo.value, field.type.registry)
            is IntroduceInfo.DefaultConstruct -> generateDefaultConstructGraph(
                containingMessage,
                field.type,
                isTailRecursive,
                combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!, type)) }
            )
            is IntroduceInfo.Compute -> generateFunctionCallGraph(
                containingMessage.findFunction(introduceInfo.value),
                paramResolver = combineParamResolvers(outerParamResolver) { name, type ->
                    // TODO: type checking?
                    McNode(LoadFieldOp(fieldFromVariantType, name, fieldFromVariant.findField(name).type.realType),
                        McNode(LoadVariableOp(fieldFromVarId, fieldFromVariantType))
                    )
                },
                argTranslator = argTranslator@{ type, argNode ->
                    val argNodes = mutableListOf<McNode>()
                    val argVarId = VariableId.create()
                    argNodes += McNode(StoreVariableStmtOp(argVarId, type, true), argNode)
                    val argTranslateNode = when (val classInfo = type.classInfoOrNull) {
                        is MessageInfo -> McNode(ImplicitCastOp(classInfo.getVariant(toVersion)!!.toMcType(), classInfo.toMcType()), translate(classInfo, fromVersion, toVersion, argVarId,
                            combineParamResolvers(outerParamResolver) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) }
                        ))
                        is MessageVariantInfo -> translate(classInfo, fromVersion, toVersion, argVarId,
                            combineParamResolvers(outerParamResolver) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) }
                        )
                        else -> return@argTranslator argNode
                    }
                    argNodes += McNode(ReturnStmtOp(type), argTranslateNode)
                    McNode(StmtListOp, argNodes)
                }
            )
            null -> {
                val fromField = fieldFromVariant.findFieldOrNull(field.name)
                    ?: throw CompileException("Automatic variant conversion transfer not possible, field \"${field.name}\" not found in \"${fieldFromVariant.className}\", and @Introduce is not specified")
                val fieldLoadNode = McNode(LoadFieldOp(fieldFromVariantType, fromField.name, fromField.type.realType),
                    McNode(LoadVariableOp(fieldFromVarId, fieldFromVariantType))
                )
                autoTransfer(
                    fieldLoadNode,
                    field.type.realType,
                    fromVersion,
                    toVersion,
                    combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!, type)) }
                )
            }
        }
        return McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true), fieldCreationNode)
    }

    if (toVariant.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(toVariant.polymorphicParent)
        for (field in parentInfo.fields) {
            nodes += handleField(field, parentInfo, fromVariant, castedFromVarId, false, vars)
        }
    }

    for (field in toVariant.fields) {
        nodes += handleField(field, toVariant, fromVariant, castedFromVarId, toVariant.tailrec && field.type.realType.hasName(toVariant.className), vars)
    }

    val resultVarId = VariableId.create()

    val instantiationNode = if (toVariant.polymorphic != null && toVariant.polymorphicParent == null) {
        val typeField = toVariant.fields.first()
        generatePolymorphicInstantiationGraph(
            toVariant,
            typeField.type,
            McNode(LoadVariableOp(vars[typeField.name]!!, typeField.type.realType)),
            combineParamResolvers(outerParamResolver) { name, type -> McNode(LoadVariableOp(vars[name]!!, type)) }
        ) { childVariant, childVarId ->
            val childVariantOf = childVariant.variantOf
                ?: throw CompileException("Polymorphic subclass \"${childVariant.className}\" of multi-variant message \"${toVariant.className}\" is not itself multi-variant")
            val fromChildVariant = (getClassInfo(childVariantOf) as MessageInfo).getVariant(fromVersion)
                ?: throw CompileException("Polymorphic subclass \"${childVariant.className}\" of multi-variant message \"${toVariant.className}\" does not have a variant for protocol $fromVersion")
            val fromChildVariantType = fromChildVariant.toMcType()
            val castedFromChildVarId = VariableId.create()
            val childNodes = mutableListOf<McNode>()
            val childVars = vars.toMutableMap()

            childNodes += McNode(StoreVariableStmtOp(castedFromChildVarId, fromChildVariantType, true),
                McNode(CastOp(fromVariantType, fromChildVariantType), McNode(LoadVariableOp(fromVarId, fromVariantType)))
            )

            for (field in childVariant.fields) {
                childNodes += handleField(field, childVariant, fromChildVariant, castedFromChildVarId, toVariant.tailrec && field.type.realType.hasName(toVariant.className), childVars)
                val fieldVarId = childVars[field.name]!!
                childNodes += McNode(StoreFieldStmtOp(childVariant.toMcType(), field.name, field.type.realType),
                    McNode(LoadVariableOp(childVarId, childVariant.toMcType())),
                    McNode(LoadVariableOp(fieldVarId, field.type.realType))
                )
            }

            if (extraSubtypeInstructions != null) {
                childNodes += extraSubtypeInstructions(fromChildVariant, childVariant, castedFromChildVarId)
            }

            McNode(StmtListOp, childNodes)
        }
    } else {
        McNode(NewOp(toVariant.className, listOf()))
    }

    nodes += McNode(StoreVariableStmtOp(resultVarId, toVariant.toMcType(), true), instantiationNode)

    for ((name, varId) in vars) {
        val type = toVariant.findField(name).type.realType
        nodes += McNode(StoreFieldStmtOp(toVariant.toMcType(), name, type),
            McNode(LoadVariableOp(resultVarId, toVariant.toMcType())),
            McNode(LoadVariableOp(varId, type))
        )
    }

    nodes += McNode(ReturnStmtOp(toVariant.toMcType()),
        McNode(LoadVariableOp(resultVarId, toVariant.toMcType()))
    )

    return McNode(StmtListOp, nodes)
}

private fun ProtocolCompiler.autoTransfer(
    node: McNode,
    type: McType,
    fromProtocol: Int,
    toProtocol: Int,
    outerParamResolver: ParamResolver?,
): McNode {
    if (type is McType.PrimitiveType) {
        return type.cast(node)
    }

    val oldType = node.op.returnType
    if (type == oldType) {
        val needsTranslation = when (val classInfo = type.deepComponentType().classInfoOrNull) {
            is MessageInfo -> classInfo.needsTranslation(fromProtocol, toProtocol)
            is MessageVariantInfo -> classInfo.needsTranslation(fromProtocol, toProtocol)
            else -> false
        }
        if (!needsTranslation) {
            return node
        }
    }

    if (type.isOptional && oldType.isOptional) {
        if (type.hasName(OPTIONAL) && oldType.hasName(OPTIONAL)) {
            val functionType = McType.DeclaredType("java.util.function.Function")
            val lambdaParam = VariableId.create()
            return McNode(FunctionCallOp(OPTIONAL, "map", listOf(functionType), type, false, isStatic = false),
                node,
                McNode(LambdaOp(functionType, type.componentType(), listOf(oldType.componentType()), listOf(lambdaParam)),
                    autoTransfer(McNode(LoadVariableOp(lambdaParam, oldType.componentType())), type.componentType(), fromProtocol, toProtocol, outerParamResolver)
                )
            )
        }
        if ((type.hasName(OPTIONAL_INT) && oldType.hasName(OPTIONAL_LONG))
            || (type.hasName(OPTIONAL_LONG) && oldType.hasName(OPTIONAL_INT))
        ) {
            val result = VariableId.create()

            return McNode(StmtListOp,
                McNode(DeclareVariableOnlyStmtOp(result, type)),
                McNode(IfElseStmtOp,
                    McNode(FunctionCallOp(oldType.name, "isPresent", listOf(oldType), McType.BOOLEAN, false, isStatic = false),
                        node
                    ),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(result, type, false),
                            McNode(FunctionCallOp(type.name, "of", listOf(type.componentType()), type, false),
                                (type.componentType() as McType.PrimitiveType).cast(
                                    McNode(FunctionCallOp(oldType.name, if (oldType.hasName(OPTIONAL_INT)) "getAsInt" else "getAsLong", listOf(oldType), oldType.componentType(), false, isStatic = false),
                                        node
                                    )
                                )
                            )
                        )
                    ),
                    McNode(StmtListOp,
                        McNode(StoreVariableStmtOp(result, type, false),
                            McNode(FunctionCallOp(type.name, "empty", listOf(), type, false))
                        )
                    )
                ),
                McNode(ReturnStmtOp(type), McNode(LoadVariableOp(result, type)))
            )
        }
    }

    if ((type.isList || type is McType.ArrayType) && (oldType.isList || oldType is McType.ArrayType)) {
        val lengthVar = VariableId.create()
        val indexVar = VariableId.create()
        val resultVar = VariableId.create()

        val loadFromOldNode = if (oldType is McType.ArrayType) {
            McNode(LoadArrayOp(oldType.elementType),
                node,
                McNode(LoadVariableOp(indexVar, McType.INT))
            )
        } else {
            val getName = when ((oldType as McType.DeclaredType).name) {
                LIST -> "get"
                INT_LIST -> "getInt"
                LONG_LIST -> "getLong"
                else -> throw CompileException("Invalid list")
            }
            McNode(FunctionCallOp(oldType.name, getName, listOf(oldType, McType.INT), oldType.componentType(), false, isStatic = false),
                node,
                McNode(LoadVariableOp(indexVar, McType.INT))
            )
        }

        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(lengthVar, McType.INT, true),
                McNode(
                    if (oldType is McType.ArrayType) {
                        LoadFieldOp(oldType, "length", McType.INT)
                    } else {
                        FunctionCallOp((oldType as McType.DeclaredType).name, "size", listOf(oldType), McType.INT, false, isStatic = false)
                    },
                    node
                )
            ),
            McNode(StoreVariableStmtOp(resultVar, type, true),
                if (type is McType.ArrayType) {
                    McNode(NewArrayOp(type.componentType()), McNode(LoadVariableOp(lengthVar, McType.INT)))
                } else {
                    val concreteType = when ((type as McType.DeclaredType).name) {
                        LIST -> ARRAY_LIST
                        INT_LIST -> INT_ARRAY_LIST
                        LONG_LIST -> LONG_ARRAY_LIST
                        else -> throw CompileException("Invalid list")
                    }
                    McNode(ImplicitCastOp(McType.DeclaredType(concreteType), type),
                        McNode(NewOp(concreteType, listOf(McType.INT)),
                            McNode(LoadVariableOp(lengthVar, McType.INT))
                        )
                    )
                }
            ),
            McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                ),
                McNode(StmtListOp,
                    if (type is McType.ArrayType) {
                        McNode(StoreArrayStmtOp(type.elementType),
                            McNode(LoadVariableOp(resultVar, type)),
                            McNode(LoadVariableOp(indexVar, McType.INT)),
                            autoTransfer(loadFromOldNode, type.elementType, fromProtocol, toProtocol, outerParamResolver)
                        )
                    } else {
                        McNode(PopStmtOp,
                            McNode(FunctionCallOp((type as McType.DeclaredType).name, "add", listOf(type, type.componentType()), McType.VOID, true, isStatic = false),
                                McNode(LoadVariableOp(resultVar, type)),
                                autoTransfer(loadFromOldNode, type.componentType(), fromProtocol, toProtocol, outerParamResolver)
                            )
                        )
                    },
                    McNode(StoreVariableStmtOp(indexVar, McType.INT, false, "+="),
                        McNode(CstIntOp(1))
                    )
                )
            ),
            McNode(ReturnStmtOp(type), McNode(LoadVariableOp(resultVar, type)))
        )
    }

    val oldInfo = oldType.classInfoOrNull
    val info = type.classInfoOrNull
    if (oldInfo != null && info != null) {
        val oldGroup = (oldInfo as? MessageVariantInfo)?.variantOf ?: oldInfo.className
        val group = (info as? MessageVariantInfo)?.variantOf ?: info.className
        if (group == oldGroup) {
            val variableId = VariableId.create()
            return McNode(StmtListOp,
                McNode(StoreVariableStmtOp(variableId, oldType, true), node),
                when (val groupInfo = getClassInfo(group)) {
                    is MessageVariantInfo -> translate(groupInfo, fromProtocol, toProtocol, variableId, outerParamResolver)
                    is MessageInfo -> McNode(ReturnStmtOp(groupInfo.toMcType()),
                        McNode(ImplicitCastOp(groupInfo.getVariant(toProtocol)!!.toMcType(), groupInfo.toMcType()),
                            translate(groupInfo, fromProtocol, toProtocol, variableId, outerParamResolver)
                        )
                    )
                    else -> throw CompileException("Invalid groupInfo")
                }
            )
        }
    }

    throw CompileException("Cannot perform automatic transfer from $oldType to $type")
}
