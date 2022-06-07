package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
class MessageType @OptIn(ExperimentalSerializationApi::class) constructor(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val type: String = "message"
)
