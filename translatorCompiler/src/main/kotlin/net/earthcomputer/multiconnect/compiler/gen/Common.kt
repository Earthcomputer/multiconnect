package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.ap.Registries
import net.earthcomputer.multiconnect.compiler.ArgumentParameter
import net.earthcomputer.multiconnect.compiler.CommonClassNames
import net.earthcomputer.multiconnect.compiler.CommonClassNames.ARRAY_LIST
import net.earthcomputer.multiconnect.compiler.CommonClassNames.CLASS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.CONSUMER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.DELAYED_PACKET_SENDER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.IDENTIFIER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.INT_FUNCTION
import net.earthcomputer.multiconnect.compiler.CommonClassNames.MAP
import net.earthcomputer.multiconnect.compiler.CommonClassNames.NETWORK_HANDLER
import net.earthcomputer.multiconnect.compiler.CommonClassNames.OBJECT
import net.earthcomputer.multiconnect.compiler.CommonClassNames.PACKET_INTRINSICS
import net.earthcomputer.multiconnect.compiler.CommonClassNames.TO_INT_FUNCTION
import net.earthcomputer.multiconnect.compiler.CommonClassNames.TYPED_MAP
import net.earthcomputer.multiconnect.compiler.CompileException
import net.earthcomputer.multiconnect.compiler.DefaultConstruct
import net.earthcomputer.multiconnect.compiler.DefaultConstructedParameter
import net.earthcomputer.multiconnect.compiler.Either
import net.earthcomputer.multiconnect.compiler.EnumInfo
import net.earthcomputer.multiconnect.compiler.FieldType
import net.earthcomputer.multiconnect.compiler.FilledParameter
import net.earthcomputer.multiconnect.compiler.GlobalDataParameter
import net.earthcomputer.multiconnect.compiler.McFunction
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.Polymorphic
import net.earthcomputer.multiconnect.compiler.SuppliedDefaultConstructedParameter
import net.earthcomputer.multiconnect.compiler.classInfoOrNull
import net.earthcomputer.multiconnect.compiler.downcastConstant
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.hasName
import net.earthcomputer.multiconnect.compiler.isIntegral
import net.earthcomputer.multiconnect.compiler.messageVariantInfo
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CastOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.CstStringOp
import net.earthcomputer.multiconnect.compiler.node.DeclareVariableOnlyStmtOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.IfElseStmtOp
import net.earthcomputer.multiconnect.compiler.node.IfStmtOp
import net.earthcomputer.multiconnect.compiler.node.ImplicitCastOp
import net.earthcomputer.multiconnect.compiler.node.LambdaOp
import net.earthcomputer.multiconnect.compiler.node.LoadFieldOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.NewArrayOp
import net.earthcomputer.multiconnect.compiler.node.NewOp
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.Precedence
import net.earthcomputer.multiconnect.compiler.node.ReturnStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.StoreFieldStmtOp
import net.earthcomputer.multiconnect.compiler.node.StoreVariableStmtOp
import net.earthcomputer.multiconnect.compiler.node.SwitchOp
import net.earthcomputer.multiconnect.compiler.node.ThrowStmtOp
import net.earthcomputer.multiconnect.compiler.node.VariableId
import net.earthcomputer.multiconnect.compiler.node.createCstNode
import net.earthcomputer.multiconnect.compiler.normalizeIdentifier
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.protocolNamesById
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.splitPackageClass

typealias ParamResolver = (String, McType) -> McNode

internal fun combineParamResolvers(
    outer: ParamResolver?,
    inner: ParamResolver
): ParamResolver {
    if (outer == null) {
        return inner
    }

    return { name, type ->
        if (name.startsWith("outer.")) {
            outer(name.substring(6), type)
        } else {
            inner(name, type)
        }
    }
}

