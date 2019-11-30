package net.earthcomputer.multiconnect.protocols.v1_13_2;

import java.util.List;

public interface ILightUpdatePacket {

    void setChunkX(int chunkX);
    void setChunkZ(int chunkZ);
    void setSkylightMask(int skylightMask);
    void setBlocklightMask(int blocklightMask);
    void setSkyLightUpdates(List<byte[]> skyLightUpdates);
    void setBlockLightUpdates(List<byte[]> blockLightUpdates);

}
