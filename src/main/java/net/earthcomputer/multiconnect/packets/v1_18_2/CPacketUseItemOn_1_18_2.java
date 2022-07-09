package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUseItemOn;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_18_2)
public class CPacketUseItemOn_1_18_2 implements CPacketUseItemOn {
    public CommonTypes.Hand hand;
    public CommonTypes.BlockPos pos;
    public CommonTypes.Direction face;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public boolean insideBlock;
}
