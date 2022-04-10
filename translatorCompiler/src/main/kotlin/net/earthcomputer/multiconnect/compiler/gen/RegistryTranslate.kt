package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.CommonClassNames.IDENTIFIER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.LONG_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OPTIONAL
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_INTRINSICS
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.FieldType
import net.earthcomputer.multiconnect.compiler.McField
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.RegistryEntry
import net.earthcomputer.multiconnect.compiler.byId
import net.earthcomputer.multiconnect.compiler.byName
import net.earthcomputer.multiconnect.compiler.classInfoOrNull
import net.earthcomputer.multiconnect.compiler.componentType
import net.earthcomputer.multiconnect.compiler.deepComponentType
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.hasName
import net.earthcomputer.multiconnect.compiler.isIntegral
import net.earthcomputer.multiconnect.compiler.isList
import net.earthcomputer.multiconnect.compiler.isOptional
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CastOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstNullOp
import net.earthcomputer.multiconnect.compiler.node.DoWhileStmtOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.IfStmtOp
import net.earthcomputer.multiconnect.compiler.node.InstanceOfOp
import net.earthcomputer.multiconnect.compiler.node.LambdaOp
import net.earthcomputer.multiconnect.compiler.node.LoadArrayOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreArrayStmtOp
import net.earthcomputer.multiconnect.compiler.node.StoreFieldStmtOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.WhileStmtOp
import net.earthcomputer.multiconnect.compiler.normalizeIdentifier
import net.earthcomputer.multiconnect.compiler.opto.optimize
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.readCsv
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

private fun ProtocolCompiler.hasChanged(registry: Registries, wireType: Types): Boolean {
    val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${registry.name.lowercase()}.csv"))
    val newEntries = readCsv<RegistryEntry>(latestDataDir.resolve("${registry.name.lowercase()}.csv"))
    return if (wireType.isIntegral) {
        oldEntries.any { newEntries.byId(it.id)?.name != it.name }
    } else {
        oldEntries.any { it.name != it.oldName }
    }
}

private fun ProtocolCompiler.needsTranslating(field: FieldType): Boolean {
    if (field.registry != null) {
        if (hasChanged(field.registry, field.wireType)) {
            return true
        }
    }
    val messageInfo = when (val classInfo = field.realType.deepComponentType().classInfoOrNull) {
        is MessageVariantInfo -> classInfo
        is MessageInfo -> classInfo.getVariant(protocols[0].id) ?: throw CompileException("Message ${classInfo.className} does not have variant for latest version")
        else -> return false
    }

    return needsTranslating(messageInfo)
}

private fun ProtocolCompiler.needsTranslating(messageInfo: MessageVariantInfo): Boolean {
    if (messageInfo.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(messageInfo.polymorphicParent)
        if (parentInfo.fields.any { needsTranslating(it.type) }) {
            return true
        }
    }

    if (messageInfo.fields.any {
        if (messageInfo.tailrec && it.type.realType.hasName(messageInfo.className)) {
            return@any false
        }
        needsTranslating(it.type)
    }) {
        return true
    }

    if (messageInfo.polymorphic != null && messageInfo.polymorphicParent == null) {
        for (polymorphicChild in polymorphicChildren[messageInfo.className]!!) {
            val childInfo = getMessageVariantInfo(polymorphicChild)
            if (childInfo.fields.any {
                if (messageInfo.tailrec && it.type.realType.hasName(messageInfo.className)) {
                    return@any false
                }
                if (childInfo.tailrec && it.type.realType.hasName(childInfo.className)) {
                    return@any false
                }
                needsTranslating(it.type)
            }) {
                return true
            }
        }
    }

    return false
}

private fun ProtocolCompiler.fixRegistries(field: FieldType, fieldVar: VariableId, clientbound: Boolean): McNode {
    return fixRegistriesWithType(field, field.realType, fieldVar, clientbound)
}

