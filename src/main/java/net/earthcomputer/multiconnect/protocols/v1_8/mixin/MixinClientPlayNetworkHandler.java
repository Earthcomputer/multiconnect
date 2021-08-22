package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.IChunkDataS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_16_5.PendingFullChunkData;
import net.earthcomputer.multiconnect.protocols.v1_8.DataTrackerEntry_1_8;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.EnumMap;

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public abstract class MixinClientPlayNetworkHandler {
    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");

    @Shadow private ClientWorld world;

    @Shadow public abstract void onUnloadChunk(UnloadChunkS2CPacket packet);

    @Shadow public abstract void onEntityStatus(EntityStatusS2CPacket packet);

    @Shadow public abstract void onChunkData(ChunkDataS2CPacket packet);

    @Shadow private DynamicRegistryManager registryManager;

    @Inject(method = {"onGameJoin", "onPlayerRespawn"}, at = @At("TAIL"))
    private void onOnGameJoinOrRespawn(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // client permission level 4 to enable features just in case we're opped
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            assert player != null;
            onEntityStatus(new EntityStatusS2CPacket(player, (byte) 28));
        }
    }

    @Inject(method = "onChunkData", at = @At("HEAD"), cancellable = true)
    private void onOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8
                && PendingFullChunkData.isFullChunk(new ChunkPos(packet.getX(), packet.getZ()), false)
                && packet.getVerticalStripBitmask().isEmpty()) {
            onUnloadChunk(new UnloadChunkS2CPacket(packet.getX(), packet.getZ()));
            ci.cancel();
        }
    }

    @Inject(method = "onChunkData", at = @At("RETURN"), cancellable = true)
    private void postChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        // 1.8 doesn't send neighboring empty chunks, so we must assume they are empty unless otherwise specified

        // don't load more empty chunks next to empty chunks, that would cause an infinite loop
        WorldChunk chunk = world.getChunk(packet.getX(), packet.getZ());
        boolean isEmpty = true;
        if (!chunk.isEmpty()) {
            for (ChunkSection section : chunk.getSectionArray()) {
                if (!ChunkSection.isEmpty(section)) {
                    isEmpty = false;
                    break;
                }
            }
        }
        if (!isEmpty) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                int x = packet.getX() + dir.getOffsetX();
                int z = packet.getZ() + dir.getOffsetZ();
                if (world.getChunk(x, z, ChunkStatus.FULL, false) == null) {
                    Registry<Biome> biomeRegistry = registryManager.get(Registry.BIOME_KEY);
                    Biome plainsBiome = biomeRegistry.get(BiomeKeys.PLAINS);
                    int horizontalSectionCount = MathHelper.log2DeBruijn(16) - 2;
                    int biomeLength = (1 << (horizontalSectionCount + horizontalSectionCount)) * ((world.getHeight() + 3) / 4);

                    ChunkDataS2CPacket neighborPacket = new ChunkDataS2CPacket(new WorldChunk(world, new ChunkPos(x, z), new BiomeArray(biomeRegistry, world, new int[biomeLength])));
                    //noinspection ConstantConditions
                    IChunkDataS2CPacket iPacket = (IChunkDataS2CPacket) neighborPacket;
                    iPacket.multiconnect_setDataTranslated(true);
                    iPacket.multiconnect_setDimension(((IChunkDataS2CPacket) packet).multiconnect_getDimension());
                    iPacket.multiconnect_setBlocksNeedingUpdate(new EnumMap<>(EightWayDirection.class));
                    Biome[] biomes = new Biome[256];
                    Arrays.fill(biomes, plainsBiome);
                    ((net.earthcomputer.multiconnect.protocols.v1_14_4.IChunkDataS2CPacket) iPacket).set_1_14_4_biomeData(biomes);
                    onChunkData(neighborPacket);
                }
            }
        }
    }

    @Inject(method = "onEntityTrackerUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER),
            cancellable = true)
    private void onOnEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            Entity entity = world.getEntityById(packet.id());
            if (entity != null) {
                for (DataTracker.Entry<?> entry : packet.getTrackedValues()) {
                    if (!(entry instanceof DataTrackerEntry_1_8 entry_1_8)) {
                        MULTICONNECT_LOGGER.warn("Not handling entity tracker update entry which was not constructed for 1.8");
                        continue;
                    }
                    switch (entry_1_8.getSerializerId()) {
                        case 0 -> Protocol_1_8.handleByteTrackedData(entity, entry_1_8.getId(), (Byte) entry_1_8.get());
                        case 1 -> Protocol_1_8.handleShortTrackedData(entity, entry_1_8.getId(),
                                (Short) entry_1_8.get());
                        case 2 -> Protocol_1_8.handleIntTrackedData(entity, entry_1_8.getId(),
                                (Integer) entry_1_8.get());
                        case 3 -> Protocol_1_8.handleFloatTrackedData(entity, entry_1_8.getId(),
                                (Float) entry_1_8.get());
                        case 4 -> Protocol_1_8.handleStringTrackedData(entity, entry_1_8.getId(),
                                (String) entry_1_8.get());
                        case 5 -> Protocol_1_8.handleItemStackTrackedData(entity, entry_1_8.getId(),
                                (ItemStack) entry_1_8.get());
                        case 6 -> Protocol_1_8.handleBlockPosTrackedData(entity, entry_1_8.getId(), (BlockPos) entry_1_8.get());
                        case 7 -> Protocol_1_8.handleEulerAngleTrackedData(entity, entry_1_8.getId(), (EulerAngle) entry_1_8.get());
                        default -> throw new AssertionError();
                    }
                }
            }
            ci.cancel();
        }
    }
}
