package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUpdatePlayerAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class CPacketUpdatePlayerAbilities_1_15_2 implements CPacketUpdatePlayerAbilities {
    @Introduce(compute = "computeFlags")
    public byte flags;
    @Introduce(compute = "computeFlySpeed")
    public float flySpeed;
    @Introduce(compute = "computeWalkSpeed")
    public float walkSpeed;

    public static byte computeFlags(@Argument("flags") byte flags) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Abilities abilities = player.getAbilities();
            if (abilities.invulnerable) {
                flags |= 1;
            }
            if (abilities.mayfly) {
                flags |= 4;
            }
            if (abilities.instabuild) {
                flags |= 8;
            }
        }
        return flags;
    }

    public static float computeFlySpeed() {
        Player player = Minecraft.getInstance().player;
        return player != null ? player.getAbilities().getFlyingSpeed() : 0;
    }

    public static float computeWalkSpeed() {
        Player player = Minecraft.getInstance().player;
        return player != null ? player.getAbilities().getWalkingSpeed() : 0;
    }
}
