package net.earthcomputer.multiconnect.protocols.generic;

public interface ICustomPayloadC2SPacket {
    boolean multiconnect_isBlocked();
    void multiconnect_unblock();
}
