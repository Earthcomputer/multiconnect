package net.earthcomputer.multiconnect.protocols.generic;

public interface IChunkDataS2CPacket {
    boolean multiconnect_isDataTranslated();
    void multiconnect_setDataTranslated(boolean dataTranslated);

    void setData(byte[] data);
}
