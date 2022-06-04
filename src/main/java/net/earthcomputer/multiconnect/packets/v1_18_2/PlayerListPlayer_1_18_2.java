package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlayerList;

import java.util.Optional;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class PlayerListPlayer_1_18_2 implements SPacketPlayerList.AddPlayer.Player {
    public CommonTypes.GameProfile profile;
    public int gamemode;
    public int ping;
    public Optional<CommonTypes.Text> displayName;
}
