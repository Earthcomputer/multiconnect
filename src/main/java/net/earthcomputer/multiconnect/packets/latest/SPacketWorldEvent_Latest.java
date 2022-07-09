package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.CustomFix;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketWorldEvent;
import net.earthcomputer.multiconnect.protocols.v1_12.block.Blocks_1_12_2;
import net.minecraft.world.level.block.LevelEvent;

@MessageVariant(minVersion = Protocols.V1_13)
public class SPacketWorldEvent_Latest implements SPacketWorldEvent {
    @Type(Types.INT)
    public int id;
    public CommonTypes.BlockPos location;
    @Type(Types.INT)
    @Introduce(compute = "computeData")
    @CustomFix("fixData")
    public int data;
    public boolean global;

    public static int computeData(@Argument("id") int id, @Argument("data") int data) {
        if (id == LevelEvent.PARTICLES_DESTROY_BLOCK) {
            return Blocks_1_12_2.convertToStateRegistryId(data);
        } else {
            return data;
        }
    }

    public static int fixData(int data, @Argument("id") int id) {
        if (id == LevelEvent.PARTICLES_DESTROY_BLOCK) {
            return PacketSystem.serverBlockStateIdToClient(data);
        } else {
            return data;
        }
    }
}
