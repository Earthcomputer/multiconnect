package net.earthcomputer.multiconnect.protocols.v1_8;

import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_11_2.ClientStatusC2SPacket_1_11_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Blocks_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Particles_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_13_2.GuiOpenS2CPacket_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_13_2.UseBedS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_15_2.EntitySpawnGlobalS2CPacket_1_15_2;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.TameableEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_1.ChunkDeltaUpdateS2CPacket_1_16_1;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.earthcomputer.multiconnect.protocols.v1_9_2.UpdateSignS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_9_4.ResourcePackStatusC2SPacket_1_9_4;
import net.earthcomputer.multiconnect.transformer.CustomPayload;
import net.earthcomputer.multiconnect.transformer.StringCustomPayload;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.*;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Protocol_1_8 extends Protocol_1_9 {

    private static final AtomicInteger FAKE_TELEPORT_ID_COUNTER = new AtomicInteger();
    public static final int WORLD_EVENT_QUIET_GHAST_SHOOT = -1000 + 1;

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkData.class, buf -> {
            int verticalStripBitmask = ChunkDataTranslator.current().getPacket().getVerticalStripBitmask();
            int sectionCount = Integer.bitCount(verticalStripBitmask & 0xffff);
            Char2CharMap paletteMap = new Char2CharOpenHashMap();
            byte[] bitsPerBlock = new byte[sectionCount];
            char[][] palette = new char[sectionCount][];
            char[] oldData = new char[4096];
            char[][] newData = new char[sectionCount][4096];

            // read blocks from 1.8 data, build palettes and new data (index into palettes or raw, depending on size)
            for (int sec = 0; sec < sectionCount; sec++) {
                for (int i = 0; i < 4096; i++) {
                    char stateId = (char) (buf.readUnsignedByte() | (buf.readUnsignedByte() << 8));
                    paletteMap.putIfAbsent(stateId, (char)paletteMap.size());
                    oldData[i] = stateId;
                }

                int paletteCount = paletteMap.size();
                char[] secPalette;
                char[] secData = newData[sec];
                bitsPerBlock[sec] = (byte) Math.max(4, MathHelper.log2DeBruijn(paletteCount));

                if (paletteCount <= 256) {
                    secPalette = new char[paletteCount];
                    for (Char2CharMap.Entry paletteEntry : paletteMap.char2CharEntrySet()) {
                        secPalette[paletteEntry.getCharValue()] = paletteEntry.getCharKey();
                    }

                    for (int i = 0; i < 4096; i++) {
                        secData[i] = paletteMap.get(oldData[i]);
                    }
                } else {
                    secPalette = new char[0];
                    System.arraycopy(oldData, 0, secData, 0, 4096);
                }

                palette[sec] = secPalette;

                paletteMap.clear();
            }

            // read block light from 1.8 data
            byte[][] blockLight = new byte[sectionCount][16 * 16 * 16 / 2];
            for (int sec = 0; sec < sectionCount; sec++) {
                buf.readBytes(blockLight[sec]);
            }

            // read sky light from 1.8 data
            byte[][] skyLight = null;
            if (ChunkDataTranslator.current().getDimension().hasSkyLight()) {
                skyLight = new byte[sectionCount][16 * 16 * 16 / 2];
                for (int sec = 0; sec < sectionCount; sec++) {
                    buf.readBytes(skyLight[sec]);
                }
            }

            // write 1.9 data
            for (int sec = 0; sec < sectionCount; sec++) {
                // palette
                int secBitsPerBlock = bitsPerBlock[sec];
                buf.pendingRead(Byte.class, (byte)secBitsPerBlock);
                buf.pendingRead(VarInt.class, new VarInt(palette[sec].length));
                for (char pal : palette[sec]) {
                    buf.pendingRead(VarInt.class, new VarInt(pal));
                }
                // data
                char[] data = newData[sec];
                long[] packedData = new long[(4096 * secBitsPerBlock + 63) / 64];
                int packedDataIndex = 0;
                long val = 0;
                for (int i = 0; i < 4096; i++) {
                    int bit = i * secBitsPerBlock;
                    int offset = bit & 63;
                    val |= (long)data[i] << offset;
                    if (offset + secBitsPerBlock >= 64 || i == 4095) {
                        packedData[packedDataIndex++] = val;
                        val = data[i] >> (64 - offset);
                    }
                }
                buf.pendingRead(long[].class, packedData);

                // light
                buf.pendingRead(byte[].class, blockLight[sec]);
                if (skyLight != null) {
                    buf.pendingRead(byte[].class, skyLight[sec]);
                }
            }

            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // x
            buf.readInt(); // z
            buf.readBoolean(); // full chunk
            buf.disablePassthroughMode();
            int verticalStripBitmask = buf.readUnsignedShort();
            buf.pendingRead(VarInt.class, new VarInt(verticalStripBitmask));
            byte[] chunkData = buf.readByteArray(2097152);
            buf.pendingRead(VarInt.class, new VarInt(chunkData.length));
            buf.pendingRead(byte[].class, chunkData);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(CombatEventS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            CombatEventS2CPacket.Type type = buf.readEnumConstant(CombatEventS2CPacket.Type.class);
            if (type != CombatEventS2CPacket.Type.ENTITY_DIED) {
                buf.disablePassthroughMode();
                buf.applyPendingReads();
                return;
            }
            buf.readVarInt(); // entity id
            buf.readVarInt(); // attacker id
            buf.disablePassthroughMode();
            String deathMessage = buf.readString(32767);
            buf.pendingRead(Text.class, new LiteralText(deathMessage));
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlaySoundIdS2CPacket.class, buf -> {
            String soundId = buf.readString(256);

            SoundEvent soundEvent = SoundData_1_8.getInstance().getSoundEvent(soundId);
            if (soundEvent == null) soundEvent = SoundEvents.AMBIENT_CAVE;
            Identifier eventId = Registry.SOUND_EVENT.getId(soundEvent);
            assert eventId != null;
            buf.pendingRead(String.class, eventId.getPath());

            SoundCategory category = SoundData_1_8.getInstance().getCategory(soundId);
            if (category == null) category = SoundCategory.MASTER;
            buf.pendingRead(SoundCategory.class, category);

            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityStatusEffectS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.readByte(); // effect id
            buf.readByte(); // amplifier
            buf.readVarInt(); // duration
            buf.disablePassthroughMode();
            int flags = buf.readByte();
            buf.pendingRead(Byte.class, (byte) (flags != 0 ? 2 : 0));
            // TODO: test that other flags aren't needed
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityEquipmentUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            int oldSlot = buf.readShort();
            EquipmentSlot newSlot;
            switch (oldSlot) {
                case 0:
                default:
                    newSlot = EquipmentSlot.MAINHAND;
                    break;
                case 1:
                    newSlot = EquipmentSlot.FEET;
                    break;
                case 2:
                    newSlot = EquipmentSlot.LEGS;
                    break;
                case 3:
                    newSlot = EquipmentSlot.CHEST;
                    break;
                case 4:
                    newSlot = EquipmentSlot.HEAD;
                    break;
            }
            buf.pendingRead(EquipmentSlot.class, newSlot);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityS2CPacket.MoveRelative.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(Short.class, (short)((short)buf.readByte() * 128)); // x
            buf.pendingRead(Short.class, (short)((short)buf.readByte() * 128)); // y
            buf.pendingRead(Short.class, (short)((short)buf.readByte() * 128)); // z
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityS2CPacket.RotateAndMoveRelative.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(Short.class, (short)((short)buf.readByte() * 128)); // x
            buf.pendingRead(Short.class, (short)((short)buf.readByte() * 128)); // y
            buf.pendingRead(Short.class, (short)((short)buf.readByte() * 128)); // z
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityPositionS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // x
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // y
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // z
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(MapUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // id
            buf.readByte(); // scale
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, true); // show icons
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerPositionLookS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readDouble(); // x
            buf.readDouble(); // y
            buf.readDouble(); // z
            buf.readFloat(); // yaw
            buf.readFloat(); // pitch
            buf.readUnsignedByte(); // flags
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(FAKE_TELEPORT_ID_COUNTER.getAndIncrement())); // teleport id (used for teleport confirm which doesn't exist in 1.8)
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ExperienceOrbSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // x
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // y
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // z
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntitySpawnGlobalS2CPacket_1_15_2.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.readByte(); // entity type id
            buf.disablePassthroughMode();
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // x
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // y
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // z
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(MobSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.pendingRead(UUID.class, MathHelper.randomUuid()); // entity uuid
            buf.readByte(); // type
            buf.disablePassthroughMode();
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // x
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // y
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // z
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntitySpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.pendingRead(UUID.class, MathHelper.randomUuid()); // entity uuid
            buf.readByte(); // entity type id
            buf.disablePassthroughMode();
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // x
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // y
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // z
            buf.enablePassthroughMode();
            buf.readByte(); // yaw
            buf.readByte(); // pitch
            int entityData = buf.readInt();
            buf.disablePassthroughMode();
            if (entityData <= 0) {
                buf.pendingRead(Short.class, (short)0); // speed x
                buf.pendingRead(Short.class, (short)0); // speed y
                buf.pendingRead(Short.class, (short)0); // speed z
            }
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PaintingSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(UUID.class, MathHelper.randomUuid()); // entity uuid
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.readUuid(); // entity uuid
            buf.disablePassthroughMode();
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // x
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // y
            buf.pendingRead(Double.class, buf.readInt() / 32.0); // z
            buf.enablePassthroughMode();
            buf.readByte(); // yaw
            buf.readByte(); // pitch
            buf.disablePassthroughMode();
            // TODO: does this need to be stored somewhere?
            buf.readShort(); // selected item id
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(TeamS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(32); // team name
            int mode = buf.readByte();
            if (mode == 0 || mode == 2) {
                buf.readString(32); // display name
                buf.readString(16); // prefix
                buf.readString(16); // suffix
                buf.readByte(); // flags
                buf.readString(32); // team visibility rule
                buf.pendingRead(String.class, "never"); // collision rule
            }
            buf.disablePassthroughMode();
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(WorldEventS2CPacket.class, buf -> {
            int eventId = buf.readInt();
            if (eventId >= 1003 && eventId <= 1022 && eventId != 1019) {
                if (eventId == 1003) {
                    eventId += 2;
                } else if (eventId <= 1006) {
                    eventId += 5;
                } else if (eventId <= 1008) {
                    eventId += 8;
                } else if (eventId == 1009) {
                    eventId = WORLD_EVENT_QUIET_GHAST_SHOOT;
                } else if (eventId <= 1012) {
                    eventId += 9;
                } else if (eventId <= 1018) {
                    eventId += 10;
                } else {
                    eventId += 9;
                }
            }
            buf.pendingRead(Integer.class, eventId);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslatorComplexType(new CustomPayload(Protocol_1_13_2.CUSTOM_PAYLOAD_OPEN_BOOK), buf -> {
            buf.pendingRead(Hand.class, Hand.MAIN_HAND);
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerOutboundTranslator(HandSwingC2SPacket.class, buf -> {
            buf.skipWrite(Hand.class);
        });
        ProtocolRegistry.registerOutboundTranslator(ClickWindowC2SPacket.class, buf -> {
            buf.passthroughWrite(Byte.class); // sync id
            buf.passthroughWrite(Short.class); // slot
            buf.passthroughWrite(Byte.class); // click data
            buf.passthroughWrite(Short.class); // action id
            Supplier<SlotActionType> mode = buf.skipWrite(SlotActionType.class);
            buf.pendingWrite(Byte.class, () -> {
                switch (mode.get()) {
                    case PICKUP:
                    default:
                        return (byte)0;
                    case QUICK_MOVE:
                        return (byte)1;
                    case SWAP:
                        return (byte)2;
                    case CLONE:
                        return (byte)3;
                    case THROW:
                        return (byte)4;
                    case QUICK_CRAFT:
                        return (byte)5;
                    case PICKUP_ALL:
                        return (byte)6;
                }
            }, (Consumer<Byte>) buf::writeByte);
        });
        ProtocolRegistry.registerOutboundTranslator(ClientSettingsC2SPacket.class, buf -> {
            buf.passthroughWrite(String.class); // language
            buf.passthroughWrite(Byte.class); // render distance
            Supplier<ChatVisibility> chatVisibility = buf.skipWrite(ChatVisibility.class);
            buf.pendingWrite(Byte.class, () -> (byte)chatVisibility.get().getId(), (Consumer<Byte>) buf::writeByte);
            buf.passthroughWrite(Boolean.class); // chat colors
            buf.passthroughWrite(Byte.class); // player model bitmask
            buf.skipWrite(Arm.class); // main arm
        });
        ProtocolRegistry.registerOutboundTranslator(ClientCommandC2SPacket.class, buf -> {
            buf.passthroughWrite(VarInt.class); // entity id
            Supplier<ClientCommandC2SPacket.Mode> mode = buf.skipWrite(ClientCommandC2SPacket.Mode.class);
            buf.pendingWrite(ClientCommandMode_1_8.class, () -> ClientCommandMode_1_8.fromNew(mode.get()), buf::writeEnumConstant);
        });
        ProtocolRegistry.registerOutboundTranslator(RequestCommandCompletionsC2SPacket.class, buf -> {
            buf.passthroughWrite(String.class); // command
            buf.skipWrite(Boolean.class); // has target
        });
        ProtocolRegistry.registerOutboundTranslator(UpdateSignC2SPacket.class, buf -> {
            buf.passthroughWrite(BlockPos.class); // pos
            for (int i = 0; i < 4; i++) {
                Supplier<String> line = buf.skipWrite(String.class);
                buf.pendingWrite(String.class, () -> Text.Serializer.toJson(new LiteralText(line.get())), buf::writeString);
            }
        });
        ProtocolRegistry.registerOutboundTranslator(PlayerInteractEntityC2SPacket.class, buf -> {
            buf.passthroughWrite(VarInt.class); // entity id
            Supplier<PlayerInteractEntityC2SPacket.InteractionType> type = buf.passthroughWrite(PlayerInteractEntityC2SPacket.InteractionType.class);
            buf.whenWrite(() -> {
                if (type.get() == PlayerInteractEntityC2SPacket.InteractionType.INTERACT_AT) {
                    buf.passthroughWrite(Float.class); // hit x
                    buf.passthroughWrite(Float.class); // hit y
                    buf.passthroughWrite(Float.class); // hit z
                }
                if (type.get() == PlayerInteractEntityC2SPacket.InteractionType.INTERACT || type.get() == PlayerInteractEntityC2SPacket.InteractionType.INTERACT_AT) {
                    buf.skipWrite(Hand.class); // hand
                }
            });
        });
        ProtocolRegistry.registerOutboundTranslator(CustomPayloadC2SPacket_1_12_2.class, buf -> {
            Supplier<String> channel = buf.skipWrite(String.class);
            buf.pendingWrite(String.class, () -> "MC|AdvCmd".equals(channel.get()) || "MC|AutoCmd".equals(channel.get()) ? "MC|AdvCdm" : channel.get(), buf::writeString);
        });
        ProtocolRegistry.registerOutboundTranslatorComplexType(new StringCustomPayload("MC|AutoCmd"), buf -> {
            buf.pendingWrite(Byte.class, () -> (byte) 0, (Consumer<Byte>) buf::writeByte); // block or minecart (block)
            buf.passthroughWrite(Integer.class); // x
            buf.passthroughWrite(Integer.class); // y
            buf.passthroughWrite(Integer.class); // z
            buf.passthroughWrite(String.class); // command
            buf.passthroughWrite(Boolean.class); // should track output
            buf.skipWrite(String.class); // command block type
            buf.skipWrite(Boolean.class); // conditional
            buf.skipWrite(Boolean.class); // always active
        });
        ProtocolRegistry.registerOutboundTranslatorComplexType(new StringCustomPayload("MC|BSign"), buf -> {
            Supplier<ItemStack> stack = buf.skipWrite(ItemStack.class);
            buf.pendingWrite(ItemStack.class, () -> {
                if (stack.get().getItem() == Items.WRITABLE_BOOK) {
                    ItemStack newStack = new ItemStack(Items.WRITTEN_BOOK, stack.get().getCount());
                    newStack.setTag(stack.get().getTag());
                    return newStack;
                } else {
                    return stack.get();
                }
            }, buf::writeItemStack);
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, ExperienceOrbSpawnS2CPacket.class);
        remove(packets, EntitySpawnGlobalS2CPacket_1_15_2.class);
        remove(packets, PlayerSpawnS2CPacket.class);
        remove(packets, EntityAnimationS2CPacket.class);
        remove(packets, StatisticsS2CPacket.class);
        remove(packets, BlockEntityUpdateS2CPacket.class);
        remove(packets, BlockEventS2CPacket.class);
        remove(packets, BlockUpdateS2CPacket.class);
        remove(packets, BossBarS2CPacket.class);
        remove(packets, DifficultyS2CPacket.class);
        remove(packets, CommandSuggestionsS2CPacket.class);
        remove(packets, GameMessageS2CPacket.class);
        remove(packets, ChunkDeltaUpdateS2CPacket_1_16_1.class);
        remove(packets, ConfirmGuiActionS2CPacket.class);
        remove(packets, GuiOpenS2CPacket_1_13_2.class);
        remove(packets, ScreenHandlerSlotUpdateS2CPacket.class);
        remove(packets, CooldownUpdateS2CPacket.class);
        remove(packets, CustomPayloadS2CPacket.class);
        remove(packets, PlaySoundIdS2CPacket.class);
        remove(packets, DisconnectS2CPacket.class);
        remove(packets, EntityStatusS2CPacket.class);
        remove(packets, ExplosionS2CPacket.class);
        remove(packets, UnloadChunkS2CPacket.class);
        remove(packets, GameStateChangeS2CPacket.class);
        remove(packets, KeepAliveS2CPacket.class);
        remove(packets, ChunkDataS2CPacket.class);
        remove(packets, WorldEventS2CPacket.class);
        remove(packets, ParticleS2CPacket.class);
        remove(packets, GameJoinS2CPacket.class);
        remove(packets, EntityS2CPacket.MoveRelative.class);
        remove(packets, EntityS2CPacket.RotateAndMoveRelative.class);
        remove(packets, EntityS2CPacket.Rotate.class);
        remove(packets, EntityS2CPacket.class);
        remove(packets, VehicleMoveS2CPacket.class);
        remove(packets, PlayerListS2CPacket.class);
        remove(packets, PlayerPositionLookS2CPacket.class);
        remove(packets, UseBedS2CPacket.class);
        remove(packets, EntitiesDestroyS2CPacket.class);
        remove(packets, RemoveEntityStatusEffectS2CPacket.class);
        remove(packets, ResourcePackSendS2CPacket.class);
        remove(packets, PlayerRespawnS2CPacket.class);
        remove(packets, EntitySetHeadYawS2CPacket.class);
        remove(packets, SetCameraEntityS2CPacket.class);
        remove(packets, HeldItemChangeS2CPacket.class);
        remove(packets, ScoreboardDisplayS2CPacket.class);
        remove(packets, EntityTrackerUpdateS2CPacket.class);
        remove(packets, EntityAttachS2CPacket.class);
        remove(packets, EntityVelocityUpdateS2CPacket.class);
        remove(packets, EntityEquipmentUpdateS2CPacket.class);
        remove(packets, ExperienceBarUpdateS2CPacket.class);
        remove(packets, HealthUpdateS2CPacket.class);
        remove(packets, ScoreboardObjectiveUpdateS2CPacket.class);
        remove(packets, EntityPassengersSetS2CPacket.class);
        remove(packets, TeamS2CPacket.class);
        remove(packets, ScoreboardPlayerUpdateS2CPacket.class);
        remove(packets, PlayerSpawnPositionS2CPacket.class);
        remove(packets, WorldTimeUpdateS2CPacket.class);
        remove(packets, UpdateSignS2CPacket.class);
        remove(packets, PlaySoundFromEntityS2CPacket.class);
        remove(packets, PlaySoundS2CPacket.class);
        remove(packets, ItemPickupAnimationS2CPacket.class);
        remove(packets, EntityPositionS2CPacket.class);
        remove(packets, EntityAttributesS2CPacket.class);
        remove(packets, EntityStatusEffectS2CPacket.class);
        packets.add(0, PacketInfo.of(KeepAliveS2CPacket.class, KeepAliveS2CPacket::new));
        insertAfter(packets, KeepAliveS2CPacket.class, PacketInfo.of(GameJoinS2CPacket.class, GameJoinS2CPacket::new));
        insertAfter(packets, GameJoinS2CPacket.class, PacketInfo.of(GameMessageS2CPacket.class, GameMessageS2CPacket::new));
        insertAfter(packets, GameMessageS2CPacket.class, PacketInfo.of(WorldTimeUpdateS2CPacket.class, WorldTimeUpdateS2CPacket::new));
        insertAfter(packets, WorldTimeUpdateS2CPacket.class, PacketInfo.of(EntityEquipmentUpdateS2CPacket.class, EntityEquipmentUpdateS2CPacket::new));
        insertAfter(packets, EntityEquipmentUpdateS2CPacket.class, PacketInfo.of(PlayerSpawnPositionS2CPacket.class, PlayerSpawnPositionS2CPacket::new));
        insertAfter(packets, PlayerSpawnPositionS2CPacket.class, PacketInfo.of(HealthUpdateS2CPacket.class, HealthUpdateS2CPacket::new));
        insertAfter(packets, HealthUpdateS2CPacket.class, PacketInfo.of(PlayerRespawnS2CPacket.class, PlayerRespawnS2CPacket::new));
        insertAfter(packets, PlayerRespawnS2CPacket.class, PacketInfo.of(PlayerPositionLookS2CPacket.class, PlayerPositionLookS2CPacket::new));
        insertAfter(packets, PlayerPositionLookS2CPacket.class, PacketInfo.of(HeldItemChangeS2CPacket.class, HeldItemChangeS2CPacket::new));
        insertAfter(packets, HeldItemChangeS2CPacket.class, PacketInfo.of(UseBedS2CPacket.class, UseBedS2CPacket::new));
        insertAfter(packets, UseBedS2CPacket.class, PacketInfo.of(EntityAnimationS2CPacket.class, EntityAnimationS2CPacket::new));
        insertAfter(packets, EntityAnimationS2CPacket.class, PacketInfo.of(PlayerSpawnS2CPacket.class, PlayerSpawnS2CPacket::new));
        insertAfter(packets, PlayerSpawnS2CPacket.class, PacketInfo.of(ItemPickupAnimationS2CPacket.class, ItemPickupAnimationS2CPacket::new));
        insertAfter(packets, PaintingSpawnS2CPacket.class, PacketInfo.of(ExperienceOrbSpawnS2CPacket.class, ExperienceOrbSpawnS2CPacket::new));
        insertAfter(packets, ExperienceOrbSpawnS2CPacket.class, PacketInfo.of(EntityVelocityUpdateS2CPacket.class, EntityVelocityUpdateS2CPacket::new));
        insertAfter(packets, EntityVelocityUpdateS2CPacket.class, PacketInfo.of(EntitiesDestroyS2CPacket.class, EntitiesDestroyS2CPacket::new));
        insertAfter(packets, EntitiesDestroyS2CPacket.class, PacketInfo.of(EntityS2CPacket.class, EntityS2CPacket::new));
        insertAfter(packets, EntityS2CPacket.class, PacketInfo.of(EntityS2CPacket.MoveRelative.class, EntityS2CPacket.MoveRelative::new));
        insertAfter(packets, EntityS2CPacket.MoveRelative.class, PacketInfo.of(EntityS2CPacket.Rotate.class, EntityS2CPacket.Rotate::new));
        insertAfter(packets, EntityS2CPacket.Rotate.class, PacketInfo.of(EntityS2CPacket.RotateAndMoveRelative.class, EntityS2CPacket.RotateAndMoveRelative::new));
        insertAfter(packets, EntityS2CPacket.RotateAndMoveRelative.class, PacketInfo.of(EntityPositionS2CPacket.class, EntityPositionS2CPacket::new));
        insertAfter(packets, EntityPositionS2CPacket.class, PacketInfo.of(EntitySetHeadYawS2CPacket.class, EntitySetHeadYawS2CPacket::new));
        insertAfter(packets, EntitySetHeadYawS2CPacket.class, PacketInfo.of(EntityStatusS2CPacket.class, EntityStatusS2CPacket::new));
        insertAfter(packets, EntityStatusS2CPacket.class, PacketInfo.of(EntityAttachS2CPacket_1_8.class, EntityAttachS2CPacket_1_8::new));
        insertAfter(packets, EntityAttachS2CPacket_1_8.class, PacketInfo.of(EntityTrackerUpdateS2CPacket.class, EntityTrackerUpdateS2CPacket::new));
        insertAfter(packets, EntityTrackerUpdateS2CPacket.class, PacketInfo.of(EntityStatusEffectS2CPacket.class, EntityStatusEffectS2CPacket::new));
        insertAfter(packets, EntityStatusEffectS2CPacket.class, PacketInfo.of(RemoveEntityStatusEffectS2CPacket.class, RemoveEntityStatusEffectS2CPacket::new));
        insertAfter(packets, RemoveEntityStatusEffectS2CPacket.class, PacketInfo.of(ExperienceBarUpdateS2CPacket.class, ExperienceBarUpdateS2CPacket::new));
        insertAfter(packets, ExperienceBarUpdateS2CPacket.class, PacketInfo.of(EntityAttributesS2CPacket.class, EntityAttributesS2CPacket::new));
        insertAfter(packets, EntityAttributesS2CPacket.class, PacketInfo.of(ChunkDataS2CPacket.class, ChunkDataS2CPacket::new));
        insertAfter(packets, ChunkDataS2CPacket.class, PacketInfo.of(ChunkDeltaUpdateS2CPacket_1_16_1.class, ChunkDeltaUpdateS2CPacket_1_16_1::new));
        insertAfter(packets, ChunkDeltaUpdateS2CPacket_1_16_1.class, PacketInfo.of(BlockUpdateS2CPacket.class, BlockUpdateS2CPacket::new));
        insertAfter(packets, BlockUpdateS2CPacket.class, PacketInfo.of(BlockEventS2CPacket.class, BlockEventS2CPacket::new));
        insertAfter(packets, BlockBreakingProgressS2CPacket.class, PacketInfo.of(BulkChunkDataS2CPacket_1_8.class, BulkChunkDataS2CPacket_1_8::new));
        insertAfter(packets, BulkChunkDataS2CPacket_1_8.class, PacketInfo.of(ExplosionS2CPacket.class, ExplosionS2CPacket::new));
        insertAfter(packets, ExplosionS2CPacket.class, PacketInfo.of(WorldEventS2CPacket.class, WorldEventS2CPacket::new));
        insertAfter(packets, WorldEventS2CPacket.class, PacketInfo.of(PlaySoundIdS2CPacket.class, PlaySoundIdS2CPacket::new));
        insertAfter(packets, PlaySoundIdS2CPacket.class, PacketInfo.of(ParticleS2CPacket.class, ParticleS2CPacket::new));
        insertAfter(packets, ParticleS2CPacket.class, PacketInfo.of(GameStateChangeS2CPacket.class, GameStateChangeS2CPacket::new));
        insertAfter(packets, GameStateChangeS2CPacket.class, PacketInfo.of(EntitySpawnGlobalS2CPacket_1_15_2.class, EntitySpawnGlobalS2CPacket_1_15_2::new));
        insertAfter(packets, EntitySpawnGlobalS2CPacket_1_15_2.class, PacketInfo.of(GuiOpenS2CPacket_1_13_2.class, GuiOpenS2CPacket_1_13_2::new));
        insertAfter(packets, CloseScreenS2CPacket.class, PacketInfo.of(ScreenHandlerSlotUpdateS2CPacket.class, ScreenHandlerSlotUpdateS2CPacket::new));
        insertAfter(packets, ScreenHandlerPropertyUpdateS2CPacket.class, PacketInfo.of(ConfirmGuiActionS2CPacket.class, ConfirmGuiActionS2CPacket::new));
        insertAfter(packets, ConfirmGuiActionS2CPacket.class, PacketInfo.of(UpdateSignS2CPacket.class, UpdateSignS2CPacket::new));
        insertAfter(packets, MapUpdateS2CPacket.class, PacketInfo.of(BlockEntityUpdateS2CPacket.class, BlockEntityUpdateS2CPacket::new));
        insertAfter(packets, SignEditorOpenS2CPacket.class, PacketInfo.of(StatisticsS2CPacket.class, StatisticsS2CPacket::new));
        insertAfter(packets, StatisticsS2CPacket.class, PacketInfo.of(PlayerListS2CPacket.class, PlayerListS2CPacket::new));
        insertAfter(packets, PlayerAbilitiesS2CPacket.class, PacketInfo.of(CommandSuggestionsS2CPacket.class, CommandSuggestionsS2CPacket::new));
        insertAfter(packets, CommandSuggestionsS2CPacket.class, PacketInfo.of(ScoreboardObjectiveUpdateS2CPacket.class, ScoreboardObjectiveUpdateS2CPacket::new));
        insertAfter(packets, ScoreboardObjectiveUpdateS2CPacket.class, PacketInfo.of(ScoreboardPlayerUpdateS2CPacket.class, ScoreboardPlayerUpdateS2CPacket::new));
        insertAfter(packets, ScoreboardPlayerUpdateS2CPacket.class, PacketInfo.of(ScoreboardDisplayS2CPacket.class, ScoreboardDisplayS2CPacket::new));
        insertAfter(packets, ScoreboardDisplayS2CPacket.class, PacketInfo.of(TeamS2CPacket.class, TeamS2CPacket::new));
        insertAfter(packets, TeamS2CPacket.class, PacketInfo.of(CustomPayloadS2CPacket.class, CustomPayloadS2CPacket::new));
        insertAfter(packets, CustomPayloadS2CPacket.class, PacketInfo.of(DisconnectS2CPacket.class, DisconnectS2CPacket::new));
        insertAfter(packets, DisconnectS2CPacket.class, PacketInfo.of(DifficultyS2CPacket.class, DifficultyS2CPacket::new));
        insertAfter(packets, CombatEventS2CPacket.class, PacketInfo.of(SetCameraEntityS2CPacket.class, SetCameraEntityS2CPacket::new));
        insertAfter(packets, TitleS2CPacket.class, PacketInfo.of(SetCompressionThresholdS2CPacket_1_8.class, SetCompressionThresholdS2CPacket_1_8::new));
        insertAfter(packets, PlayerListHeaderS2CPacket.class, PacketInfo.of(ResourcePackSendS2CPacket.class, ResourcePackSendS2CPacket::new));
        insertAfter(packets, ResourcePackSendS2CPacket.class, PacketInfo.of(UpdateEntityNbtS2CPacket_1_8.class, UpdateEntityNbtS2CPacket_1_8::new));
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        remove(packets, TeleportConfirmC2SPacket.class);
        remove(packets, RequestCommandCompletionsC2SPacket.class);
        remove(packets, ClientStatusC2SPacket_1_11_2.class);
        remove(packets, ClientSettingsC2SPacket.class);
        remove(packets, ConfirmGuiActionC2SPacket.class);
        remove(packets, ButtonClickC2SPacket.class);
        remove(packets, ClickWindowC2SPacket.class);
        remove(packets, GuiCloseC2SPacket.class);
        remove(packets, CustomPayloadC2SPacket_1_12_2.class);
        remove(packets, KeepAliveC2SPacket.class);
        remove(packets, PlayerMoveC2SPacket.LookOnly.class);
        remove(packets, PlayerMoveC2SPacket.class);
        remove(packets, VehicleMoveC2SPacket.class);
        remove(packets, BoatPaddleStateC2SPacket.class);
        remove(packets, UpdatePlayerAbilitiesC2SPacket.class);
        remove(packets, ResourcePackStatusC2SPacket_1_9_4.class);
        remove(packets, UpdateSelectedSlotC2SPacket.class);
        remove(packets, HandSwingC2SPacket.class);
        remove(packets, PlayerInteractBlockC2SPacket.class);
        remove(packets, PlayerInteractItemC2SPacket.class);
        packets.add(0, PacketInfo.of(KeepAliveC2SPacket.class, KeepAliveC2SPacket::new));
        insertAfter(packets, PlayerInteractEntityC2SPacket.class, PacketInfo.of(PlayerMoveC2SPacket.class, PlayerMoveC2SPacket::new));
        insertAfter(packets, PlayerMoveC2SPacket.PositionOnly.class, PacketInfo.of(PlayerMoveC2SPacket.LookOnly.class, PlayerMoveC2SPacket.LookOnly::new));
        insertAfter(packets, PlayerActionC2SPacket.class, PacketInfo.of(PlayerUseItemC2SPacket_1_8.class, PlayerUseItemC2SPacket_1_8::new));
        insertAfter(packets, PlayerUseItemC2SPacket_1_8.class, PacketInfo.of(UpdateSelectedSlotC2SPacket.class, UpdateSelectedSlotC2SPacket::new));
        insertAfter(packets, UpdateSelectedSlotC2SPacket.class, PacketInfo.of(HandSwingC2SPacket.class, HandSwingC2SPacket::new));
        insertAfter(packets, PlayerInputC2SPacket.class, PacketInfo.of(GuiCloseC2SPacket.class, GuiCloseC2SPacket::new));
        insertAfter(packets, GuiCloseC2SPacket.class, PacketInfo.of(ClickWindowC2SPacket.class, ClickWindowC2SPacket::new));
        insertAfter(packets, ClickWindowC2SPacket.class, PacketInfo.of(ConfirmGuiActionC2SPacket.class, ConfirmGuiActionC2SPacket::new));
        insertAfter(packets, CreativeInventoryActionC2SPacket.class, PacketInfo.of(ButtonClickC2SPacket.class, ButtonClickC2SPacket::new));
        insertAfter(packets, UpdateSignC2SPacket.class, PacketInfo.of(UpdatePlayerAbilitiesC2SPacket.class, UpdatePlayerAbilitiesC2SPacket::new));
        insertAfter(packets, UpdatePlayerAbilitiesC2SPacket.class, PacketInfo.of(RequestCommandCompletionsC2SPacket.class, RequestCommandCompletionsC2SPacket::new));
        insertAfter(packets, RequestCommandCompletionsC2SPacket.class, PacketInfo.of(ClientSettingsC2SPacket.class, ClientSettingsC2SPacket::new));
        insertAfter(packets, ClientSettingsC2SPacket.class, PacketInfo.of(ClientStatusC2SPacket_1_11_2.class, ClientStatusC2SPacket_1_11_2::new));
        insertAfter(packets, ClientStatusC2SPacket_1_11_2.class, PacketInfo.of(CustomPayloadC2SPacket_1_12_2.class, CustomPayloadC2SPacket_1_12_2::new));
        insertAfter(packets, SpectatorTeleportC2SPacket.class, PacketInfo.of(ResourcePackStatusC2SPacket_1_9_4.class, ResourcePackStatusC2SPacket_1_9_4::new));
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof TeleportConfirmC2SPacket) {
            return false;
        }
        if (packet instanceof BoatPaddleStateC2SPacket) {
            return false; // TODO: better behavior?
        }
        if (packet instanceof VehicleMoveC2SPacket) {
            return false; // TODO: better behavior
        }
        if (packet instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) packet).getMode() == ClientCommandC2SPacket.Mode.STOP_RIDING_JUMP) {
            return false;
        }
        if (packet instanceof PlayerActionC2SPacket && ((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return false;
        }
        if (packet instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket interactBlock = (PlayerInteractBlockC2SPacket) packet;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                BlockHitResult hitResult = interactBlock.getBlockHitResult();
                BlockPos blockPos = hitResult.getBlockPos();
                Vec3d pos = hitResult.getPos();
                player.networkHandler.sendPacket(new PlayerUseItemC2SPacket_1_8(blockPos, hitResult.getSide().getId(), player.getMainHandStack(), (float)(pos.x - blockPos.getX()), (float)(pos.y - blockPos.getY()), (float)(pos.z - blockPos.getZ())));
            }
            return false;
        }
        if (packet instanceof PlayerInteractItemC2SPacket) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.networkHandler.sendPacket(new PlayerUseItemC2SPacket_1_8(player.getMainHandStack()));
            }
            return false;
        }
        if (packet instanceof ClickWindowC2SPacket) {
            ClickWindowC2SPacket clickWindow = (ClickWindowC2SPacket) packet;
            if (clickWindow.getActionType() == SlotActionType.SWAP && clickWindow.getClickData() == 40) {
                // swap with offhand
                return false;
            }
        }
        if (packet instanceof CustomPayloadC2SPacket_1_12_2) {
            CustomPayloadC2SPacket_1_12_2 customPayload = (CustomPayloadC2SPacket_1_12_2) packet;
            if ("MC|Struct".equals(customPayload.getChannel())) {
                return false;
            }
            if ("MC|PickItem".equals(customPayload.getChannel())) {
                // TODO: emulate this?
                return false;
            }
        }
        return super.onSendPacket(packet);
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_8, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_8, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_8, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_8, Registry.ENCHANTMENT, this::mutateEnchantmentRegistry);
        mutator.mutate(Protocols.V1_8, Registry.PARTICLE_TYPE, this::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_8, Registry.BLOCK_ENTITY_TYPE, this::mutateBlockEntityTypeRegistry);
        mutator.mutate(Protocols.V1_8, Registry.STATUS_EFFECT, this::mutateStatusEffectRegistry);
        mutator.mutate(Protocols.V1_8, Registry.CUSTOM_STAT, this::mutateCustomStatRegistry);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.purge(Blocks.END_ROD);
        registry.purge(Blocks.CHORUS_PLANT);
        registry.purge(Blocks.CHORUS_FLOWER);
        registry.purge(Blocks.PURPUR_BLOCK);
        registry.purge(Blocks.PURPUR_PILLAR);
        registry.purge(Blocks.PURPUR_STAIRS);
        registry.purge(Blocks_1_12_2.PURPUR_DOUBLE_SLAB);
        registry.purge(Blocks.PURPUR_SLAB);
        registry.purge(Blocks.END_STONE_BRICKS);
        registry.purge(Blocks.BEETROOTS);
        registry.purge(Blocks.GRASS_PATH);
        registry.purge(Blocks.END_GATEWAY);
        registry.purge(Blocks.REPEATING_COMMAND_BLOCK);
        registry.purge(Blocks.CHAIN_COMMAND_BLOCK);
        registry.purge(Blocks.FROSTED_ICE);
        registry.purge(Blocks.STRUCTURE_BLOCK);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.purge(Items.SPECTRAL_ARROW);
        registry.purge(Items.TIPPED_ARROW);
        registry.purge(Items.SPRUCE_BOAT);
        registry.purge(Items.BIRCH_BOAT);
        registry.purge(Items.JUNGLE_BOAT);
        registry.purge(Items.ACACIA_BOAT);
        registry.purge(Items.DARK_OAK_BOAT);
        registry.purge(Items.SPLASH_POTION);
        registry.purge(Items.LINGERING_POTION);
        registry.purge(Items.DRAGON_BREATH);
        registry.purge(Items.END_CRYSTAL);
        registry.purge(Items.SHIELD);
        registry.purge(Items.ELYTRA);
        registry.purge(Items.CHORUS_FRUIT);
        registry.purge(Items.POPPED_CHORUS_FRUIT);
        registry.purge(Items.BEETROOT_SEEDS);
        registry.purge(Items.BEETROOT);
        registry.purge(Items.BEETROOT_SOUP);
    }

    private void mutateEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.purge(EntityType.AREA_EFFECT_CLOUD);
        registry.purge(EntityType.SPECTRAL_ARROW);
        registry.purge(EntityType.SHULKER_BULLET);
        registry.purge(EntityType.DRAGON_FIREBALL);
        registry.purge(EntityType.SHULKER);
    }

    private void mutateEnchantmentRegistry(ISimpleRegistry<Enchantment> registry) {
        registry.purge(Enchantments.FROST_WALKER);
        registry.purge(Enchantments.MENDING);
    }

    private void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        rename(registry, ParticleTypes.ITEM, "iconcrack_");
        rename(registry, ParticleTypes.BLOCK, "blockcrack_");
        rename(registry, Particles_1_12_2.BLOCK_DUST, "blockdust_");
        registry.purge(ParticleTypes.DRAGON_BREATH);
        registry.purge(ParticleTypes.END_ROD);
        registry.purge(ParticleTypes.DAMAGE_INDICATOR);
        registry.purge(ParticleTypes.SWEEP_ATTACK);
    }

    private void mutateBlockEntityTypeRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.purge(BlockEntityType.STRUCTURE_BLOCK);
        registry.purge(BlockEntityType.END_GATEWAY);
    }

    private void mutateStatusEffectRegistry(ISimpleRegistry<StatusEffect> registry) {
        registry.purge(StatusEffects.GLOWING);
        registry.purge(StatusEffects.LEVITATION);
        registry.purge(StatusEffects.LUCK);
        registry.purge(StatusEffects.UNLUCK);
    }

    private void mutateCustomStatRegistry(ISimpleRegistry<Identifier> registry) {
        registry.unregister(Stats.SLEEP_IN_BED);
        registry.unregister(Stats.SNEAK_TIME);
        registry.unregister(Stats.AVIATE_ONE_CM);
    }

    @Override
    protected void addStates(Block block, Consumer<BlockState> stateAdder) {
        if (block == Blocks.COMMAND_BLOCK) {
            stateAdder.accept(block.getDefaultState()); // triggered = false
            stateAdder.accept(block.getDefaultState()); // triggered = true
            return;
        }
        if (block == Blocks.FIRE) {
            for (int alt = 0; alt < 2; alt++) {
                for (int east = 0; east < 2; east++) {
                    for (int flip = 0; flip < 2; flip++) {
                        for (BlockState state : block.getStateManager().getStates()) {
                            if (!acceptBlockState(state)) continue;
                            if (state.get(FireBlock.EAST) == (east == 0)) continue;
                            stateAdder.accept(state);
                            if (state.get(FireBlock.UP) && state.get(FireBlock.WEST)) {
                                stateAdder.accept(state.with(FireBlock.WEST, false)); // upper = 2, west = false
                                stateAdder.accept(state); // upper = 2, west = true
                            }
                        }
                    }
                }
            }
            return;
        }
        if (block == Blocks.TRIPWIRE_HOOK) {
            for (BlockState state : block.getStateManager().getStates()) {
                if (!acceptBlockState(state)) continue;
                stateAdder.accept(state); // suspended = false
                stateAdder.accept(state); // suspended = true
            }
            return;
        }
        if (block == Blocks.TRIPWIRE) {
            for (BlockState state : block.getStateManager().getStates()) {
                if (!acceptBlockState(state)) continue;
                stateAdder.accept(state); // suspended = false
                if (state.get(TripwireBlock.WEST)) {
                    // suspended = true
                    stateAdder.accept(state.with(TripwireBlock.WEST, false));
                    stateAdder.accept(state);
                }
            }
            return;
        }
        super.addStates(block, stateAdder);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("time").get("query").get("day").remove();
        BrigadierRemover.of(dispatcher).get("scoreboard").get("players").get("tag").remove();
        BrigadierRemover.of(dispatcher).get("scoreboard").get("teams").get("option").get("team").get("collisionRule").remove();
    }

    public static List<DataTracker.Entry<?>> deserializeDataTrackerEntries(PacketByteBuf buf) {
        ArrayList<DataTracker.Entry<?>> entries = null;

        int n;
        while ((n = buf.readByte()) != 127) {
            if (entries == null) {
                entries = new ArrayList<>();
            }

            int serializerId = (n & 0b11100000) >> 5;
            int id = n & 0b00011111;
            Object value;
            switch (serializerId) {
                case 0:
                    value = buf.readByte();
                    break;
                case 1:
                    value = buf.readShort();
                    break;
                case 2:
                    value = buf.readInt();
                    break;
                case 3:
                    value = buf.readFloat();
                    break;
                case 4:
                    value = buf.readString(32767);
                    break;
                case 5:
                    value = buf.readItemStack();
                    break;
                case 6:
                    value = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                    break;
                case 7:
                    value = new EulerAngle(buf.readFloat(), buf.readFloat(), buf.readFloat());
                    break;
                default:
                    // serializer id ranges from 0-7
                    throw new AssertionError();
            }

            entries.add(new DataTrackerEntry_1_8(serializerId, id, value));
        }

        return entries;
    }

    public static void handleByteTrackedData(Entity entity, int id, byte data) {
        if (id == 0) {
            boolean usingItem = (data & 16) != 0;
            if (usingItem) {
                data &= ~16;
            }
            if (entity instanceof OtherClientPlayerEntity) {
                OtherClientPlayerEntity player = (OtherClientPlayerEntity) entity;
                if (usingItem) {
                    player.setCurrentHand(Hand.MAIN_HAND);
                } else {
                    player.clearActiveItem();
                }
            }
            entity.getDataTracker().set(EntityAccessor.getFlags(), data);
        } else if (id == 3) {
            entity.setCustomNameVisible(data == 1);
        } else if (id == 4) {
            entity.setSilent(data == 1);
        } else if (entity instanceof LivingEntity) {
            if (id == 8) {
                entity.getDataTracker().set(LivingEntityAccessor.getPotionSwirlsAmbient(), data > 0);
            } else if (id == 9) {
                ((LivingEntity) entity).setStuckArrowCount(data);
            } else if (entity instanceof MobEntity) {
                if (id == 15) {
                    entity.getDataTracker().set(MobEntityAccessor.getMobFlags(), data);
                } else if (entity instanceof PassiveEntity) {
                    if (id == 12) {
                        entity.getDataTracker().set(PassiveEntityAccessor.getChild(), data < 0);
                    } else if (entity instanceof HorseBaseEntity) {
                        if (id == 19) {
                            entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_TYPE, (int) data);
                        }
                    } else if (entity instanceof PigEntity) {
                        if (id == 16) {
                            entity.getDataTracker().set(PigEntityAccessor.getSaddled(), data != 0);
                        }
                    } else if (entity instanceof RabbitEntity) {
                        if (id == 18) {
                            entity.getDataTracker().set(RabbitEntityAccessor.getRabbitType(), (int) data);
                        }
                    } else if (entity instanceof SheepEntity) {
                        if (id == 16) {
                            entity.getDataTracker().set(SheepEntityAccessor.getColor(), data);
                        }
                    } else if (entity instanceof TameableEntity) {
                        if (id == 16) {
                            entity.getDataTracker().set(TameableEntityAccessor.getTameableFlags(), data);
                        } else if (entity instanceof WolfEntity) {
                            if (id == 19) {
                                ((WolfEntity) entity).setBegging(data == 1);
                            } else if (id == 20) {
                                entity.getDataTracker().set(Protocol_1_12_2.OLD_WOLF_COLLAR_COLOR, (int) data);
                            }
                        }
                    }
                } else if (entity instanceof BatEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(BatEntityAccessor.getBatFlags(), data);
                    }
                } else if (entity instanceof BlazeEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(BlazeEntityAccessor.getBlazeFlags(), data);
                    }
                } else if (entity instanceof CreeperEntity) {
                    if (id == 17) {
                        entity.getDataTracker().set(CreeperEntityAccessor.getCharged(), data == 1);
                    } else if (id == 18) {
                        entity.getDataTracker().set(CreeperEntityAccessor.getIgnited(), data == 1);
                    }
                } else if (entity instanceof EndermanEntity) {
                    if (id == 18) {
                        entity.getDataTracker().set(EndermanEntityAccessor.getAngry(), data > 0);
                    }
                } else if (entity instanceof GhastEntity) {
                    if (id == 16) {
                        ((GhastEntity) entity).setShooting(data != 0);
                    }
                } else if (entity instanceof IronGolemEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(IronGolemEntityAccessor.getIronGolemFlags(), data);
                    }
                } else if (entity instanceof AbstractSkeletonEntity) {
                    if (id == 13) {
                        entity.getDataTracker().set(Protocol_1_10.OLD_SKELETON_TYPE, (int) data);
                    }
                } else if (entity instanceof SlimeEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(SlimeEntityAccessor.getSlimeSize(), (int) data);
                    }
                } else if (entity instanceof SpiderEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(SpiderEntityAccessor.getSpiderFlags(), data);
                    }
                } else if (entity instanceof WitchEntity) {
                    if (id == 21) {
                        ((WitchEntity) entity).setDrinking(data == 1);
                    }
                } else if (entity instanceof ZombieEntity) {
                    if (id == 12) {
                        entity.getDataTracker().set(ZombieEntityAccessor.getBaby(), data == 1);
                    } else if (id == 13) {
                        entity.getDataTracker().set(Protocol_1_10.OLD_ZOMBIE_TYPE, (int) data);
                    } else if (id == 14) {
                        entity.getDataTracker().set(Protocol_1_10.OLD_ZOMBIE_CONVERTING, data == 1);
                    }
                }
            } else if (entity instanceof ArmorStandEntity) {
                if (id == 10) {
                    entity.getDataTracker().set(ArmorStandEntity.ARMOR_STAND_FLAGS, data);
                }
            } else if (entity instanceof PlayerEntity) {
                if (id == 10) {
                    entity.getDataTracker().set(PlayerEntityAccessor.getPlayerModelParts(), data);
                }
            }
        } else if (entity instanceof ProjectileEntity) {
            if (id == 16) {
                entity.getDataTracker().set(PersistentProjectileEntityAccessor.getProjectileFlags(), data);
            } else if (entity instanceof WitherSkullEntity) {
                if (id == 10) {
                    ((WitherSkullEntity) entity).setCharged(data == 1);
                }
            }
        } else if (entity instanceof ItemFrameEntity) {
            if (id == 9) {
                entity.getDataTracker().set(ItemFrameEntityAccessor.getRotation(), (int)data);
            }
        } else if (entity instanceof AbstractMinecartEntity) {
            if (id == 22) {
                ((AbstractMinecartEntity) entity).setCustomBlockPresent(data == 1);
            } else if (entity instanceof FurnaceMinecartEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(FurnaceMinecartEntityAccessor.getLit(), data != 0);
                }
            }
        }
    }

    public static void handleShortTrackedData(Entity entity, int id, short data) {
        if (id == 1) {
            entity.setAir(data);
        } else if (entity instanceof EndermanEntity) {
            EndermanEntity enderman = (EndermanEntity) entity;
            if (id == 16) {
                BlockState heldState = Block.STATE_IDS.get(Blocks_1_12_2.convertToStateRegistryId(data));
                if (heldState == null || heldState.isAir()) {
                    enderman.setCarriedBlock(null);
                } else {
                    enderman.setCarriedBlock(heldState);
                }
            }
        }
    }

    public static void handleIntTrackedData(Entity entity, int id, int data) {
        if (entity instanceof LivingEntity) {
            if (id == 7) {
                entity.getDataTracker().set(LivingEntityAccessor.getPotionSwirlsColor(), data);
            } else if (entity instanceof CreeperEntity) {
                if (id == 16) {
                    ((CreeperEntity) entity).setFuseSpeed(data);
                }
            } else if (entity instanceof GuardianEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_GUARDIAN_FLAGS, (byte) data);
                } else if (id == 17) {
                    entity.getDataTracker().set(GuardianEntityAccessor.getBeamTargetId(), data);
                }
            } else if (entity instanceof HorseBaseEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_FLAGS, (byte) data);
                } else if (id == 20) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_VARIANT, data);
                } else if (id == 22) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_ARMOR, data);
                }
            } else if (entity instanceof CatEntity) {
                if (id == 18) {
                    entity.getDataTracker().set(CatEntityAccessor.getCatType(), data);
                }
            } else if (entity instanceof PlayerEntity) {
                if (id == 18) {
                    ((PlayerEntity) entity).setScore(data);
                }
            } else if (entity instanceof VillagerEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(Protocol_1_13_2.OLD_VILLAGER_PROFESSION, data);
                }
            } else if (entity instanceof WitherEntity) {
                WitherEntity wither = (WitherEntity) entity;
                if (id >= 17 && id <= 19) {
                    wither.setTrackedEntityId(id - 17, data);
                } else if (id == 20) {
                    wither.setInvulTimer(data);
                }
            }
        } else if (entity instanceof BoatEntity) {
            BoatEntity boat = (BoatEntity) entity;
            if (id == 17) {
                boat.setDamageWobbleTicks(data);
            } else if (id == 18) {
                boat.setDamageWobbleSide(data);
            }
        } else if (entity instanceof EndCrystalEntity) {
            if (id == 8) {
                // TODO: health??
            }
        } else if (entity instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity minecart = (AbstractMinecartEntity) entity;
            if (id == 17) {
                minecart.setDamageWobbleTicks(data);
            } else if (id == 18) {
                minecart.setDamageWobbleSide(data);
            } else if (id == 20) {
                entity.getDataTracker().set(Protocol_1_12_2.OLD_MINECART_DISPLAY_TILE, data);
            } else if (id == 21) {
                minecart.setCustomBlockOffset(data);
            }
        }
    }

    public static void handleFloatTrackedData(Entity entity, int id, float data) {
        if (entity instanceof LivingEntity) {
            if (id == 6) {
                entity.getDataTracker().set(LivingEntityAccessor.getHealth(), data);
            } else if (entity instanceof PlayerEntity) {
                if (id == 17) {
                    entity.getDataTracker().set(PlayerEntityAccessor.getAbsorptionAmount(), data);
                }
            } else if (entity instanceof WolfEntity) {
                if (id == 18) {
                    entity.getDataTracker().set(Protocol_1_14_4.OLD_WOLF_HEALTH, data);
                }
            }
        } else if (entity instanceof BoatEntity) {
            if (id == 19) {
                ((BoatEntity) entity).setDamageWobbleStrength(data);
            }
        } else if (entity instanceof MinecartEntity) {
            if (id == 19) {
                ((MinecartEntity) entity).setDamageWobbleStrength(data);
            }
        }
    }

    public static void handleStringTrackedData(Entity entity, int id, String data) {
        if (id == 2) {
            entity.getDataTracker().set(Protocol_1_12_2.OLD_CUSTOM_NAME, data);
        } else if (entity instanceof HorseBaseEntity) {
            HorseBaseEntity horse = (HorseBaseEntity) entity;
            if (id == 21) {
                if (data.isEmpty()) {
                    horse.setOwnerUuid(null);
                } else {
                    try {
                        horse.setOwnerUuid(UUID.fromString(data));
                    } catch (IllegalArgumentException e) {
                        horse.setOwnerUuid(null);
                    }
                }
            }
        } else if (entity instanceof CommandBlockMinecartEntity) {
            if (id == 23) {
                entity.getDataTracker().set(CommandBlockMinecartEntityAccessor.getCommand(), data);
            } else if (id == 24) {
                entity.getDataTracker().set(CommandBlockMinecartEntityAccessor.getLastOutput(), new LiteralText(data));
            }
        } else if (entity instanceof TameableEntity) {
            TameableEntity tameable = (TameableEntity) entity;
            if (id == 17) {
                if (data.isEmpty()) {
                    tameable.setOwnerUuid(null);
                } else {
                    try {
                        tameable.setOwnerUuid(UUID.fromString(data));
                    } catch (IllegalArgumentException e) {
                        tameable.setOwnerUuid(null);
                    }
                }
            }
        }
    }

    public static void handleItemStackTrackedData(Entity entity, int id, ItemStack data) {
        if (entity instanceof FireworkRocketEntity) {
            if (id == 8) {
                entity.getDataTracker().set(FireworkRocketEntityAccessor.getItem(), data);
            }
        } else if (entity instanceof ItemFrameEntity) {
            if (id == 8) {
                entity.getDataTracker().set(ItemFrameEntityAccessor.getItemStack(), data);
            }
        } else if (entity instanceof ItemEntity) {
            if (id == 10) {
                ((ItemEntity) entity).setStack(data);
            }
        }
    }

    public static void handleBlockPosTrackedData(Entity entity, int id, BlockPos data) {

    }

    public static void handleEulerAngleTrackedData(Entity entity, int id, EulerAngle data) {
        if (entity instanceof ArmorStandEntity) {
            switch (id) {
                case 11:
                    entity.getDataTracker().set(ArmorStandEntity.TRACKER_HEAD_ROTATION, data);
                    break;
                case 12:
                    entity.getDataTracker().set(ArmorStandEntity.TRACKER_BODY_ROTATION, data);
                    break;
                case 13:
                    entity.getDataTracker().set(ArmorStandEntity.TRACKER_LEFT_ARM_ROTATION, data);
                    break;
                case 14:
                    entity.getDataTracker().set(ArmorStandEntity.TRACKER_RIGHT_ARM_ROTATION, data);
                    break;
                case 15:
                    entity.getDataTracker().set(ArmorStandEntity.TRACKER_LEFT_LEG_ROTATION, data);
                    break;
                case 16:
                    entity.getDataTracker().set(ArmorStandEntity.TRACKER_RIGHT_LEG_ROTATION, data);
                    break;
            }
        }
    }

    public static OptionalDouble getDefaultAttackDamage(Item item) {
        if (item instanceof ToolItem) {
            ToolMaterial material = ((ToolItem) item).getMaterial();
            int materialBonus;
            if (material == ToolMaterials.STONE) {
                materialBonus = 1;
            } else if (material == ToolMaterials.IRON) {
                materialBonus = 2;
            } else if (material == ToolMaterials.DIAMOND) {
                materialBonus = 3;
            } else {
                materialBonus = 0;
            }
            if (item instanceof SwordItem) {
                return OptionalDouble.of(4 + materialBonus);
            } else if (item instanceof PickaxeItem) {
                return OptionalDouble.of(2 + materialBonus);
            } else if (item instanceof ShovelItem) {
                return OptionalDouble.of(1 + materialBonus);
            } else if (item instanceof AxeItem) {
                return OptionalDouble.of(3 + materialBonus);
            }
        }

        return OptionalDouble.empty();
    }
}
