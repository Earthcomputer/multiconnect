package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.packets.latest.SPacketPlayerChat_Latest;

@MessageVariant
public class SPacketPlayerChatHeader {
    public SPacketPlayerChat_Latest.SignedHeader header;
    public byte[] headerSignature;
    public byte[] bodyDigest;
}
