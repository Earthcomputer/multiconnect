package net.earthcomputer.multiconnect.ap

import kotlinx.serialization.Serializable

@Serializable
class MulticonnectField(
    val name: String,
    val type: MulticonnectType
)
