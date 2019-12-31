package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.datafixers.Dynamic;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.IIdList;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.transformer.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.*;
import net.minecraft.client.util.TextFormat;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.packet.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Protocol_1_12_2 extends Protocol_1_13 {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, PaintingMotive> OLD_MOTIVE_NAMES = new HashMap<>();
    static {
        OLD_MOTIVE_NAMES.put("Kebab", PaintingMotive.KEBAB);
        OLD_MOTIVE_NAMES.put("Aztec", PaintingMotive.AZTEC);
        OLD_MOTIVE_NAMES.put("Alban", PaintingMotive.ALBAN);
        OLD_MOTIVE_NAMES.put("Aztec2", PaintingMotive.AZTEC2);
        OLD_MOTIVE_NAMES.put("Bomb", PaintingMotive.BOMB);
        OLD_MOTIVE_NAMES.put("Plant", PaintingMotive.PLANT);
        OLD_MOTIVE_NAMES.put("Wasteland", PaintingMotive.WASTELAND);
        OLD_MOTIVE_NAMES.put("Pool", PaintingMotive.POOL);
        OLD_MOTIVE_NAMES.put("Courbet", PaintingMotive.COURBET);
        OLD_MOTIVE_NAMES.put("Sea", PaintingMotive.SEA);
        OLD_MOTIVE_NAMES.put("Sunset", PaintingMotive.SUNSET);
        OLD_MOTIVE_NAMES.put("Creebet", PaintingMotive.CREEBET);
        OLD_MOTIVE_NAMES.put("Wanderer", PaintingMotive.WANDERER);
        OLD_MOTIVE_NAMES.put("Graham", PaintingMotive.GRAHAM);
        OLD_MOTIVE_NAMES.put("Match", PaintingMotive.MATCH);
        OLD_MOTIVE_NAMES.put("Bust", PaintingMotive.BUST);
        OLD_MOTIVE_NAMES.put("Stage", PaintingMotive.STAGE);
        OLD_MOTIVE_NAMES.put("Void", PaintingMotive.VOID);
        OLD_MOTIVE_NAMES.put("SkullAndRoses", PaintingMotive.SKULL_AND_ROSES);
        OLD_MOTIVE_NAMES.put("Wither", PaintingMotive.WITHER);
        OLD_MOTIVE_NAMES.put("Fighters", PaintingMotive.FIGHTERS);
        OLD_MOTIVE_NAMES.put("Pointer", PaintingMotive.POINTER);
        OLD_MOTIVE_NAMES.put("Pigscene", PaintingMotive.PIGSCENE);
        OLD_MOTIVE_NAMES.put("BurningSkull", PaintingMotive.BURNING_SKULL);
        OLD_MOTIVE_NAMES.put("Skeleton", PaintingMotive.SKELETON);
        OLD_MOTIVE_NAMES.put("DonkeyKong", PaintingMotive.DONKEY_KONG);
    }

    private static final Pattern NON_IDENTIFIER_CHARS = Pattern.compile("[^a-z0-9/._\\-]");

    private static final Field AREA_EFFECT_CLOUD_PARTICLE_ID = DataTrackerManager.getTrackedDataField(AreaEffectCloudEntity.class, 3, "PARTICLE_ID");
    private static final Field ENTITY_CUSTOM_NAME = DataTrackerManager.getTrackedDataField(Entity.class, 2, "CUSTOM_NAME");
    private static final Field BOAT_BUBBLE_WOBBLE_TICKS = DataTrackerManager.getTrackedDataField(BoatEntity.class, 6, "BUBBLE_WOBBLE_TICKS");
    private static final Field ZOMBIE_CONVERTING_IN_WATER = DataTrackerManager.getTrackedDataField(ZombieEntity.class, 2, "CONVERTING_IN_WATER");

    private static final TrackedData<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_ID = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1 = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2 = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> OLD_CUSTOM_NAME = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.STRING);

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int chunkX = buf.readInt();
            int chunkZ = buf.readInt();
            boolean fullChunk = buf.readBoolean();
            int verticalStripBitmask = buf.readVarInt();
            buf.disablePassthroughMode();
            int dataLength = buf.readVarInt();
            if (dataLength > 2097152)
                throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
            byte[] data = new byte[dataLength + 256 * 3];
            buf.readBytes(data, 0, dataLength);

            PacketByteBuf inBuf = new PacketByteBuf(Unpooled.wrappedBuffer(data.clone()));
            PacketByteBuf outBuf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
            outBuf.writerIndex(0);
            for (int sectionY = 0; sectionY < 16; sectionY++) {
                if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                    int paletteSize = inBuf.readByte();
                    outBuf.writeByte(paletteSize);
                    if (paletteSize <= 8) {
                        // array and bimap palette data look the same enough to use the same code here
                        int size = inBuf.readVarInt();
                        outBuf.writeVarInt(size);
                        for (int i = 0; i < size; i++) {
                            int stateId = inBuf.readVarInt();
                            if (Block.STATE_IDS.get(stateId) == null) {
                                LOGGER.warn("Got unknown state ID " + stateId + " in palette for chunk section (" + chunkX + ", " + sectionY + ", " + chunkZ + ")");
                                stateId = 0;
                            }
                            outBuf.writeVarInt(stateId);
                        }
                    } else {
                        inBuf.readVarInt();
                    }
                    long[] chunkData = new long[paletteSize * 64];
                    inBuf.readLongArray(chunkData);
                    outBuf.writeLongArray(chunkData);
                    byte[] light = new byte[16 * 16 * 16 / 2];
                    inBuf.readBytes(light);
                    outBuf.writeBytes(light);
                    // since this thread is async, this code may be run before the join game packet is
                    // processed, and therefore world would be null
                    while (MinecraftClient.getInstance().world == null) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    if (MinecraftClient.getInstance().world.dimension.hasSkyLight()) {
                        light = new byte[16 * 16 * 16 / 2];
                        inBuf.readBytes(light);
                        outBuf.writeBytes(light);
                    }
                }
            }

            // biomes
            if (fullChunk) {
                for (int i = 0; i < 256; i++)
                    outBuf.writeInt(inBuf.readByte() & 0xff);
            }

            buf.pendingRead(VarInt.class, new VarInt(outBuf.writerIndex()));
            buf.pendingRead(byte[].class, data);

            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(CustomPayloadS2CPacket.class, buf -> {
            String channel = buf.readString();
            Identifier newChannel;
            if ("MC|Brand".equals(channel)) {
                newChannel = CustomPayloadS2CPacket.BRAND;
            } else if ("MC|TrList".equals(channel)) {
                newChannel = Protocol_1_13_2.CUSTOM_PAYLOAD_TRADE_LIST;
            } else if ("MC|BOpen".equals(channel)) {
                newChannel = Protocol_1_13_2.CUSTOM_PAYLOAD_OPEN_BOOK;
            } else {
                newChannel = new Identifier(NON_IDENTIFIER_CHARS.matcher(channel.toLowerCase(Locale.ENGLISH)).replaceAll("_"));
            }
            buf.pendingRead(Identifier.class, newChannel);
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(PlaySoundIdS2CPacket.class, buf -> {
            buf.pendingRead(Identifier.class, new Identifier(buf.readString(256)));
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(MapUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // map id
            buf.readByte(); // map scale
            buf.readBoolean(); // show icons
            int iconCount = buf.readVarInt();
            buf.disablePassthroughMode();
            for (int i = 0; i < iconCount; i++) {
                int metadata = buf.readByte();
                buf.pendingRead(MapIcon.Type.class, MapIcon.Type.byId((byte) ((metadata >> 4) & 15)));
                buf.enablePassthroughMode();
                buf.readByte(); // icon x
                buf.readByte(); // icon y
                buf.disablePassthroughMode();
                buf.pendingRead(Byte.class, (byte) (metadata & 15)); // rotation
                buf.pendingRead(Boolean.class, false); // has text
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(ParticleS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            ParticleType<?> particleType = Registry.PARTICLE_TYPE.get(buf.readInt());
            if (particleType != ParticleTypes.ITEM && particleType != ParticleTypes.DUST) {
                buf.disablePassthroughMode();
                buf.applyPendingReads();
                return;
            }
            buf.readBoolean(); // long distance
            buf.readFloat(); // x
            buf.readFloat(); // y
            buf.readFloat(); // z
            float red = 0, green = 0, blue = 0;
            if (particleType == ParticleTypes.DUST) {
                buf.disablePassthroughMode();
                red = buf.readFloat();
                green = buf.readFloat();
                blue = buf.readFloat();
                buf.pendingRead(Float.class, 0f); // offset x
                buf.pendingRead(Float.class, 0f); // offset y
                buf.pendingRead(Float.class, 0f); // offset z
                buf.enablePassthroughMode();
            } else {
                buf.readFloat(); // offset x
                buf.readFloat(); // offset y
                buf.readFloat(); // offset z
            }
            buf.readFloat(); // speed
            buf.readInt(); // count
            buf.disablePassthroughMode();
            if (particleType == ParticleTypes.ITEM) {
                Item item = Registry.ITEM.get(buf.readVarInt());
                int meta = buf.readVarInt();
                ItemStack stack = Items_1_12_2.oldItemStackToNew(new ItemStack(item), meta);
                buf.pendingRead(ItemStack.class, stack);
            } else {
                buf.pendingRead(Float.class, red);
                buf.pendingRead(Float.class, green);
                buf.pendingRead(Float.class, blue);
                buf.pendingRead(Float.class, 1f); // scale
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(CraftFailedResponseS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readByte(); // sync id
            buf.disablePassthroughMode();
            buf.pendingRead(Identifier.class, new Identifier(String.valueOf(buf.readVarInt())));
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(UnlockRecipesS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            UnlockRecipesS2CPacket.Action action = buf.readEnumConstant(UnlockRecipesS2CPacket.Action.class);
            buf.readBoolean(); // gui open
            buf.readBoolean(); // filtering craftable
            buf.pendingRead(Boolean.class, false); // furnace gui open
            buf.pendingRead(Boolean.class, false); // furnace filtering craftable
            int idChangeCount = buf.readVarInt();
            buf.disablePassthroughMode();
            for (int i = 0; i < idChangeCount; i++) {
                buf.pendingRead(Identifier.class, new Identifier(String.valueOf(buf.readVarInt())));
            }
            if (action == UnlockRecipesS2CPacket.Action.INIT) {
                buf.enablePassthroughMode();
                int idInitCount = buf.readVarInt();
                buf.disablePassthroughMode();
                for (int i = 0; i < idInitCount; i++) {
                    buf.pendingRead(Identifier.class, new Identifier(String.valueOf(buf.readVarInt())));
                }
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(ScoreboardObjectiveUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(16); // name
            int mode = buf.readByte();
            buf.disablePassthroughMode();
            if (mode == 0 || mode == 2) {
                buf.pendingRead(Text.class, new LiteralText(buf.readString(32))); // display name
                String renderTypeName = buf.readString(16);
                ScoreboardCriterion.RenderType renderType = "hearts".equals(renderTypeName) ? ScoreboardCriterion.RenderType.HEARTS : ScoreboardCriterion.RenderType.INTEGER;
                buf.pendingRead(ScoreboardCriterion.RenderType.class, renderType);
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(PaintingSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // id
            buf.readUuid(); // uuid
            buf.disablePassthroughMode();
            PaintingMotive motive = OLD_MOTIVE_NAMES.getOrDefault(buf.readString(13), PaintingMotive.KEBAB);
            buf.pendingRead(VarInt.class, new VarInt(Registry.MOTIVE.getRawId(motive)));
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(StatisticsS2CPacket.class, buf -> {
            int count = buf.readVarInt();
            List<Pair<StatType<?>, Integer>> stats = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Identifier stat = new Identifier(buf.readString(32767));
                int value = buf.readVarInt();
                if (Registry.STAT_TYPE.containsId(stat))
                    stats.add(Pair.of(Registry.STAT_TYPE.get(stat), value));
            }
            buf.pendingRead(VarInt.class, new VarInt(stats.size()));
            for (Pair<StatType<?>, Integer> stat : stats) {
                buf.pendingRead(VarInt.class, new VarInt(Registry.STAT_TYPE.getRawId(stat.getLeft())));
                buf.pendingRead(VarInt.class, new VarInt(stat.getRight()));
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(CommandSuggestionsS2CPacket.class, buf -> {
            TabCompletionManager.Entry entry = TabCompletionManager.nextEntry();
            if (entry == null) {
                LOGGER.error("Received unrequested tab completion packet");
                int count = buf.readVarInt();
                for (int i = 0; i < count; i++)
                    buf.readString(32767);
                buf.pendingRead(VarInt.class, new VarInt(0)); // completion id
                buf.pendingRead(VarInt.class, new VarInt(0)); // range start
                buf.pendingRead(VarInt.class, new VarInt(0)); // range length
                buf.pendingRead(VarInt.class, new VarInt(0)); // suggestion count
                buf.applyPendingReads();
                return;
            }

            buf.pendingRead(VarInt.class, new VarInt(entry.getId())); // completion id
            String message = entry.getMessage();
            int start = message.lastIndexOf(' ') + 1;
            if (start == 0 && message.startsWith("/"))
                start = 1;
            buf.pendingRead(VarInt.class, new VarInt(start)); // range start
            buf.pendingRead(VarInt.class, new VarInt(message.length() - start)); // range length
            buf.enablePassthroughMode();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                buf.readString(32767); // suggestion
                buf.pendingRead(Boolean.class, false); // has tooltip
            }
            buf.disablePassthroughMode();
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(TeamS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(16); // team name
            int mode = buf.readByte();
            buf.disablePassthroughMode();
            if (mode == 0 || mode == 2) {
                buf.pendingRead(Text.class, new LiteralText(buf.readString(32))); // display name
                LiteralText prefix = new LiteralText(buf.readString(16));
                LiteralText suffix = new LiteralText(buf.readString(16));
                buf.enablePassthroughMode();
                buf.readByte(); // flags
                buf.readString(32); // name tag visibility rule
                buf.readString(32); // collision rule
                buf.disablePassthroughMode();
                int colorCode = buf.readByte();
                TextFormat color = TextFormat.RESET;
                if (colorCode >= 0 && colorCode < 16) {
                    char formatChar = "0123456789abcdef".charAt(colorCode);
                    for (TextFormat format : TextFormat.values()) {
                        if (format.getChar() == formatChar) {
                            color = format;
                            break;
                        }
                    }
                }
                buf.pendingRead(TextFormat.class, color);
                buf.pendingRead(Text.class, prefix);
                buf.pendingRead(Text.class, suffix);
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(BossBarS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readUuid(); // uuid
            BossBarS2CPacket.Type type = buf.readEnumConstant(BossBarS2CPacket.Type.class);
            buf.disablePassthroughMode();
            if (type == BossBarS2CPacket.Type.UPDATE_PROPERTIES) {
                int flags = buf.readUnsignedByte();
                buf.pendingRead(UnsignedByte.class, new UnsignedByte((short) (flags | ((flags & 2) << 1)))); // copy bit 2 to 4
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(ItemStack.class, new InboundTranslator<ItemStack>() {
            @Override
            public void onRead(TransformerByteBuf buf) {
                short itemId = buf.readShort();
                if (itemId == -1) {
                    buf.pendingRead(Short.class, itemId);
                    buf.applyPendingReads();
                    return;
                }
                byte count = buf.readByte();
                short meta = buf.readShort();
                CompoundTag tag = buf.readCompoundTag();
                if (tag == null)
                    tag = new CompoundTag();
                tag.putShort("Damage", meta);
                buf.pendingRead(Short.class, itemId);
                buf.pendingRead(Byte.class, count);
                buf.pendingRead(CompoundTag.class, tag);
                buf.applyPendingReads();
            }

            @Override
            public ItemStack translate(ItemStack from) {
                if (from.isEmpty())
                    return from;
                from = from.copy();
                int meta = from.getDamage();
                assert from.getTag() != null;
                from.getTag().remove("Damage");
                if (from.getTag().isEmpty())
                    from.setTag(null);
                return Items_1_12_2.oldItemStackToNew(from, meta);
            }
        });

        ProtocolRegistry.registerOutboundTranslator(CraftRequestC2SPacket.class, buf -> {
            buf.passthroughWrite(Integer.class); // sync id
            Supplier<Identifier> recipeId = buf.skipWrite(Identifier.class);
            buf.pendingWrite(VarInt.class, () -> {
                try {
                    return new VarInt(Integer.parseInt(recipeId.get().getPath()));
                } catch (NumberFormatException e) {
                    return new VarInt(0);
                }
            }, val -> buf.writeVarInt(val.get()));
        });

        ProtocolRegistry.registerOutboundTranslator(RecipeBookDataC2SPacket.class, buf -> {
            Supplier<RecipeBookDataC2SPacket.Mode> mode = buf.passthroughWrite(RecipeBookDataC2SPacket.Mode.class);
            buf.whenWrite(() -> {
                if (mode.get() == RecipeBookDataC2SPacket.Mode.SHOWN) {
                    Supplier<Identifier> recipeId = buf.skipWrite(Identifier.class);
                    buf.pendingWrite(Integer.class, () -> {
                        try {
                            return Integer.parseInt(recipeId.get().getPath());
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }, buf::writeInt);
                } else if (mode.get() == RecipeBookDataC2SPacket.Mode.SETTINGS) {
                    buf.passthroughWrite(Boolean.class); // is gui open
                    buf.passthroughWrite(Boolean.class); // filtering craftable
                    buf.skipWrite(Boolean.class); // furnace gui open
                    buf.skipWrite(Boolean.class); // furnace filtering craftable
                }
            });
        });

        ProtocolRegistry.registerOutboundTranslator(RequestCommandCompletionsC2SPacket.class, buf -> {
            Supplier<VarInt> completionId = buf.skipWrite(VarInt.class);
            Supplier<String> command = buf.skipWrite(String.class);

            buf.whenWrite(() -> TabCompletionManager.addTabCompletionRequest(completionId.get().get(), command.get()));
            buf.pendingWrite(String.class, command, val -> buf.writeString(val, 32767));
            HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
            boolean hasTarget = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK;
            buf.pendingWrite(Boolean.class, () -> hasTarget, buf::writeBoolean);
            if (hasTarget)
                buf.pendingWrite(BlockPos.class, ((BlockHitResult) hitResult)::getBlockPos, buf::writeBlockPos);
        });

        ProtocolRegistry.registerOutboundTranslator(ItemStack.class, new OutboundTranslator<ItemStack>() {
            @Override
            public void onWrite(TransformerByteBuf buf) {
                Supplier<Short> itemId = buf.skipWrite(Short.class);
                buf.whenWrite(() -> {
                    if (itemId.get() == -1) {
                        buf.pendingWrite(Short.class, itemId, (Consumer<Short>) buf::writeShort);
                    } else {
                        Supplier<Byte> count = buf.skipWrite(Byte.class);
                        Supplier<CompoundTag> tag = buf.skipWrite(CompoundTag.class);
                        buf.whenWrite(() -> {
                            CompoundTag oldTag = tag.get();
                            int meta = oldTag.getInt("Damage");
                            oldTag.remove("Damage");
                            buf.pendingWrite(Short.class, itemId, (Consumer<Short>) buf::writeShort);
                            buf.pendingWrite(Byte.class, count, (Consumer<Byte>) buf::writeByte);
                            buf.pendingWrite(Short.class, () -> (short) meta, (Consumer<Short>) buf::writeShort);
                            buf.pendingWrite(CompoundTag.class, () -> oldTag.isEmpty() ? null : oldTag, buf::writeCompoundTag);
                        });
                    }
                });
            }

            @Override
            public ItemStack translate(ItemStack from) {
                Pair<ItemStack, Integer> itemAndMeta = Items_1_12_2.newItemStackToOld(from);
                ItemStack to = itemAndMeta.getLeft();
                to.setDamage(itemAndMeta.getRight());
                return to;
            }
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, CommandSuggestionsS2CPacket.class);
        insertAfter(packets, DifficultyS2CPacket.class, PacketInfo.of(CommandSuggestionsS2CPacket.class, CommandSuggestionsS2CPacket::new));
        remove(packets, CommandTreeS2CPacket.class);
        remove(packets, TagQueryResponseS2CPacket.class);
        remove(packets, LookAtS2CPacket.class);
        remove(packets, StopSoundS2CPacket.class);
        remove(packets, SynchronizeRecipesS2CPacket.class);
        remove(packets, SynchronizeTagsS2CPacket.class);
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        remove(packets, RequestCommandCompletionsC2SPacket.class);
        insertAfter(packets, TeleportConfirmC2SPacket.class, PacketInfo.of(RequestCommandCompletionsC2SPacket.class, RequestCommandCompletionsC2SPacket::new));
        remove(packets, QueryBlockNbtC2SPacket.class);
        remove(packets, BookUpdateC2SPacket.class);
        remove(packets, QueryEntityNbtC2SPacket.class);
        remove(packets, PickFromInventoryC2SPacket.class);
        remove(packets, RenameItemC2SPacket.class);
        remove(packets, SelectVillagerTradeC2SPacket.class);
        remove(packets, UpdateBeaconC2SPacket.class);
        remove(packets, UpdateCommandBlockC2SPacket.class);
        remove(packets, UpdateCommandBlockMinecartC2SPacket.class);
        remove(packets, UpdateStructureBlockC2SPacket.class);
        remove(packets, CustomPayloadC2SPacket.class);
        insertAfter(packets, GuiCloseC2SPacket.class, PacketInfo.of(CustomPayloadC2SPacket_1_12_2.class, CustomPayloadC2SPacket_1_12_2::new));
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (!super.onSendPacket(packet))
            return false;
        if (packet.getClass() == QueryBlockNbtC2SPacket.class || packet.getClass() == QueryEntityNbtC2SPacket.class) {
            return false;
        }
        ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();
        assert connection != null;
        if (packet.getClass() == CustomPayloadC2SPacket.class) {
            ICustomPaylaodC2SPacket customPayload = (ICustomPaylaodC2SPacket) packet;
            String channel;
            if (customPayload.multiconnect_getChannel().equals(CustomPayloadC2SPacket.BRAND))
                channel = "MC|Brand";
            else
                channel = customPayload.multiconnect_getChannel().toString();
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2(channel, customPayload.multiconnect_getData()));
            return false;
        }
        if (packet.getClass() == BookUpdateC2SPacket.class) {
            BookUpdateC2SPacket bookUpdate = (BookUpdateC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeItemStack(bookUpdate.getBook());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2(bookUpdate.wasSigned() ? "MC|BSign" : "MC|BEdit", buf));
            return false;
        }
        if (packet.getClass() == RenameItemC2SPacket.class) {
            RenameItemC2SPacket renameItem = (RenameItemC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeString(renameItem.getItemName(), 32767);
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|ItemName", buf));
            return false;
        }
        if (packet.getClass() == SelectVillagerTradeC2SPacket.class) {
            SelectVillagerTradeC2SPacket selectTrade = (SelectVillagerTradeC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(selectTrade.method_12431());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|TrSel", buf));
            return false;
        }
        if (packet.getClass() == UpdateBeaconC2SPacket.class) {
            UpdateBeaconC2SPacket updateBeacon = (UpdateBeaconC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(updateBeacon.getPrimaryEffectId());
            buf.writeInt(updateBeacon.getSecondaryEffectId());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|Beacon", buf));
            return false;
        }
        if (packet.getClass() == UpdateCommandBlockC2SPacket.class) {
            UpdateCommandBlockC2SPacket updateCmdBlock = (UpdateCommandBlockC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(updateCmdBlock.getBlockPos().getX());
            buf.writeInt(updateCmdBlock.getBlockPos().getY());
            buf.writeInt(updateCmdBlock.getBlockPos().getZ());
            buf.writeString(updateCmdBlock.getCommand());
            buf.writeBoolean(updateCmdBlock.shouldTrackOutput());
            switch (updateCmdBlock.getType()) {
                case AUTO:
                    buf.writeString("AUTO");
                    break;
                case REDSTONE:
                    buf.writeString("REDSTONE");
                    break;
                case SEQUENCE:
                    buf.writeString("SEQUENCE");
                    break;
                default:
                    LOGGER.error("Unknown command block type: " + updateCmdBlock.getType());
                    return false;
            }
            buf.writeBoolean(updateCmdBlock.isConditional());
            buf.writeBoolean(updateCmdBlock.isAlwaysActive());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|AutoCmd", buf));
            return false;
        }
        if (packet.getClass() == UpdateCommandBlockMinecartC2SPacket.class) {
            UpdateCommandBlockMinecartC2SPacket updateCmdMinecart = (UpdateCommandBlockMinecartC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeByte(1); // command block type (minecart)
            buf.writeInt(((ICommandBlockMinecartC2SPacket) updateCmdMinecart).getEntityId());
            buf.writeString(updateCmdMinecart.getCommand());
            buf.writeBoolean(updateCmdMinecart.shouldTrackOutput());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|AdvCmd", buf));
            return false;
        }
        if (packet.getClass() == UpdateStructureBlockC2SPacket.class) {
            UpdateStructureBlockC2SPacket updateStructBlock = (UpdateStructureBlockC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(updateStructBlock.getPos().getX());
            buf.writeInt(updateStructBlock.getPos().getY());
            buf.writeInt(updateStructBlock.getPos().getZ());
            switch (updateStructBlock.getAction()) {
                case UPDATE_DATA:
                    buf.writeByte(1);
                    break;
                case SAVE_AREA:
                    buf.writeByte(2);
                    break;
                case LOAD_AREA:
                    buf.writeByte(3);
                    break;
                case SCAN_AREA:
                    buf.writeByte(4);
                    break;
                default:
                    LOGGER.error("Unknown structure block action: " + updateStructBlock.getAction());
                    return false;
            }
            switch (updateStructBlock.getMode()) {
                case SAVE:
                    buf.writeString("SAVE");
                    break;
                case LOAD:
                     buf.writeString("LOAD");
                     break;
                case CORNER:
                    buf.writeString("CORNER");
                    break;
                case DATA:
                    buf.writeString("DATA");
                    break;
                default:
                    LOGGER.error("Unknown structure block mode: " + updateStructBlock.getMode());
                    return false;
            }
            buf.writeString(updateStructBlock.getStructureName());
            buf.writeInt(updateStructBlock.getOffset().getX());
            buf.writeInt(updateStructBlock.getOffset().getY());
            buf.writeInt(updateStructBlock.getOffset().getZ());
            buf.writeInt(updateStructBlock.getSize().getX());
            buf.writeInt(updateStructBlock.getSize().getY());
            buf.writeInt(updateStructBlock.getSize().getZ());
            switch (updateStructBlock.getMirror()) {
                case NONE:
                    buf.writeString("NONE");
                    break;
                case LEFT_RIGHT:
                    buf.writeString("LEFT_RIGHT");
                    break;
                case FRONT_BACK:
                    buf.writeString("FRONT_BACK");
                    break;
                default:
                    LOGGER.error("Unknown mirror: " + updateStructBlock.getMirror());
                    return false;
            }
            switch (updateStructBlock.getRotation()) {
                case NONE:
                    buf.writeString("NONE");
                    break;
                case CLOCKWISE_90:
                    buf.writeString("CLOCKWISE_90");
                    break;
                case CLOCKWISE_180:
                    buf.writeString("CLOCKWISE_180");
                    break;
                case COUNTERCLOCKWISE_90:
                    buf.writeString("COUNTERCLOCKWISE_90");
                    break;
                default:
                    LOGGER.error("Unknown rotation: " + updateStructBlock.getRotation());
                    return false;
            }
            buf.writeString(updateStructBlock.getMetadata());
            buf.writeBoolean(updateStructBlock.getIgnoreEntities());
            buf.writeBoolean(updateStructBlock.shouldShowAir());
            buf.writeBoolean(updateStructBlock.shouldShowBoundingBox());
            buf.writeFloat(updateStructBlock.getIntegrity());
            buf.writeVarLong(updateStructBlock.getSeed());
            // have fun with all that, server!
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|Struct", buf));
            return false;
        }

        return true;
    }

    @Override
    protected void removeTrackedDataHandlers() {
        super.removeTrackedDataHandlers();
        removeTrackedDataHandler(TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (!super.acceptEntityData(clazz, data))
            return false;

        if (clazz == AreaEffectCloudEntity.class && data == DataTrackerManager.getTrackedData(ParticleEffect.class, AREA_EFFECT_CLOUD_PARTICLE_ID)) {
            DataTrackerManager.registerOldTrackedData(AreaEffectCloudEntity.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_ID,
                    Registry.PARTICLE_TYPE.getRawId(ParticleTypes.ENTITY_EFFECT),
                    (entity, val) -> {
                ParticleType<?> type = Registry.PARTICLE_TYPE.get(val);
                if (type == null)
                    type = ParticleTypes.ENTITY_EFFECT;
                setParticleType(entity, type);
            });
            DataTrackerManager.registerOldTrackedData(AreaEffectCloudEntity.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1,
                    0,
                    (entity, val) -> {
                ((IAreaEffectCloudEntity) entity).multiconnect_setParam1(val);
                setParticleType(entity, entity.getParticleType().getType());
            });
            DataTrackerManager.registerOldTrackedData(AreaEffectCloudEntity.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2,
                    0,
                    (entity, val) -> {
                ((IAreaEffectCloudEntity) entity).multiconnect_setParam2(val);
                setParticleType(entity, entity.getParticleType().getType());
            });
            return false;
        }

        if (clazz == Entity.class && data == DataTrackerManager.getTrackedData(Optional.class, ENTITY_CUSTOM_NAME)) {
            DataTrackerManager.registerOldTrackedData(Entity.class, OLD_CUSTOM_NAME, "",
                    (entity, val) -> entity.setCustomName(val.isEmpty() ? null : new LiteralText(val)));
            return false;
        }

        if (clazz == BoatEntity.class && data == DataTrackerManager.getTrackedData(Integer.class, BOAT_BUBBLE_WOBBLE_TICKS)) {
            return false;
        }

        if (clazz == ZombieEntity.class && data == DataTrackerManager.getTrackedData(Boolean.class, ZOMBIE_CONVERTING_IN_WATER)) {
            return false;
        }

        return true;
    }

    private static void setParticleType(AreaEffectCloudEntity entity, ParticleType<?> type) {
        IAreaEffectCloudEntity iaece = (IAreaEffectCloudEntity) entity;
        if (type.getParametersFactory() == ItemStackParticleEffect.PARAMETERS_FACTORY) {
            Item item = Registry.ITEM.get(iaece.multiconnect_getParam1());
            int meta = iaece.multiconnect_getParam2();
            ItemStack stack = Items_1_12_2.oldItemStackToNew(new ItemStack(item), meta);
            entity.setParticleType(createParticle(type, buf -> buf.writeItemStack(stack)));
        } else if (type.getParametersFactory() == BlockStateParticleEffect.PARAMETERS_FACTORY) {
            entity.setParticleType(createParticle(type, buf -> buf.writeVarInt(iaece.multiconnect_getParam1())));
        } else if (type.getParametersFactory() == DustParticleEffect.PARAMETERS_FACTORY) {
            entity.setParticleType(createParticle(type, buf -> {
                buf.writeFloat(1);
                buf.writeFloat(0);
                buf.writeFloat(0);
                buf.writeFloat(1);
            }));
        } else {
            entity.setParticleType(createParticle(type, buf -> {}));
        }
    }

    private static <T extends ParticleEffect> T createParticle(ParticleType<T> type, Consumer<PacketByteBuf> function) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        function.accept(buf);
        return type.getParametersFactory().read(type, buf);
    }

    @Override
    protected void recomputeBlockStates() {
        final int skullId = Registry.BLOCK.getRawId(Blocks.SKELETON_SKULL);

        ((IIdList) Block.STATE_IDS).clear();
        Set<BlockState> addedStates = new HashSet<>();
        for (int blockId = 0; blockId < 256; blockId++) {
            if (blockId == skullId) {
                final Direction[] dirs = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP};
                for (int meta = 0; meta < 16; meta++) {
                    Direction dir = dirs[meta & 7];
                    BlockState state;
                    if (dir == Direction.DOWN || dir == Direction.UP) {
                        state = Blocks.SKELETON_SKULL.getDefaultState();
                    } else {
                        state = Blocks.SKELETON_WALL_SKULL.getDefaultState().with(WallSkullBlock.FACING, dir);
                    }
                    Block.STATE_IDS.set(state, blockId << 4 | meta);
                    addedStates.add(state);
                }
            } else {
                for (int meta = 0; meta < 16; meta++) {
                    Dynamic<?> dynamicState = BlockStateFlattening.lookupState(blockId << 4 | meta);
                    String fixedName = dynamicState.get("Name").asString("");
                    Block block = Registry.BLOCK.get(new Identifier(fixedName));
                    if (block == Blocks.AIR && blockId != 0) {
                        dynamicState = BlockStateReverseFlattening.reverseLookupState(blockId << 4 | meta);
                        fixedName = dynamicState.get("Name").asString("");
                        block = Registry.BLOCK.get(new Identifier(fixedName));
                    }
                    if (block != Blocks.AIR || blockId == 0) {
                        if (block == Blocks.GRASS && meta == 0) block = Blocks.DEAD_BUSH;
                        StateManager<Block, BlockState> stateManager = block.getStateManager();
                        BlockState state = block.getDefaultState();
                        for (Map.Entry<String, String> entry : dynamicState.get("Properties").asMap(k -> k.asString(""), v -> v.asString("")).entrySet()) {
                            state = addProperty(stateManager, state, entry.getKey(), entry.getValue());
                        }
                        Block.STATE_IDS.set(state, blockId << 4 | meta);
                        addedStates.add(state);
                    }
                }
            }
        }
        for (Block block : Registry.BLOCK) {
            for (BlockState state : block.getStateManager().getStates()) {
                if (!addedStates.contains(state) && acceptBlockState(state)) {
                    Block.STATE_IDS.add(state);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState addProperty(StateManager<Block, BlockState> stateManager, BlockState state, String propName, String valName) {
        Property<T> prop = (Property<T>) stateManager.getProperty(propName);
        return prop == null ? state : state.with(prop, prop.parse(valName).orElseGet(() -> state.get(prop)));
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        super.modifyRegistry(registry);

        // just fucking nuke them all, it's the flattening after all
        if (registry == Registry.BLOCK) {
            Blocks_1_12_2.registerBlocks((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            Items_1_12_2.registerItems((ISimpleRegistry<Item>) registry);
        } else if (registry == Registry.ENTITY_TYPE) {
            Entities_1_12_2.registerEntities((ISimpleRegistry<EntityType<?>>) registry);
        } else if (registry == Registry.ENCHANTMENT) {
            Enchantments_1_12_2.registerEnchantments((ISimpleRegistry<Enchantment>) registry);
        } else if (registry == Registry.POTION) {
            modifyPotionRegistry((ISimpleRegistry<Potion>) registry);
        } else if (registry == Registry.BIOME) {
            modifyBiomeRegistry((ISimpleRegistry<Biome>) registry);
        } else if (registry == Registry.PARTICLE_TYPE) {
            Particles_1_12_2.registerParticles((ISimpleRegistry<ParticleType<?>>) registry);
        } else if (registry == Registry.BLOCK_ENTITY) {
            BlockEntities_1_12_2.registerBlockEntities((ISimpleRegistry<BlockEntityType<?>>) registry);
        } else if (registry == Registry.STATUS_EFFECT) {
            modifyStatusEffectRegistry((ISimpleRegistry<StatusEffect>) registry);
        } else if (registry == Registry.SOUND_EVENT) {
            modifySoundRegistry((ISimpleRegistry<SoundEvent>) registry);
        }
    }

    private static void modifyPotionRegistry(ISimpleRegistry<Potion> registry) {
        registry.unregister(Potions.STRONG_SLOWNESS);
        registry.unregister(Potions.TURTLE_MASTER);
        registry.unregister(Potions.LONG_TURTLE_MASTER);
        registry.unregister(Potions.STRONG_TURTLE_MASTER);
        registry.unregister(Potions.SLOW_FALLING);
        registry.unregister(Potions.LONG_SLOW_FALLING);
    }

    private static void modifyBiomeRegistry(ISimpleRegistry<Biome> registry) {
        rename(registry, Biomes.MOUNTAINS, "extreme_hills");
        rename(registry, Biomes.SWAMP, "swampland");
        rename(registry, Biomes.NETHER, "hell");
        rename(registry, Biomes.THE_END, "sky");
        rename(registry, Biomes.SNOWY_TUNDRA, "ice_flats");
        rename(registry, Biomes.SNOWY_MOUNTAINS, "ice_mountains");
        rename(registry, Biomes.MUSHROOM_FIELDS, "mushroom_island");
        rename(registry, Biomes.MUSHROOM_FIELD_SHORE, "mushroom_island_shore");
        rename(registry, Biomes.BEACH, "beaches");
        rename(registry, Biomes.WOODED_HILLS, "forest_hills");
        rename(registry, Biomes.MOUNTAIN_EDGE, "smaller_extreme_hills");
        rename(registry, Biomes.STONE_SHORE, "stone_beach");
        rename(registry, Biomes.SNOWY_BEACH, "cold_beach");
        rename(registry, Biomes.DARK_FOREST, "roofed_forest");
        rename(registry, Biomes.SNOWY_TAIGA, "taiga_cold");
        rename(registry, Biomes.SNOWY_TAIGA_HILLS, "taiga_cold_hills");
        rename(registry, Biomes.GIANT_TREE_TAIGA, "redwood_taiga");
        rename(registry, Biomes.GIANT_TREE_TAIGA_HILLS, "redwood_taiga_hills");
        rename(registry, Biomes.WOODED_MOUNTAINS, "extreme_hills_with_trees");
        rename(registry, Biomes.SAVANNA_PLATEAU, "savanna_rock");
        rename(registry, Biomes.BADLANDS, "mesa");
        rename(registry, Biomes.WOODED_BADLANDS_PLATEAU, "mesa_rock");
        rename(registry, Biomes.BADLANDS_PLATEAU, "mesa_clear_rock");
        registry.purge(Biomes.SMALL_END_ISLANDS);
        registry.purge(Biomes.END_MIDLANDS);
        registry.purge(Biomes.END_HIGHLANDS);
        registry.purge(Biomes.END_BARRENS);
        registry.purge(Biomes.WARM_OCEAN);
        registry.purge(Biomes.LUKEWARM_OCEAN);
        registry.purge(Biomes.COLD_OCEAN);
        registry.purge(Biomes.DEEP_WARM_OCEAN);
        registry.purge(Biomes.DEEP_LUKEWARM_OCEAN);
        registry.purge(Biomes.DEEP_WARM_OCEAN);
        registry.purge(Biomes.DEEP_LUKEWARM_OCEAN);
        registry.purge(Biomes.DEEP_COLD_OCEAN);
        registry.purge(Biomes.DEEP_FROZEN_OCEAN);
        rename(registry, Biomes.THE_VOID, "void");
        rename(registry, Biomes.SUNFLOWER_PLAINS, "mutated_plains");
        rename(registry, Biomes.DESERT_LAKES, "mutated_desert");
        rename(registry, Biomes.GRAVELLY_MOUNTAINS, "mutated_extreme_hills");
        rename(registry, Biomes.FLOWER_FOREST, "mutated_forest");
        rename(registry, Biomes.TAIGA_MOUNTAINS, "mutated_taiga");
        rename(registry, Biomes.SWAMP_HILLS, "mutated_swampland");
        rename(registry, Biomes.ICE_SPIKES, "mutated_ice_flats");
        rename(registry, Biomes.MODIFIED_JUNGLE, "mutated_jungle");
        rename(registry, Biomes.MODIFIED_JUNGLE_EDGE, "mutated_jungle_edge");
        rename(registry, Biomes.TALL_BIRCH_FOREST, "mutated_birch_forest");
        rename(registry, Biomes.TALL_BIRCH_HILLS, "mutated_birch_forest_hills");
        rename(registry, Biomes.DARK_FOREST_HILLS, "mutated_roofed_forest_hills");
        rename(registry, Biomes.SNOWY_TAIGA_MOUNTAINS, "mutated_taiga_cold");
        rename(registry, Biomes.GIANT_SPRUCE_TAIGA, "mutated_redwood_taiga");
        rename(registry, Biomes.GIANT_SPRUCE_TAIGA_HILLS, "mutated_redwood_taiga_hills");
        rename(registry, Biomes.MODIFIED_GRAVELLY_MOUNTAINS, "mutated_extreme_hills_with_trees");
        rename(registry, Biomes.SHATTERED_SAVANNA, "mutated_savanna");
        rename(registry, Biomes.SHATTERED_SAVANNA_PLATEAU, "mutated_savanna_rock");
        rename(registry, Biomes.ERODED_BADLANDS, "mutated_mesa");
        rename(registry, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, "mutated_mesa_rock");
        rename(registry, Biomes.MODIFIED_BADLANDS_PLATEAU, "mutated_mesa_clear_rock");
    }

    private static void modifyStatusEffectRegistry(ISimpleRegistry<StatusEffect> registry) {
        registry.unregister(StatusEffects.SLOW_FALLING);
        registry.unregister(StatusEffects.CONDUIT_POWER);
        registry.unregister(StatusEffects.DOLPHINS_GRACE);
    }

    private static void modifySoundRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.AMBIENT_UNDERWATER_ENTER);
        registry.unregister(SoundEvents.AMBIENT_UNDERWATER_EXIT);
        registry.unregister(SoundEvents.AMBIENT_UNDERWATER_LOOP);
        registry.unregister(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS);
        registry.unregister(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE);
        registry.unregister(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE);
        registry.unregister(SoundEvents.BLOCK_BEACON_ACTIVATE);
        registry.unregister(SoundEvents.BLOCK_BEACON_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_BEACON_DEACTIVATE);
        registry.unregister(SoundEvents.BLOCK_BEACON_POWER_SELECT);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_ACTIVATE);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_DEACTIVATE);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_BREAK);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_FALL);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_HIT);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_PLACE);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_STEP);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_PUMPKIN_CARVE);
        registry.unregister(SoundEvents.ENTITY_COD_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_COD_DEATH);
        registry.unregister(SoundEvents.ENTITY_COD_FLOP);
        registry.unregister(SoundEvents.ENTITY_COD_HURT);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_AMBIENT_WATER);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_ATTACK);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_DEATH);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_EAT);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_HURT);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_JUMP);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_PLAY);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_SPLASH);
        registry.unregister(SoundEvents.ENTITY_DOLPHIN_SWIM);
        registry.unregister(SoundEvents.ENTITY_DROWNED_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_DROWNED_AMBIENT_WATER);
        registry.unregister(SoundEvents.ENTITY_DROWNED_DEATH);
        registry.unregister(SoundEvents.ENTITY_DROWNED_DEATH_WATER);
        registry.unregister(SoundEvents.ENTITY_DROWNED_HURT);
        registry.unregister(SoundEvents.ENTITY_DROWNED_HURT_WATER);
        registry.unregister(SoundEvents.ENTITY_DROWNED_SHOOT);
        registry.unregister(SoundEvents.ENTITY_DROWNED_STEP);
        registry.unregister(SoundEvents.ENTITY_DROWNED_SWIM);
        registry.unregister(SoundEvents.ENTITY_FISH_SWIM);
        registry.unregister(SoundEvents.ENTITY_HUSK_CONVERTED_TO_ZOMBIE);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_DROWNED);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_PHANTOM);
        registry.unregister(SoundEvents.ENTITY_PHANTOM_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PHANTOM_BITE);
        registry.unregister(SoundEvents.ENTITY_PHANTOM_DEATH);
        registry.unregister(SoundEvents.ENTITY_PHANTOM_FLAP);
        registry.unregister(SoundEvents.ENTITY_PHANTOM_HURT);
        registry.unregister(SoundEvents.ENTITY_PHANTOM_SWOOP);
        registry.unregister(SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_DEATH);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_FLOP);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_HURT);
        registry.unregister(SoundEvents.ENTITY_PUFFER_FISH_STING);
        registry.unregister(SoundEvents.ENTITY_SALMON_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_SALMON_DEATH);
        registry.unregister(SoundEvents.ENTITY_SALMON_FLOP);
        registry.unregister(SoundEvents.ENTITY_SALMON_HURT);
        registry.unregister(SoundEvents.ENTITY_SKELETON_HORSE_SWIM);
        registry.unregister(SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT_WATER);
        registry.unregister(SoundEvents.ENTITY_SKELETON_HORSE_GALLOP_WATER);
        registry.unregister(SoundEvents.ENTITY_SKELETON_HORSE_JUMP_WATER);
        registry.unregister(SoundEvents.ENTITY_SKELETON_HORSE_STEP_WATER);
        registry.unregister(SoundEvents.ENTITY_SQUID_SQUIRT);
        registry.unregister(SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_TROPICAL_FISH_DEATH);
        registry.unregister(SoundEvents.ENTITY_TROPICAL_FISH_FLOP);
        registry.unregister(SoundEvents.ENTITY_TROPICAL_FISH_HURT);
        registry.unregister(SoundEvents.ENTITY_TURTLE_AMBIENT_LAND);
        registry.unregister(SoundEvents.ENTITY_TURTLE_DEATH);
        registry.unregister(SoundEvents.ENTITY_TURTLE_DEATH_BABY);
        registry.unregister(SoundEvents.ENTITY_TURTLE_EGG_BREAK);
        registry.unregister(SoundEvents.ENTITY_TURTLE_EGG_CRACK);
        registry.unregister(SoundEvents.ENTITY_TURTLE_EGG_HATCH);
        registry.unregister(SoundEvents.ENTITY_TURTLE_HURT);
        registry.unregister(SoundEvents.ENTITY_TURTLE_HURT_BABY);
        registry.unregister(SoundEvents.ENTITY_TURTLE_LAY_EGG);
        registry.unregister(SoundEvents.ENTITY_TURTLE_SHAMBLE);
        registry.unregister(SoundEvents.ENTITY_TURTLE_SHAMBLE_BABY);
        registry.unregister(SoundEvents.ENTITY_TURTLE_SWIM);
        registry.unregister(SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED);
        registry.unregister(SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE);
        registry.unregister(SoundEvents.ITEM_AXE_STRIP);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_FISH);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_FISH);
        registry.unregister(SoundEvents.ITEM_TRIDENT_HIT);
        registry.unregister(SoundEvents.ITEM_TRIDENT_HIT_GROUND);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RETURN);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RIPTIDE_1);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RIPTIDE_2);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RIPTIDE_3);
        registry.unregister(SoundEvents.ITEM_TRIDENT_THROW);
        registry.unregister(SoundEvents.ITEM_TRIDENT_THUNDER);
        registry.unregister(SoundEvents.MUSIC_UNDER_WATER);
    }
}
