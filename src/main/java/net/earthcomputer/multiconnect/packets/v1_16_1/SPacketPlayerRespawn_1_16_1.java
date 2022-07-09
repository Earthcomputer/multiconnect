package net.earthcomputer.multiconnect.packets.v1_16_1;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketPlayerRespawn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

@MessageVariant(minVersion = Protocols.V1_16, maxVersion = Protocols.V1_16_1)
public class SPacketPlayerRespawn_1_16_1 implements SPacketPlayerRespawn {
    @Introduce(compute = "computeDimension")
    public ResourceLocation dimension;
    @Introduce(compute = "computeDimension")
    public ResourceLocation dimensionId;
    @Type(Types.LONG)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.UNSIGNED_BYTE)
    @Introduce(compute = "computePreviousGamemode")
    public int previousGamemode;
    @Introduce(compute = "computeIsDebug")
    public boolean isDebug;
    @Introduce(compute = "computeIsFlat")
    public boolean isFlat;
    @Introduce(booleanValue = true)
    public boolean copyMetadata;

    public static ResourceLocation computeDimension(@Argument("dimensionId") int dimensionId) {
        return SPacketGameJoin_1_16_1.computeDimension(dimensionId);
    }

    public static int computePreviousGamemode() {
        // TODO: save this value in global data rather than getting it from the main thread which is racey
        var interactionManager = Minecraft.getInstance().gameMode;
        if (interactionManager != null) {
            GameType previousGameMode = interactionManager.getPreviousPlayerMode();
            if (previousGameMode != null) {
                return previousGameMode.getId();
            }
        }
        return 255; // none
    }

    public static boolean computeIsDebug(@Argument("genType") String genType) {
        return SPacketGameJoin_1_16_1.computeIsDebug(genType);
    }

    public static boolean computeIsFlat(@Argument("genType") String genType) {
        return SPacketGameJoin_1_16_1.computeIsFlat(genType);
    }
}
