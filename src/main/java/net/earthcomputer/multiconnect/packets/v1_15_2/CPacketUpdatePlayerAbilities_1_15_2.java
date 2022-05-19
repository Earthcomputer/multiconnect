package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerAbilities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class CPacketUpdatePlayerAbilities_1_15_2 implements CPacketPlayerAbilities {
    @Introduce(compute = "computeFlags")
    public byte flags;
    @Introduce(compute = "computeFlySpeed")
    public float flySpeed;
    @Introduce(compute = "computeWalkSpeed")
    public float walkSpeed;

    public static byte computeFlags(@Argument("flags") byte flags) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            PlayerAbilities abilities = player.getAbilities();
            if (abilities.invulnerable) {
                flags |= 1;
            }
            if (abilities.allowFlying) {
                flags |= 4;
            }
            if (abilities.creativeMode) {
                flags |= 8;
            }
        }
        return flags;
    }

    public static float computeFlySpeed() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getAbilities().getFlySpeed() : 0;
    }

    public static float computeWalkSpeed() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getAbilities().getWalkSpeed() : 0;
    }
}
