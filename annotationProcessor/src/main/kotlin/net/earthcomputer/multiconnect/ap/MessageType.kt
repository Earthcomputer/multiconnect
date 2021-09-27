package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.Serializable

@Serializable
class MessageType(
    val functions: List<MulticonnectFunction>
)
