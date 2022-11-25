package net.earthcomputer.multiconnect.impl.via;

import com.mojang.logging.LogUtils;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ServerboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.BitSet;

public class ViaMulticonnectTranslator implements IMulticonnectTranslator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AttributeKey<UserConnection> VIA_USER_CONNECTION_KEY = AttributeKey.valueOf("multiconnect.via_user_connection");

    private IMulticonnectTranslatorApi api;

    @Override
    public boolean isApplicableInEnvironment(IMulticonnectTranslatorApi api) {
        return !api.isModLoaded("viafabric");
    }

    @Override
    public void init(IMulticonnectTranslatorApi api) {
        this.api = api;

        var manager = ViaManagerImpl.builder()
            .injector(new MulticonnectInjector())
            .loader(new MulticonnectLoader(api))
            .platform(new MulticonnectPlatform(api))
            .build();
        Via.init(manager);
        manager.init();

        LOGGER.info("ViaVersion version: {}", Via.getAPI().getVersion());
    }

    @Override
    public void inject(Channel channel) {
        // Singleplayer doesnt include encoding
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }

        UserConnection info = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(info);
        channel.attr(VIA_USER_CONNECTION_KEY).set(info);
        channel.pipeline()
            .addBefore("encoder", "multiconnect_serverbound_translator", new MulticonnectServerboundTranslator(info))
            .addBefore("decoder", "multiconnect_clientbound_translator", new MulticonnectClientboundTranslator(info));
    }

    @Override
    public void postPipelineModifiers(Channel channel) {
        // reinstall transformers to make sure they're in the right order compared to the decoders
        if (channel.pipeline().context("multiconnect_serverbound_translator") != null) {
            var translator = channel.pipeline().remove("multiconnect_serverbound_translator");
            channel.pipeline().addBefore("encoder", "multiconnect_serverbound_translator", translator);
        }
        if (channel.pipeline().context("multiconnect_clientbound_translator") != null) {
            var translator = channel.pipeline().remove("multiconnect_clientbound_translator");
            channel.pipeline().addBefore("decoder", "multiconnect_clientbound_translator", translator);
        }
    }

    private static BitSet invertMappings(Mappings mappings) {
        BitSet result = new BitSet(mappings.mappedSize());
        for (int oldId = 0; oldId < mappings.size(); oldId++) {
            int newId = mappings.getNewId(oldId);
            if (newId != -1) {
                result.set(newId);
            }
        }
        return result;
    }

    private static BitSet getInverseMappings(Channel channel, AttributeKey<BitSet> key, Mappings mappings) {
        if (mappings == null) {
            return null;
        }

        BitSet result = channel.attr(key).get();
        if (result == null) {
            channel.attr(key).set(result = invertMappings(mappings));
        }
        return result;
    }

    private static final AttributeKey<BitSet> INV_BLOCK_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invBlockMappings");
    private static final AttributeKey<BitSet> INV_ENTITY_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invEntityMappings");
    private static final AttributeKey<BitSet> INV_ENCHANTMENT_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invEnchantmentMappings");
    private static final AttributeKey<BitSet> INV_ARGUMENT_TYPE_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invArgumentTypeMappings");
    private static final AttributeKey<BitSet> INV_BLOCK_ENTITY_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invBlockEntityMappings");
    private static final AttributeKey<BitSet> INV_PAINTING_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invPaintingMappings");
    private static final AttributeKey<BitSet> INV_PARTICLE_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invParticleMappings");
    private static final AttributeKey<BitSet> INV_SOUND_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invSoundMappings");
    private static final AttributeKey<BitSet> INV_STATISTICS_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invStatisticsMappings");

    @Override
    public boolean doesServerKnow(String registry, String entry) {
        int id = getRegistryId(registry, entry);

        Channel channel = api.getCurrentChannel();
        if (channel == null) {
            return true;
        }
        UserConnection connection = channel.attr(VIA_USER_CONNECTION_KEY).get();
        MappingData mappingData = connection.getProtocolInfo().getPipeline().getMappingData();
        if (mappingData == null) {
            return true;
        }

        return switch (registry) {
            case "minecraft:block" -> doesServerKnow(channel, INV_BLOCK_MAPPINGS_KEY, mappingData.getBlockMappings(), id);
            case "minecraft:item" -> mappingData.getOldItemId(id) != -1;
            case "minecraft:entity_type" -> doesServerKnow(channel, INV_ENTITY_MAPPINGS_KEY, mappingData.getEntityMappings(), id);
            case "minecraft:enchantment" -> doesServerKnow(channel, INV_ENCHANTMENT_MAPPINGS_KEY, mappingData.getEnchantmentMappings(), id);
            case "minecraft:command_argument_type" -> doesServerKnow(channel, INV_ARGUMENT_TYPE_MAPPINGS_KEY, mappingData.getArgumentTypeMappings(), id);
            case "minecraft:block_entity_type" -> doesServerKnow(channel, INV_BLOCK_ENTITY_MAPPINGS_KEY, mappingData.getBlockEntityMappings(), id);
            case "minecraft:painting_variant" -> doesServerKnow(channel, INV_PAINTING_MAPPINGS_KEY, mappingData.getPaintingMappings(), id);
            case "minecraft:particle_type" -> doesServerKnow(channel, INV_PARTICLE_MAPPINGS_KEY, mappingData.getParticleMappings(), id);
            case "minecraft:sound_event" -> doesServerKnow(channel, INV_SOUND_MAPPINGS_KEY, mappingData.getSoundMappings(), id);
            case "minecraft:custom_stat" -> doesServerKnow(channel, INV_STATISTICS_MAPPINGS_KEY, mappingData.getStatisticsMappings(), id);
            default -> true;
        };
    }

    private static boolean doesServerKnow(Channel channel, AttributeKey<BitSet> key, @Nullable FullMappings mappings, int id) {
        if (mappings == null) {
            return true;
        }
        return doesServerKnow(channel, key, mappings.mappings(), id);
    }

    private static boolean doesServerKnow(Channel channel, AttributeKey<BitSet> key, @Nullable Mappings mappings, int id) {
        if (mappings == null) {
            return true;
        }
        return getInverseMappings(channel, key, mappings).get(id);
    }

    @SuppressWarnings("unchecked")
    private static <T> int getRegistryId(String registry, String entry) {
        Registry<T> reg = (Registry<T>) Registry.REGISTRY.get(new ResourceLocation(registry));
        if (reg == null) {
            throw new RuntimeException("Unknown registry: " + registry);
        }
        T value = reg.getOrThrow(ResourceKey.create(reg.key(), new ResourceLocation(entry)));
        return reg.getId(value);
    }

    @Override
    public void sendStringCustomPayload(Channel channel, String payloadChannel, ByteBuf payload) throws Exception {
        UserConnection connection = channel.attr(VIA_USER_CONNECTION_KEY).get();
        PacketWrapper packet = Via.getManager().getProtocolManager().createPacketWrapper(
            ServerboundPackets1_12_1.PLUGIN_MESSAGE,
            channel.alloc().buffer(),
            connection
        );
        packet.write(Type.STRING, payloadChannel);
        byte[] bytes = new byte[payload.readableBytes()];
        payload.readBytes(bytes);
        packet.write(Type.REMAINING_BYTES, bytes);
        packet.sendToServer(Protocol1_13To1_12_2.class);
    }
}
