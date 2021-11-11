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
    val handlerProtocol: Int?,
    val partialHandlers: List<String>,
    val variantOf: String?,
    val minVersion: Int?,
    val maxVersion: Int?,
    val tailrec: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val type: String = "message"
)