internal fun ProtocolCompiler.generatePolymorphicInstantiationGraph(
    message: MessageVariantInfo,
    type: FieldType,
    loadTypeField: McNode,
    paramResolver: ParamResolver,
    postConstruct: ((MessageVariantInfo, VariableId) -> McNode)?
): McNode {
    message.polymorphic!!
    val messageType = message.toMcType()

    fun construct(childMessage: MessageVariantInfo): McNode {
        val childType = childMessage.toMcType()
        return if (postConstruct == null) {
            McNode(
                ImplicitCastOp(childType, messageType),
                McNode(NewOp(childMessage.className, listOf()))
            )
        } else {
            val varId = VariableId.create()
            McNode(
                StmtListOp,
                McNode(StoreVariableStmtOp(varId, childType, true), McNode(NewOp(childMessage.className, listOf()))),
                postConstruct(childMessage, varId),
                McNode(
                    ReturnStmtOp(messageType),
                    McNode(
                        ImplicitCastOp(childType, messageType),
                        McNode(LoadVariableOp(varId, childType))
                    )
                )
            )
        }
    }

    val children = polymorphicChildren[message.className]?.map { getMessageVariantInfo(it) } ?: emptyList()

    if (type.realType == McType.BOOLEAN && children.size == 2
        && (children.all { it.polymorphic is Polymorphic.Constant<*> }
            || (children.any { it.polymorphic is Polymorphic.Constant<*> } && children.any { it.polymorphic is Polymorphic.Otherwise })
    )) {
        val trueChild = children.firstOrNull { (it.polymorphic as? Polymorphic.Constant<*>)?.value?.contains(true) == true }
            ?: children.firstOrNull { it.polymorphic is Polymorphic.Otherwise }
        val falseChild = children.firstOrNull { (it.polymorphic as? Polymorphic.Constant<*>)?.value?.contains(false) == true }
            ?: children.firstOrNull { it.polymorphic is Polymorphic.Otherwise }
        if (trueChild != null && falseChild != null && trueChild.className != falseChild.className) {
            val varId = VariableId.create()
            return McNode(StmtListOp,
                McNode(DeclareVariableOnlyStmtOp(varId, messageType)),
                McNode(IfElseStmtOp,
                    loadTypeField,
                    McNode(StmtListOp, McNode(StoreVariableStmtOp(varId, messageType, false), construct(trueChild))),
                    McNode(StmtListOp, McNode(StoreVariableStmtOp(varId, messageType, false), construct(falseChild)))
                ),
                McNode(ReturnStmtOp(messageType), McNode(LoadVariableOp(varId, messageType)))
            )
        }
    }

    fun hasRealConstant(polymorphic: Polymorphic?): Boolean {
        if (polymorphic !is Polymorphic.Constant<*>) {
            return false
        }
        if (type.registry == null || !type.realType.isIntegral) {
            return true
        }
        return polymorphic.value.any { it !is String || type.registry.getRawId(it) !is Either.Right }
    }
    fun hasNonConstant(polymorphic: Polymorphic?): Boolean {
        if (polymorphic !is Polymorphic.Constant<*>) {
            return true
        }
        if (type.registry == null || !type.realType.isIntegral) {
            return false
        }
        return polymorphic.value.any { it is String && type.registry.getRawId(it) is Either.Right }
    }

    val useSwitchExpression = type.realType != McType.BOOLEAN
            && type.realType != McType.FLOAT
            && type.realType != McType.DOUBLE
            && children.any { hasRealConstant(it.polymorphic) }
    val nonSwitchChildren: MutableList<MessageVariantInfo?> = if (useSwitchExpression) {
        children.filterTo(mutableListOf()) { hasNonConstant(it.polymorphic) }
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
        McNode(
            StmtListOp,
            nonSwitchChildren.sortedBy {
                when (it?.polymorphic) {
                    is Polymorphic.Constant<*> -> 0
                    is Polymorphic.Condition -> 1
                    is Polymorphic.Otherwise -> 2
                    null -> 2
                }
            }.mapTo(mutableListOf()) { childMessage ->
                when (val polymorphic = childMessage?.polymorphic) {
                    is Polymorphic.Constant<*> -> {
                        val constantNodes = if (type.registry != null) {
                            polymorphic.value.asSequence()
                                .filterIsInstance<String>()
                                .mapNotNull { type.registry.getRawId(it)?.rightOrNull() }
                                .map { (type.realType as McType.PrimitiveType).cast(it) }
                        } else {
                            polymorphic.value.asSequence().map {
                                val actualValue = if (type.realType == McType.FLOAT) {
                                    (it as? Double)?.toFloat() ?: it
                                } else {
                                    it
                                }
                                createCstNode(actualValue)
                            }
                        }
                        McNode(IfStmtOp,
                            constantNodes.map {
                                McNode(BinaryExpressionOp("==", type.realType, type.realType), loadTypeField, it)
                            }
                            .reduce { left, right ->
                                McNode(BinaryExpressionOp("||", McType.BOOLEAN, McType.BOOLEAN), left, right)
                            },
                            McNode(StmtListOp, McNode(ReturnStmtOp(messageType), construct(childMessage)))
                        )
                    }
                    is Polymorphic.Condition -> McNode(
                        IfStmtOp,
                        generateFunctionCallGraph(message.findFunction(polymorphic.value), loadTypeField, paramResolver = paramResolver),
                        McNode(StmtListOp, McNode(ReturnStmtOp(messageType), construct(childMessage)))
                    )
                    is Polymorphic.Otherwise -> McNode(ReturnStmtOp(messageType), construct(childMessage))
                    null -> McNode(
                        ThrowStmtOp,
                        McNode(
                            NewOp("java.lang.IllegalArgumentException", listOf(McType.STRING)),
                            createCstNode("Could not select polymorphic child of \"${splitPackageClass(message.className).second}\"")
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
        val (results, cases) =
            children
                .asSequence()
                .filter { it.polymorphic is Polymorphic.Constant<*> }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it to (it.polymorphic as Polymorphic.Constant<T>)
                }
                .map { (child, group) ->
                    child to when {
                        type.realType.hasName(IDENTIFIER) ->
                            group.value.map { (it as String).normalizeIdentifier() }
                        type.realType.isIntegral && type.registry != null ->
                            group.value.filter { it !is String || type.registry.getRawId(it) is Either.Left }.map { case ->
                                (case as? String)?.let {
                                    type.registry.getRawId(it)!!.left().downcastConstant(type.realType)
                                } ?: case
                            }
                        type.realType.classInfoOrNull is EnumInfo ->
                            group.value.map { SwitchOp.EnumCase(it as String) }
                        else -> group.value.map { it.downcastConstant(type.realType) }
                    }
                }
                .filter { it.second.isNotEmpty() }
                .map { (child, lst) ->
                    @Suppress("UNCHECKED_CAST")
                    child to SwitchOp.GroupCase((lst as List<T>).toSortedSet())
                }
                .sortedBy { (_, case) -> case }
                .unzip()
        return McNode(
            SwitchOp(
            cases.toSortedSet(),
            true,
            if (type.realType.hasName(IDENTIFIER)) { McType.STRING } else { type.realType },
            messageType
        ),
            (listOf(if (type.realType.hasName(IDENTIFIER)) {
                McNode(
                    FunctionCallOp(IDENTIFIER, "toString", listOf(type.realType), McType.STRING, false, isStatic = false),
                    loadTypeField
                )
            } else {
                loadTypeField
            }) + results.map(::construct) + listOf(defaultBranch)).toMutableList()
        )
    }
    return makeCases<String>() // use String as placeholder type argument
}



internal fun ProtocolCompiler.generateFunctionCallGraph(function: McFunction, vararg positionalArguments: McNode, paramResolver: ParamResolver): McNode {
    val loadParams = function.parameters.mapTo(mutableListOf()) { param ->
        when (param) {
            is ArgumentParameter -> paramResolver(param.name, param.paramType)
            is DefaultConstructedParameter -> generateDefaultConstructGraph(param.paramType, null)
            is SuppliedDefaultConstructedParameter -> McNode(
                LambdaOp(param.paramType, param.suppliedType, emptyList(), emptyList()),
                generateDefaultConstructGraph(param.suppliedType, null)
            )
            is FilledParameter -> generateFilledConstructGraph(param.paramType, param)
            is GlobalDataParameter -> generateGlobalDataGraph(param.paramType)
        }
    }
    val params = positionalArguments.toMutableList()
    params += loadParams
    return McNode(
        FunctionCallOp(function.owner, function.name, function.positionalParameters + function.parameters.map { it.paramType }, function.returnType, true),
        params
    )
}

internal fun ProtocolCompiler.generateDefaultConstructGraph(classInfo: MessageVariantInfo, outerResolver: ParamResolver?): McNode {
    val type = classInfo.toMcType()

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
                    generateDefaultConstructGraph(subclassInfo, outerResolver)
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
                    generateDefaultConstructGraph(parentInfo, field.type, false,
                        combineParamResolvers(outerResolver) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) })
                }
                nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true), valueNode)
                nodes += McNode(
                    StoreFieldStmtOp(type, field.name, field.type.realType),
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
        val valueNode = generateDefaultConstructGraph(classInfo, field.type, isTailRecursive,
            combineParamResolvers(outerResolver) { name, typ -> McNode(LoadVariableOp(vars[name]!!, typ)) })
        nodes += McNode(StoreVariableStmtOp(fieldVarId, field.type.realType, true), valueNode)
        nodes += McNode(
            StoreFieldStmtOp(type, field.name, field.type.realType),
            McNode(LoadVariableOp(varId, type)),
            McNode(LoadVariableOp(fieldVarId, field.type.realType))
        )
    }

    nodes += McNode(ReturnStmtOp(type), McNode(LoadVariableOp(varId, type)))

    return McNode(StmtListOp, nodes)
}

