package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClientSettings;

@MessageVariant(minVersion = Protocols.V1_18)
public class CPacketClientSettings_Latest implements CPacketClientSettings {
    public String language;
    public byte renderDistance;
    public ChatSetting chatSetting;
    public boolean chatColors;
    @Type(Types.UNSIGNED_BYTE)
    public int displayedSkinParts;
    public Arm mainHand;
    public boolean disableTextFiltering;
    public boolean allowsListing;

    @NetworkEnum
    public enum ChatSetting {
        ENABLED, COMMANDS_ONLY, HIDDEN
    }

    @NetworkEnum
    public enum Arm {
        LEFT, RIGHT
    }
}
