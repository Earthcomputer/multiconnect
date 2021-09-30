package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.Contextual
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
class MessageType @OptIn(ExperimentalSerializationApi::class) constructor(
    val fields: List<MulticonnectField>,
    val functions: List<MulticonnectFunction>,
    val polymorphicParent: String?,
    @Contextual val polymorphic: Polymorphic?,
    @Contextual val defaultConstruct: DefaultConstruct?,
    val handler: String?,
    val partialHandlers: List<String>,
    @Contextual val translateFromNewer: Protocol?,
    @Contextual val translateFromOlder: Protocol?,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val type: String = "message"
)