internal fun ProtocolCompiler.generateDefaultConstructGraph(type: McType, outerResolver: ParamResolver?): McNode {
    return when (val classInfo = type.classInfoOrNull) {
        is MessageVariantInfo -> generateDefaultConstructGraph(classInfo, outerResolver)
        is MessageInfo -> {
            val variantInfo = classInfo.getVariant(defaultConstructProtocolId ?: currentProtocolId)!!
            McNode(ImplicitCastOp(variantInfo.toMcType(), type as McType.DeclaredType),
                generateDefaultConstructGraph(variantInfo, outerResolver)
            )
        }
        is EnumInfo -> McNode(LoadFieldOp(type, classInfo.values.first(), type, isStatic = true))
        else -> when((type as? McType.DeclaredType)?.name) {
            CommonClassNames.OPTIONAL, CommonClassNames.OPTIONAL_INT, CommonClassNames.OPTIONAL_LONG -> {
                McNode(FunctionCallOp(type.name, "empty", listOf(), type, false))
            }
            CommonClassNames.LIST -> {
                McNode(ImplicitCastOp(McType.DeclaredType(ARRAY_LIST), type), McNode(NewOp(
                    ARRAY_LIST, listOf())))
            }
            CommonClassNames.INT_LIST -> {
                McNode(ImplicitCastOp(McType.DeclaredType(CommonClassNames.INT_ARRAY_LIST), type), McNode(NewOp(
                    CommonClassNames.INT_ARRAY_LIST, listOf())))
            }
            CommonClassNames.LONG_ARRAY_LIST -> {
                McNode(ImplicitCastOp(McType.DeclaredType(CommonClassNames.LONG_ARRAY_LIST), type), McNode(NewOp(
                    CommonClassNames.LONG_ARRAY_LIST, listOf())))
            }
            CommonClassNames.BITSET -> McNode(NewOp(type.name, listOf()))
            else -> {
                if (type is McType.ArrayType) {
                    McNode(NewArrayOp(type.elementType), McNode(CstIntOp(0)))
                } else {
                    type.defaultValue()
                }
            }
        }
    }
}

