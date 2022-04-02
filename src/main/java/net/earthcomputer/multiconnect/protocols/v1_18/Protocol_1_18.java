package net.earthcomputer.multiconnect.protocols.v1_18;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_18_2.Protocol_1_18_2;
import net.earthcomputer.multiconnect.transformer.Codecked;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

public class Protocol_1_18 extends Protocol_1_18_2 {
    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // entity id
            buf.readBoolean(); // hardcore
            buf.readByte(); // game mode
            buf.readByte(); // previous game mode
            buf.readCollection(Sets::newHashSetWithExpectedSize, b -> RegistryKey.of(Registry.WORLD_KEY, b.readIdentifier())); // dimension ids
            buf.disablePassthroughMode();
            buf.decode(Codec.unit(() -> Unit.INSTANCE)); // registry manager
            buf.decode(Codec.unit(() -> Unit.INSTANCE)); // dimension type
            Identifier dimensionId = buf.readIdentifier();
            DynamicRegistryManager.Mutable registryManager = DynamicRegistryManager.createAndLoad();
            //noinspection unchecked
            buf.pendingRead(
                    (Class<Codecked<DynamicRegistryManager>>) (Class<?>) Codecked.class,
                    new Codecked<>(DynamicRegistryManager.CODEC, registryManager)
            );
            //noinspection unchecked
            buf.pendingRead(
                    (Class<Codecked<RegistryEntry<DimensionType>>>) (Class<?>) Codecked.class,
                    new Codecked<>(
                            DimensionType.REGISTRY_CODEC,
                            registryManager.get(Registry.DIMENSION_TYPE_KEY)
                                    .getOrCreateEntry(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, dimensionId))
                    )
            );
            buf.pendingRead(Identifier.class, dimensionId);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(RemoveEntityStatusEffectS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(buf.readUnsignedByte())); // effect id
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityStatusEffectS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(buf.readByte() & 0xff)); // effect id
            buf.applyPendingReads();
        });
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.addTag(BlockTags.FALL_DAMAGE_RESETTING, BlockTags.CLIMBABLE);
        tags.add(BlockTags.FALL_DAMAGE_RESETTING, Blocks.SWEET_BERRY_BUSH, Blocks.COBWEB);
        super.addExtraBlockTags(tags);
    }
}
