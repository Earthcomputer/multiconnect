package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerInteractBlock;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class CPacketPlayerInteractBlock_1_13_2 implements CPacketPlayerInteractBlock {
    public CommonTypes.BlockPos pos;
    public CommonTypes.Direction face;
    public CommonTypes.Hand hand;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
}