internal fun ProtocolCompiler.generateDefaultConstructGraph(
    message: MessageVariantInfo,
    type: FieldType,
    isTailRecursive: Boolean,
    paramResolver: ParamResolver
): McNode {
    return when (type.defaultConstructInfo) {
        is DefaultConstruct.SubType -> {
            if (isTailRecursive) {
                type.realType.defaultValue()
            } else {
                McNode(ImplicitCastOp(type.defaultConstructInfo.value, type.realType), generateDefaultConstructGraph(type.defaultConstructInfo.value, paramResolver))
            }
        }
        is DefaultConstruct.Constant<*> -> generateConstantGraph(type.realType, type.defaultConstructInfo.value, type.registry)
        is DefaultConstruct.Compute -> generateFunctionCallGraph(message.findFunction(type.defaultConstructInfo.value), paramResolver = paramResolver)
        null -> {
            if (isTailRecursive) {
                type.realType.defaultValue()
            } else {
                generateDefaultConstructGraph(type.realType, paramResolver)
            }
        }
    }
}

internal fun ProtocolCompiler.generateConstantGraph(targetType: McType, value: Any, registry: Registries? = null): McNode {
    return if (targetType.isIntegral && registry != null && value is String) {
        targetType as McType.PrimitiveType
        val rawId = registry.getRawId(value) ?: throw CompileException("Unknown value \"$value\" in registry $registry")
        when (rawId) {
            is Either.Left -> createCstNode(rawId.left.downcastConstant(targetType))
            is Either.Right -> targetType.cast(rawId.right)
        }
    } else if (targetType.hasName(IDENTIFIER)) {
        val (namespace, name) = (value as String).normalizeIdentifier().split(':', limit = 2)
        McNode(NewOp(IDENTIFIER, listOf(McType.STRING, McType.STRING)),
            createCstNode(namespace),
            createCstNode(name)
        )
    } else {
        val enumInfo = targetType.classInfoOrNull as? EnumInfo
        if (enumInfo != null) {
            McNode(LoadFieldOp(targetType, value as String, targetType, isStatic = true))
        } else {
            createCstNode(value.downcastConstant(targetType))
        }
    }
}

