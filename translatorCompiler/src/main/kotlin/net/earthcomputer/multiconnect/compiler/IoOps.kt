package net.earthcomputer.multiconnect.compiler

object IoOps {
    fun readVarIntOp(): McNodeOp {
        return FunctionCallOp(CommonClassNames.COMMON_TYPES, "readVarInt", listOf(McType.DeclaredType(CommonClassNames.BYTE_BUF)), McType.INT, true)
    }
}
