package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class CPacketClientSettings {
    public String language;
    public byte renderDistance;
    public ChatSetting chatSetting;
    public boolean chatColors;
    @Type(Types.UNSIGNED_BYTE)
    public int displayedSkinParts;
    public Arm mainHand;
    public boolean disableTextFiltering;

    public enum ChatSetting {
        ENABLED, COMMANDS_ONLY, HIDDEN
    }

    public enum Arm {
        LEFT, RIGHT
    }
}