internal fun ProtocolCompiler.generateFilledConstructGraph(type: McType, filledParam: FilledParameter): McNode {
    if (filledParam.fromRegistry != null) {
        return if (type == McType.INT) {
            when (val rawId = filledParam.fromRegistry.registry.getRawId(filledParam.fromRegistry.value)) {
                is Either.Left -> McNode(CstIntOp(rawId.left))
                is Either.Right -> rawId.right
                else -> throw CompileException("Unknown registry value ${filledParam.fromRegistry.value} in protocol ${protocolNamesById[currentProtocolId]}")
            }
        } else if (type.hasName(IDENTIFIER)) {
            val oldName = filledParam.fromRegistry.registry.getOldName(filledParam.fromRegistry.value) ?: throw CompileException("Unknown registry value ${filledParam.fromRegistry.value} in protocol ${protocolNamesById[currentProtocolId]}")
            val identifierField = createIdentifierConstantField(oldName)
            McNode(LoadFieldOp(McType.DeclaredType(className), identifierField, McType.DeclaredType(IDENTIFIER), isStatic = true))
        } else {
            throw CompileException("Invalid @Filled(fromRegistry) type $type")
        }
    }

    if (filledParam.registry != null) {
        return if (type.hasName(INT_FUNCTION)) {
            val fieldName = "MAP_${filledParam.registry}_I2S_${currentProtocolId}"
            val identifierType = McType.DeclaredType(IDENTIFIER)
            val mapType = McType.DeclaredType("it.unimi.dsi.fastutil.ints.Int2ObjectMap", listOf(identifierType))
            cacheMembers[fieldName] = { emitter ->
                emitter.append("private static final ")
                mapType.emit(emitter)
                emitter.append(" ").append(fieldName).append(" = ").appendClassName(PACKET_INTRINSICS).append(".makeInt2ObjectMap(").indent()
                for ((index, entry) in filledParam.registry.entries.withIndex()) {
                    if (index != 0) {
                        emitter.append(",")
                    }
                    emitter.appendNewLine().append(entry.id.toString()).append(", new ").appendClassName(IDENTIFIER).append("(")
                    val (namespace, path) = entry.oldName.normalizeIdentifier().split(':', limit = 2)
                    if (namespace != "minecraft") {
                        emitter.append("\"").append(namespace).append("\", ")
                    }
                    emitter.append("\"").append(path).append("\")")
                }
                emitter.dedent().appendNewLine().append(");")
            }
            val intParam = VariableId.create()
            McNode(LambdaOp(type, identifierType, listOf(McType.INT), listOf(intParam)),
                McNode(FunctionCallOp(mapType.name, "get", listOf(mapType, McType.INT), identifierType, false, isStatic = false),
                    McNode(LoadFieldOp(McType.DeclaredType(className), fieldName, mapType, isStatic = true)),
                    McNode(LoadVariableOp(intParam, McType.INT))
                )
            )
        } else if (type.hasName(TO_INT_FUNCTION)) {
            val fieldName = "MAP_${filledParam.registry}_S2I_${currentProtocolId}"
            val identifierType = McType.DeclaredType(IDENTIFIER)
            val mapType = McType.DeclaredType("it.unimi.dsi.fastutil.objects.Object2IntMap", listOf(identifierType))
            cacheMembers[fieldName] = { emitter ->
                emitter.append("private static final ")
                mapType.emit(emitter)
                emitter.append(" ").append(fieldName).append(" = ").appendClassName(PACKET_INTRINSICS).append(".makeObject2IntMap(").indent()
                for ((index, entry) in filledParam.registry.entries.withIndex()) {
                    if (index != 0) {
                        emitter.append(",")
                    }
                    emitter.appendNewLine().append("new ").appendClassName(IDENTIFIER).append("(")
                    val (namespace, path) = entry.oldName.normalizeIdentifier().split(':', limit = 2)
                    if (namespace != "minecraft") {
                        emitter.append("\"").append(namespace).append("\", ")
                    }
                    emitter.append("\"").append(path).append("\"), ").append(entry.id.toString())
                }
                emitter.dedent().appendNewLine().append(");")
            }
            val nameParam = VariableId.create()
            McNode(LambdaOp(type, McType.INT, listOf(identifierType), listOf(nameParam)),
                McNode(FunctionCallOp(mapType.name, "getInt", listOf(mapType, identifierType), McType.INT, false, isStatic = false),
                    McNode(LoadFieldOp(McType.DeclaredType(className), fieldName, mapType, isStatic = true)),
                    McNode(LoadVariableOp(nameParam, identifierType))
                )
            )
        } else {
            throw CompileException("Invalid @Filled(registry) type $type")
        }
    }

    return when ((type as? McType.DeclaredType)?.name) {
        NETWORK_HANDLER -> McNode(LoadVariableOp(VariableId.immediate("networkHandler"), type))
        TYPED_MAP -> McNode(LoadVariableOp(VariableId.immediate("userData"), type))
        DELAYED_PACKET_SENDER -> {
            val bodyParam = VariableId.create()
            val bufsVar = VariableId.create()
            val packetType = type.typeArguments.firstOrNull() as? McType.DeclaredType ?: throw CompileException("$type should have declared type argument")

            val clientbound = (protocols.mapNotNull { protocol ->
                getPacketDirection(protocol.id, packetType.name).takeIf { it != PacketDirection.INVALID }
            }.distinct().singleOrNull() ?: throw CompileException("Packet $packetType has ambiguous direction")) == PacketDirection.CLIENTBOUND

            val sendRaw = if (clientbound) {
                McNode(FunctionCallOp(PACKET_INTRINSICS, "sendRawToClient", listOf(McType.DeclaredType(NETWORK_HANDLER), McType.DeclaredType(TYPED_MAP), McType.BYTE_BUF.listOf()), McType.VOID, true),
                    McNode(LoadVariableOp(VariableId.immediate("networkHandler"), McType.DeclaredType(NETWORK_HANDLER))),
                    McNode(LoadVariableOp(VariableId.immediate("userData"), McType.DeclaredType(TYPED_MAP))),
                    McNode(LoadVariableOp(bufsVar, McType.BYTE_BUF.listOf()))
                )
            } else {
                McNode(FunctionCallOp(PACKET_INTRINSICS, "sendRawToServer", listOf(McType.DeclaredType(NETWORK_HANDLER), McType.BYTE_BUF.listOf()), McType.VOID, true),
                    McNode(LoadVariableOp(VariableId.immediate("networkHandler"), McType.DeclaredType(NETWORK_HANDLER))),
                    McNode(LoadVariableOp(bufsVar, McType.BYTE_BUF.listOf()))
                )
            }

            McNode(LambdaOp(type, McType.VOID, listOf(packetType), listOf(bodyParam)),
                McNode(StmtListOp,
                    McNode(StoreVariableStmtOp(bufsVar, McType.BYTE_BUF.listOf(), true),
                        McNode(ImplicitCastOp(McType.DeclaredType(ARRAY_LIST), McType.BYTE_BUF.listOf()),
                            McNode(NewOp(ARRAY_LIST, listOf(McType.INT)), McNode(CstIntOp(1)))
                        )
                    ),
                    generateExplicitSenderServerRegistries(packetType.messageVariantInfo, currentProtocolId, bodyParam, clientbound, bufsVar),
                    McNode(PopStmtOp, sendRaw)
                )
            )
        }
        else -> throw CompileException("Invalid @Filled argument type $type")
    }
}

