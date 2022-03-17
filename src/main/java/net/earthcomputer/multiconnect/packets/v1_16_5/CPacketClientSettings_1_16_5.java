package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClientSettings;
import net.earthcomputer.multiconnect.packets.latest.CPacketClientSettings_Latest;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class CPacketClientSettings_1_16_5 implements CPacketClientSettings {
    public String language;
    public byte renderDistance;
    public CPacketClientSettings_Latest.ChatSetting chatSetting;
    public boolean chatColors;
    @Type(Types.UNSIGNED_BYTE)
    public int displayedSkinParts;
    public CPacketClientSettings_Latest.Arm mainHand;
}