private fun ProtocolCompiler.fixRegistriesWithType(
    field: FieldType,
    type: McType,
    varId: VariableId,
    clientbound: Boolean
): McNode {
    if (type.isOptional) {
        val functionType = McType.DeclaredType("java.util.function.Function")
        val isOptional = (type as McType.DeclaredType).name == OPTIONAL
        val innerVarId = VariableId.create()
        return McNode(FunctionCallOp(if (isOptional) OPTIONAL else PACKET_INTRINSICS, "map", listOf(type, functionType), type, false, isStatic = !isOptional),
            McNode(LoadVariableOp(varId, type)),
            McNode(LambdaOp(functionType, type.componentType(), listOf(type.componentType()), listOf(innerVarId)),
                fixRegistriesWithType(field, type.componentType(), innerVarId, clientbound)
            )
        )
    }

    if (type.isList) {
        val lengthVar = VariableId.create()
        val indexVar = VariableId.create()
        val elementVar = VariableId.create()
        val getName = when ((type as McType.DeclaredType).name) {
            LIST -> "get"
            INT_LIST -> "getInt"
            LONG_LIST -> "getLong"
            else -> throw CompileException("Invalid list")
        }
        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(lengthVar, McType.INT, true),
                McNode(FunctionCallOp(type.name, "size", listOf(type), McType.INT, false, isStatic = false),
                    McNode(LoadVariableOp(varId, type))
                )
            ),
            McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(elementVar, type.componentType(), true),
                        McNode(FunctionCallOp(type.name, getName, listOf(type, McType.INT), type.componentType(), false, isStatic = false),
                            McNode(LoadVariableOp(varId, type)),
                            McNode(LoadVariableOp(indexVar, McType.INT))
                        )
                    ),
                    McNode(PopStmtOp,
                        McNode(FunctionCallOp(type.name, "set", listOf(type, McType.INT, type.componentType()), McType.VOID, true, isStatic = false),
                            McNode(LoadVariableOp(varId, type)),
                            McNode(LoadVariableOp(indexVar, McType.INT)),
                            fixRegistriesWithType(field, type.componentType(), elementVar, clientbound)
                        )
                    )
                )
            ),
            McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))
        )
    }

    if (type is McType.ArrayType) {
        val lengthVar = VariableId.create()
        val indexVar = VariableId.create()
        val elementVar = VariableId.create()
        return McNode(StmtListOp,
            McNode(StoreVariableStmtOp(lengthVar, McType.INT, true),
                McNode(LoadFieldOp(type, "length", McType.INT),
                    McNode(LoadVariableOp(varId, type))
                )
            ),
            McNode(StoreVariableStmtOp(indexVar, McType.INT, true), McNode(CstIntOp(0))),
            McNode(WhileStmtOp,
                McNode(BinaryExpressionOp("<", McType.INT, McType.INT),
                    McNode(LoadVariableOp(indexVar, McType.INT)),
                    McNode(LoadVariableOp(lengthVar, McType.INT))
                ),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(elementVar, type.elementType, true),
                        McNode(LoadArrayOp(type.elementType),
                            McNode(LoadVariableOp(varId, type)),
                            McNode(LoadVariableOp(indexVar, McType.INT))
                        )
                    ),
                    McNode(StoreArrayStmtOp(type.elementType),
                        McNode(LoadVariableOp(varId, type)),
                        McNode(LoadVariableOp(indexVar, McType.INT)),
                        fixRegistriesWithType(field, type.elementType, elementVar, clientbound)
                    )
                )
            ),
            McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))
        )
    }

    when (val classInfo = type.classInfoOrNull) {
        is MessageInfo -> {
            return McNode(StmtListOp,
                fixRegistries(classInfo, varId, clientbound),
                McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))
            )
        }
        is MessageVariantInfo -> return McNode(StmtListOp,
            fixRegistries(classInfo, varId, clientbound),
            McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))
        )
        else -> {}
    }

    if (type.isIntegral) {
        type as McType.PrimitiveType
        val registry = field.registry ?: throw CompileException("Should not have thought registries need fixing for integral type without a registry")
        return type.cast(McNode(FunctionCallOp(className, createIntRemapFunc(registry, clientbound), listOf(McType.INT), McType.INT, false),
            McType.INT.cast(McNode(LoadVariableOp(varId, type)))
        ))
    }

    if (type.hasName(IDENTIFIER)) {
        val registry = field.registry ?: throw CompileException("Should not have thought registries need fixing for Identifier type without a registry")
        return McNode(FunctionCallOp(className, createStringRemapFunc(registry, clientbound), listOf(type), type, false),
            McNode(LoadVariableOp(varId, type))
        )
    }

    throw CompileException("Don't know how to fix registries for type $type")
}

