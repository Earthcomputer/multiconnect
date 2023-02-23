package net.earthcomputer.multiconnect.impl.via;

import com.mojang.logging.LogUtils;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ServerboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ViaMulticonnectTranslator implements IMulticonnectTranslator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AttributeKey<UserConnection> VIA_USER_CONNECTION_KEY = AttributeKey.valueOf("multiconnect.via_user_connection");

    protected IMulticonnectTranslatorApi api;

    /**
     * Returns null in singleplayer
     */
    @Nullable
    public static UserConnection getUserConnection(Channel channel) {
        return channel.attr(VIA_USER_CONNECTION_KEY).get();
    }

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

    private static Int2IntMap invertMappings(Mappings mappings) {
        Int2IntMap result = new Int2IntOpenHashMap(mappings.mappedSize());
        for (int oldId = 0; oldId < mappings.size(); oldId++) {
            int newId = mappings.getNewId(oldId);
            if (newId != -1) {
                result.put(newId, oldId);
            }
        }
        return result;
    }

    private static Int2IntMap getInverseMappings(Channel channel, AttributeKey<Int2IntMap> key, Mappings mappings) {
        if (mappings == null) {
            return null;
        }

        Int2IntMap result = channel.attr(key).get();
        if (result == null) {
            channel.attr(key).set(result = invertMappings(mappings));
        }
        return result;
    }

    private static final AttributeKey<Int2IntMap> INV_BLOCK_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invBlockMappings");
    private static final AttributeKey<Int2IntMap> INV_ENTITY_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invEntityMappings");
    private static final AttributeKey<Int2IntMap> INV_ENCHANTMENT_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invEnchantmentMappings");
    private static final AttributeKey<Int2IntMap> INV_ARGUMENT_TYPE_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invArgumentTypeMappings");
    private static final AttributeKey<Int2IntMap> INV_BLOCK_ENTITY_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invBlockEntityMappings");
    private static final AttributeKey<Int2IntMap> INV_PAINTING_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invPaintingMappings");
    private static final AttributeKey<Int2IntMap> INV_PARTICLE_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invParticleMappings");
    private static final AttributeKey<Int2IntMap> INV_SOUND_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invSoundMappings");
    private static final AttributeKey<Int2IntMap> INV_STATISTICS_MAPPINGS_KEY = AttributeKey.valueOf("multiconnect.invStatisticsMappings");

    @Override
    public boolean doesServerKnow(String registry, String entry) {
        int id = getRegistryId(registry, entry);
        if (registry.equals("minecraft:item") && id == 1) return true; // Stone *always* exists

        Channel channel = api.getCurrentChannel();
        if (channel == null) {
            return true;
        }
        UserConnection connection = getUserConnection(channel);
        if (connection == null) {
            return true;
        }
        for (final var protocol : connection.getProtocolInfo().getPipeline().pipes()) {
            if (registry.equals("minecraft:item")) {
                final var itemRewriter = protocol.getItemRewriter();
                if (itemRewriter != null) {
                    final Item newItem = itemRewriter.handleItemToServer(new DataItem(id, (byte)1, (short)0, null));
                    if (newItem != null) {
                        id = newItem.identifier();
                        if (id == 1) return false;
                        continue;
                    }
                }
            }

            MappingData mappingData = protocol.getMappingData();
            if (mappingData == null) continue;

            id = switch (registry) {
                case "minecraft:block" -> getOldId(channel, INV_BLOCK_MAPPINGS_KEY, mappingData.getBlockMappings(), id);
                case "minecraft:item" -> mappingData.getOldItemId(id);
                case "minecraft:entity_type" -> getOldId(channel, INV_ENTITY_MAPPINGS_KEY, mappingData.getEntityMappings(), id);
                case "minecraft:enchantment" -> getOldId(channel, INV_ENCHANTMENT_MAPPINGS_KEY, mappingData.getEnchantmentMappings(), id);
                case "minecraft:command_argument_type" -> getOldId(channel, INV_ARGUMENT_TYPE_MAPPINGS_KEY, mappingData.getArgumentTypeMappings(), id);
                case "minecraft:block_entity_type" -> getOldId(channel, INV_BLOCK_ENTITY_MAPPINGS_KEY, mappingData.getBlockEntityMappings(), id);
                case "minecraft:painting_variant" -> getOldId(channel, INV_PAINTING_MAPPINGS_KEY, mappingData.getPaintingMappings(), id);
                case "minecraft:particle_type" -> getOldId(channel, INV_PARTICLE_MAPPINGS_KEY, mappingData.getParticleMappings(), id);
                case "minecraft:sound_event" -> getOldId(channel, INV_SOUND_MAPPINGS_KEY, mappingData.getSoundMappings(), id);
                case "minecraft:custom_stat" -> getOldId(channel, INV_STATISTICS_MAPPINGS_KEY, mappingData.getStatisticsMappings(), id);
                default -> id;
            };
            if (id == -1 || (registry.equals("minecraft:item") && id == 1)) return false;
        }
        return true;
    }

    private static int getOldId(Channel channel, AttributeKey<Int2IntMap> key, @Nullable FullMappings mappings, int id) {
        if (mappings == null) {
            return id;
        }
        return getOldId(channel, key, mappings.mappings(), id);
    }

    private static int getOldId(Channel channel, AttributeKey<Int2IntMap> key, @Nullable Mappings mappings, int id) {
        if (mappings == null) {
            return id;
        }
        return getInverseMappings(channel, key, mappings).get(id);
    }

    @SuppressWarnings("unchecked")
    private static <T> int getRegistryId(String registry, String entry) {
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(new ResourceLocation(registry));
        if (reg == null) {
            throw new RuntimeException("Unknown registry: " + registry);
        }
        T value = reg.getOrThrow(ResourceKey.create(reg.key(), new ResourceLocation(entry)));
        return reg.getId(value);
    }

    @Override
    public void sendStringCustomPayload(Channel channel, String payloadChannel, ByteBuf payload) throws Exception {
        UserConnection connection = getUserConnection(channel);
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

    @Override
    public void sendOpenedInventory(Channel channel) throws Exception {
        UserConnection connection = getUserConnection(channel);
        PacketWrapper packet = Via.getManager().getProtocolManager().createPacketWrapper(
            ServerboundPackets1_9_3.CLIENT_STATUS,
            channel.alloc().buffer(),
            connection
        );
        packet.write(Type.VAR_INT, 2); // opened inventory
        packet.sendToServer(Protocol1_12To1_11_1.class);
    }
}
