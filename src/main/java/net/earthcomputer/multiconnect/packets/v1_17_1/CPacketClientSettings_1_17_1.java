package net.earthcomputer.multiconnect.packets.v1_17_1;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClientSettings;
import net.earthcomputer.multiconnect.packets.latest.CPacketClientSettings_Latest;

@MessageVariant(minVersion = Protocols.V1_17, maxVersion = Protocols.V1_17_1)
public class CPacketClientSettings_1_17_1 implements CPacketClientSettings {
    public String language;
    public byte renderDistance;
    public CPacketClientSettings_Latest.ChatSetting chatSetting;
    public boolean chatColors;
    @Type(Types.UNSIGNED_BYTE)
    public int displayedSkinParts;
    public CPacketClientSettings_Latest.Arm mainHand;
    public boolean disableTextFiltering;
}
