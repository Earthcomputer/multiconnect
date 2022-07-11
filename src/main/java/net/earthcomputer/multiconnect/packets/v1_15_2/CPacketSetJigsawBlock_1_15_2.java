package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketSetJigsawBlock;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.resources.ResourceLocation;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_15_2)
public class CPacketSetJigsawBlock_1_15_2 implements CPacketSetJigsawBlock {
    public CommonTypes.BlockPos pos;
    public ResourceLocation target;
    public ResourceLocation pool;
    public String finalState;

    @Handler(protocol = Protocols.V1_13_2)
    public static void drop() {
    }
}
