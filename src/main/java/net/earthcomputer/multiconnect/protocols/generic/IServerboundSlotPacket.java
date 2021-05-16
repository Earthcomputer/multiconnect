package net.earthcomputer.multiconnect.protocols.generic;

public interface IServerboundSlotPacket {
    boolean multiconnect_isProcessed();
    void multiconnect_setProcessed();
    int multiconnect_getSlotId();
}