private fun ProtocolCompiler.createIntRemapFunc(registry: Registries, clientbound: Boolean): String {
    fun selectShift(shifts: List<Pair<Int, Int?>>, default: Int): McNode {
        val middleIndex = shifts.size / 2
        val middleShift = shifts[middleIndex].second
        var delta = 0
        val maxDelta = max(middleIndex, shifts.size - middleIndex)
        while (delta < maxDelta) {
            val index = middleIndex + delta
            if (index >= 0 && index < shifts.size) {
                val (id, shift) = shifts[index]
                if (shift != middleShift) {
                    return McNode(StmtListOp, McNode(IfElseStmtOp,
                        McNode(BinaryExpressionOp(if (delta < 0) "<=" else "<", McType.INT, McType.INT),
                            McNode(LoadVariableOp(VariableId.immediate("value"), McType.INT)),
                            McNode(CstIntOp(id))
                        ),
                        selectShift(shifts.subList(0, if (delta < 0) index + 1 else index), default),
                        selectShift(shifts.subList(if (delta < 0) index + 1 else index, shifts.size), default)
                    ))
                }
            }
            delta = if (delta > 0) -delta else -delta + 1
        }
        return when (middleShift) {
            null -> {
                McNode(StmtListOp, McNode(ReturnStmtOp(McType.INT), McNode(CstIntOp(default))))
            }
            0 -> {
                McNode(StmtListOp, McNode(ReturnStmtOp(McType.INT),
                    McNode(LoadVariableOp(VariableId.immediate("value"), McType.INT))
                ))
            }
            else -> {
                McNode(StmtListOp, McNode(ReturnStmtOp(McType.INT),
                    McNode(BinaryExpressionOp(if (middleShift < 0) "-" else "+", McType.INT, McType.INT),
                        McNode(LoadVariableOp(VariableId.immediate("value"), McType.INT)),
                        McNode(CstIntOp(abs(middleShift)))
                    )
                ))
            }
        }
    }

    fun buildNode(): McNode {
        val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${registry.name.lowercase()}.csv"))
        val newEntries = readCsv<RegistryEntry>(latestDataDir.resolve("${registry.name.lowercase()}.csv"))
        val (fromEntries, toEntries) = if (clientbound) {
            Pair(oldEntries, newEntries)
        } else {
            Pair(newEntries, oldEntries)
        }

        val shifts = mutableListOf<Pair<Int, Int?>>()

        for (fromEntry in fromEntries) {
            val newId = toEntries.byName(fromEntry.name)?.id
            if (newId == null && clientbound) {
                throw CompileException("No value for ${fromEntry.name} in latest ${registry.name} registry")
            }
            shifts += fromEntry.id to newId?.let { it - fromEntry.id }
        }

        shifts.sortBy { it.first }

        return selectShift(shifts, toEntries[0].id)
    }

    val methodName = "remap%sInt%s".format(Locale.ROOT,
        if (clientbound) "S" else "C",
        registry.name.lowercase().replaceFirstChar { it.uppercase() }
    )

    cacheMembers[methodName] = { emitter ->
        emitter.append("private static int ").append(methodName).append("(int value) {").indent().appendNewLine()
        buildNode().optimize().emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("}")
    }

    return methodName
}

