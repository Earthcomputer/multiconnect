package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClientInformation;
import net.earthcomputer.multiconnect.packets.latest.CPacketClientInformation_Latest;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class CPacketClientInformation_1_16_5 implements CPacketClientInformation {
    public String language;
    public byte renderDistance;
    public CPacketClientInformation_Latest.ChatSetting chatSetting;
    public boolean chatColors;
    @Type(Types.UNSIGNED_BYTE)
    public int displayedSkinParts;
    public CPacketClientInformation_Latest.Arm mainHand;
}
