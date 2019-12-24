package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.datafixers.Dynamic;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.IIdList;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.transformer.CustomPayload;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.earthcomputer.multiconnect.transformer.UnsignedByte;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.*;
import net.minecraft.client.util.TextFormat;
import net.minecraft.datafixers.fixes.BlockStateFlattening;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.packet.*;
import net.minecraft.stat.StatType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Protocol_1_12_2 extends Protocol_1_13 {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerTranslators() {
        // TODO: chunk data

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
                newChannel = new Identifier(channel);
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
            if (particleType != ParticleTypes.ITEM) {
                buf.disablePassthroughMode();
                buf.applyPendingReads();
                return;
            }
            buf.readBoolean(); // long distance
            buf.readFloat(); // x
            buf.readFloat(); // y
            buf.readFloat(); // z
            buf.readFloat(); // offset x
            buf.readFloat(); // offset y
            buf.readFloat(); // offset z
            buf.readFloat(); // speed
            buf.readInt(); // count
            buf.disablePassthroughMode();
            Item item = Registry.ITEM.get(buf.readVarInt());
            int meta = buf.readVarInt();
            ItemStack stack = Items_1_12_2.oldItemStackToNew(new ItemStack(item), meta);
            buf.pendingRead(ItemStack.class, stack);
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
            PaintingMotive motive = Registry.MOTIVE.get(new Identifier(buf.readString(13)));
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

        ProtocolRegistry.registerInboundTranslator(ItemStack.class, buf -> {
            short itemId = buf.readShort();
            if (itemId == -1) {
                buf.pendingRead(Short.class, itemId);
                buf.applyPendingReads();
                return;
            }
            byte count = buf.readByte();
            short meta = buf.readShort();
            ItemStack stack = new ItemStack(Registry.ITEM.get(itemId), count);
            stack.setTag(buf.readCompoundTag());
            stack = Items_1_12_2.oldItemStackToNew(stack, meta);
            buf.pendingRead(Short.class, (short)Registry.ITEM.getRawId(stack.getItem()));
            buf.pendingRead(Byte.class, count);
            buf.pendingRead(CompoundTag.class, stack.getTag());
            buf.applyPendingReads();
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

        ProtocolRegistry.registerOutboundTranslator(ItemStack.class, buf -> {
            Supplier<Short> itemId = buf.skipWrite(Short.class);
            buf.whenWrite(() -> {
                if (itemId.get() == -1) {
                    buf.pendingWrite(Short.class, itemId, (Consumer<Short>) buf::writeShort);
                } else {
                    Supplier<Byte> count = buf.skipWrite(Byte.class);
                    Supplier<CompoundTag> tag = buf.skipWrite(CompoundTag.class);
                    buf.whenWrite(() -> {
                        ItemStack stack = new ItemStack(Registry.ITEM.get(itemId.get()), count.get());
                        stack.setTag(tag.get());
                        Pair<ItemStack, Integer> oldStackAndMeta = Items_1_12_2.newItemStackToOld(stack);
                        buf.pendingWrite(Short.class, () -> (short) Registry.ITEM.getRawId(oldStackAndMeta.getLeft().getItem()), (Consumer<Short>) buf::writeShort);
                        buf.pendingWrite(Byte.class, count, (Consumer<Byte>) buf::writeByte);
                        buf.pendingWrite(Short.class, () -> (short) oldStackAndMeta.getRight().intValue(), (Consumer<Short>) buf::writeShort);
                        buf.pendingWrite(CompoundTag.class, oldStackAndMeta.getLeft()::getTag, buf::writeCompoundTag);
                    });
                }
            });
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
        List<PacketInfo<?>> packets = super.getClientboundPackets();
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
    protected void recomputeBlockStates() {
        ((IIdList) Block.STATE_IDS).clear();
        Set<BlockState> addedStates = new HashSet<>();
        for (int blockId = 0; blockId < 256; blockId++) {
            for (int meta = 0; meta < 16; meta++) {
                Dynamic<?> dynamicState = BlockStateFlattening.lookupState(blockId << 16 | meta);
                Block block = Registry.BLOCK.get(new Identifier(dynamicState.get("Name").asString("")));
                if (block != Blocks.AIR || blockId == 0) {
                    StateManager<Block, BlockState> stateManager = block.getStateManager();
                    BlockState state = block.getDefaultState();
                    for (Map.Entry<String, String> entry : dynamicState.get("Properties").asMap(k -> k.asString(""), v -> v.asString("")).entrySet()) {
                        state = addProperty(stateManager, state, entry.getKey(), entry.getValue());
                    }
                    Block.STATE_IDS.set(state, blockId << 16 | meta);
                    addedStates.add(state);
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
            modifyEntityTypeRegistry((ISimpleRegistry<EntityType<?>>) registry);
        }
    }

    private void modifyEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.clear(true);

        registry.register(EntityType.ITEM, 1, new Identifier("item"));
        registry.register(EntityType.EXPERIENCE_ORB, 2, new Identifier("xp_orb"));
        registry.register(EntityType.AREA_EFFECT_CLOUD, 3, new Identifier("area_effect_cloud"));
        registry.register(EntityType.ELDER_GUARDIAN, 4, new Identifier("elder_guardian"));
        registry.register(EntityType.WITHER_SKELETON, 5, new Identifier("wither_skeleton"));
        registry.register(EntityType.STRAY, 6, new Identifier("stray"));
        registry.register(EntityType.EGG, 7, new Identifier("egg"));
        registry.register(EntityType.LEASH_KNOT, 8, new Identifier("leash_knot"));
        registry.register(EntityType.PAINTING, 9, new Identifier("painting"));
        registry.register(EntityType.ARROW, 10, new Identifier("arrow"));
        registry.register(EntityType.SNOWBALL, 11, new Identifier("snowball"));
        registry.register(EntityType.FIREBALL, 12, new Identifier("fireball"));
        registry.register(EntityType.SMALL_FIREBALL, 13, new Identifier("small_fireball"));
        registry.register(EntityType.ENDER_PEARL, 14, new Identifier("ender_pearl"));
        registry.register(EntityType.EYE_OF_ENDER, 15, new Identifier("eye_of_ender_signal"));
        registry.register(EntityType.POTION, 16, new Identifier("potion"));
        registry.register(EntityType.EXPERIENCE_BOTTLE, 17, new Identifier("xp_bottle"));
        registry.register(EntityType.ITEM_FRAME, 18, new Identifier("item_frame"));
        registry.register(EntityType.WITHER_SKULL, 19, new Identifier("wither_skull"));
        registry.register(EntityType.TNT, 20, new Identifier("tnt"));
        registry.register(EntityType.FALLING_BLOCK, 21, new Identifier("falling_block"));
        registry.register(EntityType.FIREWORK_ROCKET, 22, new Identifier("fireworks_rocket"));
        registry.register(EntityType.HUSK, 23, new Identifier("husk"));
        registry.register(EntityType.SPECTRAL_ARROW, 24, new Identifier("spectral_arrow"));
        registry.register(EntityType.SHULKER_BULLET, 25, new Identifier("shulker_bullet"));
        registry.register(EntityType.DRAGON_FIREBALL, 26, new Identifier("dragon_fireball"));
        registry.register(EntityType.ZOMBIE_VILLAGER, 27, new Identifier("zombie_villager"));
        registry.register(EntityType.SKELETON_HORSE, 28, new Identifier("skeleton_horse"));
        registry.register(EntityType.ZOMBIE_HORSE, 29, new Identifier("zombie_horse"));
        registry.register(EntityType.ARMOR_STAND, 30, new Identifier("armor_stand"));
        registry.register(EntityType.DONKEY, 31, new Identifier("donkey"));
        registry.register(EntityType.MULE, 32, new Identifier("mule"));
        registry.register(EntityType.EVOKER_FANGS, 33, new Identifier("evocation_fangs"));
        registry.register(EntityType.EVOKER, 34, new Identifier("evocation_illager"));
        registry.register(EntityType.VEX, 35, new Identifier("vex"));
        registry.register(EntityType.VINDICATOR, 36, new Identifier("vindication_illager"));
        registry.register(EntityType.ILLUSIONER, 37, new Identifier("illusion_illager"));
        registry.register(EntityType.COMMAND_BLOCK_MINECART, 40, new Identifier("commandblock_minecart"));
        registry.register(EntityType.BOAT, 41, new Identifier("boat"));
        registry.register(EntityType.MINECART, 42, new Identifier("minecart"));
        registry.register(EntityType.CHEST_MINECART, 43, new Identifier("chest_minecart"));
        registry.register(EntityType.FURNACE_MINECART, 44, new Identifier("furnace_minecart"));
        registry.register(EntityType.TNT_MINECART, 45, new Identifier("tnt_minecart"));
        registry.register(EntityType.HOPPER_MINECART, 46, new Identifier("hopper_minecart"));
        registry.register(EntityType.SPAWNER_MINECART, 47, new Identifier("spawner_minecart"));
        registry.register(EntityType.CREEPER, 50, new Identifier("creeper"));
        registry.register(EntityType.SKELETON, 51, new Identifier("skeleton"));
        registry.register(EntityType.SPIDER, 52, new Identifier("spider"));
        registry.register(EntityType.GIANT, 53, new Identifier("giant"));
        registry.register(EntityType.ZOMBIE, 54, new Identifier("zombie"));
        registry.register(EntityType.SLIME, 55, new Identifier("slime"));
        registry.register(EntityType.GHAST, 56, new Identifier("ghast"));
        registry.register(EntityType.ZOMBIE_PIGMAN, 57, new Identifier("zombie_pigman"));
        registry.register(EntityType.ENDERMAN, 58, new Identifier("enderman"));
        registry.register(EntityType.CAVE_SPIDER, 59, new Identifier("cave_spider"));
        registry.register(EntityType.SILVERFISH, 60, new Identifier("silverfish"));
        registry.register(EntityType.BLAZE, 61, new Identifier("blaze"));
        registry.register(EntityType.MAGMA_CUBE, 62, new Identifier("magma_cube"));
        registry.register(EntityType.ENDER_DRAGON, 63, new Identifier("ender_dragon"));
        registry.register(EntityType.WITHER, 64, new Identifier("wither"));
        registry.register(EntityType.BAT, 65, new Identifier("bat"));
        registry.register(EntityType.WITCH, 66, new Identifier("witch"));
        registry.register(EntityType.ENDERMITE, 67, new Identifier("endermite"));
        registry.register(EntityType.GUARDIAN, 68, new Identifier("guardian"));
        registry.register(EntityType.SHULKER, 69, new Identifier("shulker"));
        registry.register(EntityType.PIG, 90, new Identifier("pig"));
        registry.register(EntityType.SHEEP, 91, new Identifier("sheep"));
        registry.register(EntityType.COW, 92, new Identifier("cow"));
        registry.register(EntityType.CHICKEN, 93, new Identifier("chicken"));
        registry.register(EntityType.SQUID, 94, new Identifier("squid"));
        registry.register(EntityType.WOLF, 95, new Identifier("wolf"));
        registry.register(EntityType.MOOSHROOM, 96, new Identifier("mooshroom"));
        registry.register(EntityType.SNOW_GOLEM, 97, new Identifier("snowman"));
        registry.register(EntityType.OCELOT, 98, new Identifier("ocelot"));
        registry.register(EntityType.IRON_GOLEM, 99, new Identifier("villager_golem"));
        registry.register(EntityType.HORSE, 100, new Identifier("horse"));
        registry.register(EntityType.RABBIT, 101, new Identifier("rabbit"));
        registry.register(EntityType.POLAR_BEAR, 102, new Identifier("polar_bear"));
        registry.register(EntityType.LLAMA, 103, new Identifier("llama"));
        registry.register(EntityType.LLAMA_SPIT, 104, new Identifier("llama_spit"));
        registry.register(EntityType.PARROT, 105, new Identifier("parrot"));
        registry.register(EntityType.VILLAGER, 120, new Identifier("villager"));
        registry.register(EntityType.END_CRYSTAL, 200, new Identifier("ender_crystal"));
    }
}