private fun ProtocolCompiler.createStringRemapFunc(registry: Registries, clientbound: Boolean): String {
    val identifierType = McType.DeclaredType(IDENTIFIER)

    fun buildNode(): McNode {
        val oldEntries = readCsv<RegistryEntry>(dataDir.resolve("${registry.name.lowercase()}.csv"))
        val newEntries = readCsv<RegistryEntry>(latestDataDir.resolve("${registry.name.lowercase()}.csv"))
        val (fromEntries, toEntries) = if (clientbound) {
            Pair(oldEntries, newEntries)
        } else {
            Pair(newEntries, oldEntries)
        }

        val resultToInputs = mutableMapOf<String, MutableList<String>>()

        for (fromEntry in fromEntries) {
            val newEntry = toEntries.byName(fromEntry.name) ?: if (clientbound) {
                throw CompileException("No value for ${fromEntry.name} in latest ${registry.name} registry")
            } else {
                toEntries.first()
            }
            if (newEntry.oldName != fromEntry.oldName) {
                resultToInputs.computeIfAbsent(newEntry.oldName.normalizeIdentifier()) { mutableListOf() } += fromEntry.oldName.normalizeIdentifier()
            }
        }

        val (results, cases) = resultToInputs.asSequence()
            .map { (first, second) -> Pair(first, SwitchOp.GroupCase(second.toSortedSet())) }
            .sortedBy { it.second }
            .unzip()

        val resultNodes = results.map { McNode(LoadFieldOp(McType.DeclaredType(className), createIdentifierConstantField(it), identifierType, isStatic = true)) }

        val inputNode = McNode(FunctionCallOp(IDENTIFIER, "toString", listOf(identifierType), McType.STRING, false, isStatic = false),
            McNode(LoadVariableOp(VariableId.immediate("value"), identifierType))
        )

        return McNode(StmtListOp,
            McNode(ReturnStmtOp(identifierType),
                McNode(SwitchOp(cases.toSortedSet(), true, McType.STRING, identifierType),
                    (listOf(inputNode)
                    + resultNodes
                    + listOf(McNode(LoadVariableOp(VariableId.immediate("value"), identifierType)))).toMutableList()
                )
            )
        )
    }

    val methodName = "remap%sIdentifier%s".format(Locale.ROOT,
        if (clientbound) "S" else "C",
        registry.name.lowercase().replaceFirstChar { it.uppercase() }
    )

    cacheMembers[methodName] = { emitter ->
        emitter.append("private static ")
        identifierType.emit(emitter)
        emitter.append(" ").append(methodName).append("(")
        identifierType.emit(emitter)
        emitter.append(" value) {").indent().appendNewLine()
        buildNode().optimize().emit(emitter, Precedence.COMMA)
        emitter.dedent().appendNewLine().append("}")
    }

    return methodName
}

internal fun ProtocolCompiler.fixRegistries(messageInfo: MessageVariantInfo, messageVar: VariableId, clientbound: Boolean): McNode {
    if (!needsTranslating(messageInfo)) {
        return McNode(StmtListOp)
    }

    val messageType = messageInfo.toMcType()

    return if (messageInfo.tailrec) {
        if (messageInfo.fields.last().type.realType.hasName(messageInfo.className)) {
            // the onlyIf variant of recursion
            val currentVarId = VariableId.create()
            McNode(StmtListOp,
                McNode(StoreVariableStmtOp(currentVarId, messageType, true),
                    McNode(LoadVariableOp(messageVar, messageType))
                ),
                McNode(DoWhileStmtOp,
                    McNode(StmtListOp,
                        fixRegistriesInner(messageInfo, currentVarId, clientbound, null),
                        McNode(StoreVariableStmtOp(currentVarId, messageType, false),
                            McNode(LoadFieldOp(messageType, messageInfo.fields.last().name, messageType),
                                McNode(LoadVariableOp(currentVarId, messageType))
                            )
                        )
                    ),
                    McNode(BinaryExpressionOp("!=", messageType, messageType),
                        McNode(LoadVariableOp(currentVarId, messageType)),
                        McNode(CstNullOp(messageType))
                    )
                )
            )
        } else {
            // the polymorphic variant of recursion
            val currentVarId = VariableId.create()
            McNode(StmtListOp,
                McNode(StoreVariableStmtOp(currentVarId, messageType, true),
                    McNode(LoadVariableOp(messageVar, messageType))
                ),
                McNode(DoWhileStmtOp,
                    fixRegistriesInner(messageInfo, currentVarId, clientbound, currentVarId),
                    McNode(BinaryExpressionOp("!=", messageType, messageType),
                        McNode(LoadVariableOp(currentVarId, messageType)),
                        McNode(CstNullOp(messageType))
                    )
                )
            )
        }
    } else {
        fixRegistriesInner(messageInfo, messageVar, clientbound, null)
    }
}

