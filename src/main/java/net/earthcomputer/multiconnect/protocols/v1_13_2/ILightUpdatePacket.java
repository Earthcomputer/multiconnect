package net.earthcomputer.multiconnect.protocols.v1_13_2;

public interface ILightUpdatePacket {

    void setChunkX(int chunkX);
    void setChunkZ(int chunkZ);
    void setSkylightMask(int skylightMask);
    void setBlocklightMask(int blocklightMask);


}
