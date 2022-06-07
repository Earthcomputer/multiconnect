package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerAction;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.CPacketPlayerAction_Latest;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class CPacketPlayerAction_1_18_2 implements CPacketPlayerAction {
    public CPacketPlayerAction_Latest.Action action;
    public CommonTypes.BlockPos pos;
    @Type(Types.UNSIGNED_BYTE)
    public CommonTypes.Direction face;
}