private fun ProtocolCompiler.fixRegistriesInner(
    messageInfo: MessageVariantInfo,
    messageVar: VariableId,
    clientbound: Boolean,
    outSubtypeVar: VariableId?
): McNode {
    val messageType = messageInfo.toMcType()

    val nodes = mutableListOf<McNode>()

    fun handleField(ownerVar: VariableId, ownerType: McType, field: McField, outNodes: MutableList<McNode>) {
        if (!needsTranslating(field.type)) {
            return
        }

        val fieldVar = VariableId.create()
        outNodes += McNode(StoreVariableStmtOp(fieldVar, field.type.realType, true),
            McNode(LoadFieldOp(ownerType, field.name, field.type.realType),
                McNode(LoadVariableOp(ownerVar, ownerType))
            )
        )
        outNodes += McNode(StoreFieldStmtOp(ownerType, field.name, field.type.realType),
            McNode(LoadVariableOp(ownerVar, ownerType)),
            fixRegistries(field.type, fieldVar, clientbound)
        )
    }

    if (messageInfo.polymorphicParent != null) {
        val parentInfo = getMessageVariantInfo(messageInfo.polymorphicParent)
        for (field in parentInfo.fields) {
            handleField(messageVar, messageType, field, nodes)
        }
    }

    for (field in messageInfo.fields) {
        if (messageInfo.tailrec && field.type.realType.hasName(messageInfo.className)) {
            continue
        }
        handleField(messageVar, messageType, field, nodes)
    }

    if (messageInfo.polymorphic != null && messageInfo.polymorphicParent == null) {
        var ifElseChain: McNode? = if (outSubtypeVar != null) {
            McNode(StoreVariableStmtOp(outSubtypeVar, messageType, false),
                McNode(CstNullOp(messageType))
            )
        } else {
            null
        }

        for (child in polymorphicChildren[messageInfo.className]!!.asReversed()) {
            val childInfo = getMessageVariantInfo(child)
            if (!needsTranslating(childInfo)) {
                continue
            }
            val childType = childInfo.toMcType()

            val conditionNode = McNode(InstanceOfOp(messageType, childType),
                McNode(LoadVariableOp(messageVar, messageType))
            )
            val childVarId = VariableId.create()
            val childNodes = mutableListOf<McNode>()
            childNodes += McNode(StoreVariableStmtOp(childVarId, childType, true),
                McNode(CastOp(messageType, childType), McNode(LoadVariableOp(messageVar, messageType)))
            )
            for (field in childInfo.fields) {
                if (messageInfo.tailrec && field.type.realType.hasName(messageInfo.className)) {
                    continue
                }
                handleField(childVarId, childType, field, childNodes)
            }
            if (outSubtypeVar != null) {
                val newSubtypeVal = if (childInfo.fields.lastOrNull()?.type?.realType?.hasName(messageInfo.className) == true) {
                    McNode(LoadFieldOp(childType, childInfo.fields.last().name, messageType),
                        McNode(LoadVariableOp(childVarId, childType))
                    )
                } else {
                    McNode(CstNullOp(messageType))
                }
                childNodes += McNode(StoreVariableStmtOp(outSubtypeVar, messageType, false), newSubtypeVal)
            }
            val trueNode = McNode(StmtListOp, childNodes)
            ifElseChain = if (ifElseChain != null) {
                McNode(IfElseStmtOp, conditionNode, trueNode, McNode(StmtListOp, ifElseChain))
            } else {
                McNode(IfStmtOp, conditionNode, trueNode)
            }
        }

        if (ifElseChain != null) {
            nodes += ifElseChain
        }
    }

    return McNode(StmtListOp, nodes)
}

internal fun ProtocolCompiler.fixRegistries(messageInfo: MessageInfo, messageVar: VariableId, clientbound: Boolean): McNode {
    val variant = messageInfo.getVariant(protocols[0].id)
        ?: throw CompileException("No latest variant of ${messageInfo.className}")
    if (!needsTranslating(variant)) {
        return McNode(StmtListOp)
    }

    val messageType = messageInfo.toMcType()
    val castedVarId = VariableId.create()
    return McNode(StmtListOp,
        McNode(StoreVariableStmtOp(castedVarId, variant.toMcType(), true),
            McNode(CastOp(messageType, variant.toMcType()), McNode(LoadVariableOp(messageVar, messageType)))
        ),
        fixRegistries(variant, castedVarId, clientbound)
    )
}
