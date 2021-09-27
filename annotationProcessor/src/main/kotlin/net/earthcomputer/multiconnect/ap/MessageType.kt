package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class MessageType(
    val fields: List<MulticonnectField>,
    val functions: List<MulticonnectFunction>,
    val polymorphicParent: String?,
    @Contextual val polymorphic: Polymorphic?,
    @Contextual val defaultConstruct: DefaultConstruct?,
    val handler: String?,
    val partialHandlers: List<String>,
)
