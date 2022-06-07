package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerInteractBlock;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(minVersion = Protocols.V1_14)
public class CPacketPlayerInteractBlock_Latest implements CPacketPlayerInteractBlock {
    public CommonTypes.Hand hand;
    public CommonTypes.BlockPos pos;
    public CommonTypes.Direction face;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public boolean insideBlock;
}