private fun generateGlobalDataGraph(paramType: McType): McNode {
    val classType = McType.DeclaredType(CLASS)
    val objectType = McType.DeclaredType(OBJECT)
    val mapType = McType.DeclaredType(MAP, listOf(classType, objectType))
    if (paramType.hasName(CONSUMER)) {
        val consumedType = paramType.typeArguments.single()
        val consumedVar = VariableId.create()
        return McNode(LambdaOp(paramType, McType.VOID, listOf(consumedType), listOf(consumedVar)),
            McNode(FunctionCallOp(mapType.name, "put", listOf(mapType, classType, objectType), McType.VOID, true, isStatic = false),
                McNode(LoadVariableOp(VariableId.immediate("globalData"), mapType)),
                McNode(LoadFieldOp(consumedType, "class", classType, isStatic = true)),
                McNode(LoadVariableOp(consumedVar, objectType))
            )
        )
    } else {
        return McNode(CastOp(objectType, paramType),
            McNode(FunctionCallOp(mapType.name, "get", listOf(mapType, classType), objectType, false, isStatic = false),
                McNode(LoadVariableOp(VariableId.immediate("globalData"), mapType)),
                McNode(LoadFieldOp(paramType, "class", classType, isStatic = true)),
            )
        )
    }
}

internal fun ProtocolCompiler.createTailRecurseField(ownerClass: String, fieldName: String, fieldType: String): String {
    val handleFieldName = "MH_${ownerClass.replace('.', '_').uppercase()}_TAIL_RECURSE"
    cacheMembers[handleFieldName] = { emitter ->
        emitter.append("private static final ").appendClassName(CommonClassNames.METHOD_HANDLE).append(" ").append(handleFieldName)
            .append(" = ").appendClassName(PACKET_INTRINSICS).append(".findSetterHandle(")
            .appendClassName(ownerClass).append(".class, \"").append(fieldName).append("\", ").appendClassName(fieldType).append(".class);")
    }
    return handleFieldName
}

