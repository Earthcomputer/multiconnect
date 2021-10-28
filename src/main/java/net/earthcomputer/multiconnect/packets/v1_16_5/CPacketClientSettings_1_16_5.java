package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClientSettings;

@Message(variantOf = CPacketClientSettings.class, maxVersion = Protocols.V1_16_5)
public class CPacketClientSettings_1_16_5 {
    public String language;
    public byte renderDistance;
    public CPacketClientSettings.ChatSetting chatSetting;
    public boolean chatColors;
    @Type(Types.UNSIGNED_BYTE)
    public int displayedSkinParts;
    public CPacketClientSettings.Arm mainHand;
}
