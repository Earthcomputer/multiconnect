package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

public interface IBlockConnectableChunk {

    ChunkConnector multiconnect_getChunkConnector();

    void multiconnect_setChunkConnector(ChunkConnector chunkConnector);
}