internal fun ProtocolCompiler.createIdentifierConstantField(constant: String): String {
    val (namespace, name) = constant.normalizeIdentifier().split(':', limit = 2)
    val fieldName = "IDENTIFIER_${namespace.uppercase()}_${name.uppercase().replace('/', '_').replace('.', '_')}"
    cacheMembers[fieldName] = { emitter ->
        emitter.append("private static final ").appendClassName(IDENTIFIER).append(" ").append(fieldName)
            .append(" = new ").appendClassName(IDENTIFIER).append("(")
        McNode(CstStringOp(namespace)).emit(emitter, Precedence.COMMA)
        emitter.append(", ")
        McNode(CstStringOp(name)).emit(emitter, Precedence.COMMA)
        emitter.append(");")
    }
    return fieldName
}

internal fun ProtocolCompiler.createRegistryKeyField(registry: Registries, id: String): String {
    val (namespace, name) = id.normalizeIdentifier().split(':', limit = 2)
    val fieldName = "RK_${registry.name}_${namespace.uppercase()}_${name.uppercase().replace('/', '_').replace('.', '_')}"
    cacheMembers[fieldName] = { emitter ->
        emitter.append("private static final ").appendClassName(CommonClassNames.REGISTRY_KEY).append(" ").append(fieldName)
            .append(" = ").appendClassName(CommonClassNames.REGISTRY_KEY).append(".of(")
            .appendClassName(CommonClassNames.REGISTRY).append(".").append(registry.registryKeyFieldName).append(", new ")
            .appendClassName(IDENTIFIER).append("(")
        McNode(CstStringOp(namespace)).emit(emitter, Precedence.COMMA)
        emitter.append(", ")
        McNode(CstStringOp(name)).emit(emitter, Precedence.COMMA)
        emitter.append("));")
    }
    return fieldName
}
