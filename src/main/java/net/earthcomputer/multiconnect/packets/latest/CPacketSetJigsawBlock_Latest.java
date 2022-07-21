package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketSetJigsawBlock;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.resources.ResourceLocation;

@MessageVariant(minVersion = Protocols.V1_16)
public class CPacketSetJigsawBlock_Latest implements CPacketSetJigsawBlock {
    public CommonTypes.BlockPos pos;
    public ResourceLocation name;
    public ResourceLocation target;
    public ResourceLocation pool;
    public String finalState;
    public String jointType;
}
