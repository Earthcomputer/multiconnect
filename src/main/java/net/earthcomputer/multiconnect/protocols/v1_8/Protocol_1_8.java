package net.earthcomputer.multiconnect.protocols.v1_8;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
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
import net.earthcomputer.multiconnect.protocols.v1_16_5.AckScreenActionC2SPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.AckScreenActionS2CPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.ClickSlotC2SPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.CombatEventS2CPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.EntityS2CPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.MapUpdateS2CPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.TitleS2CPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_17_1.Protocol_1_17_1;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.BatEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.BlazeEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.CatEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.CommandBlockMinecartEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.CreeperEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EndermanEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EntityTypeAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.FireworkRocketEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.FurnaceMinecartEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.GuardianEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.IronGolemEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.ItemFrameEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.LivingEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.MobEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.PassiveEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.PersistentProjectileEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.PigEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.PlayerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.RabbitEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.SheepEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.SlimeEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.SpiderEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.ZombieEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.earthcomputer.multiconnect.protocols.v1_9_2.UpdateSignS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_9_4.ResourcePackStatusC2SPacket_1_9_4;
import net.earthcomputer.multiconnect.transformer.CustomPayload;
import net.earthcomputer.multiconnect.transformer.StringCustomPayload;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtShort;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PaintingSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Protocol_1_8 extends Protocol_1_9 {

    private static final AtomicInteger FAKE_TELEPORT_ID_COUNTER = new AtomicInteger();
    public static final int WORLD_EVENT_QUIET_GHAST_SHOOT = -1000 + 1;
    private static final EntityDimensions DEFAULT_BOAT_DIMENSIONS = EntityType.BOAT.getDimensions();

    private static final BiMap<Potion, Integer> POTION_METAS = ImmutableBiMap.<Potion, Integer>builder()
            .put(Potions.SWIFTNESS, 2)
            .put(Potions.STRONG_SWIFTNESS, 2 | 32)
            .put(Potions.LONG_SWIFTNESS, 2 | 64)
            .put(Potions.SLOWNESS, 10)
            .put(Potions.STRONG_SLOWNESS, 10 | 32)
            .put(Potions.LONG_SLOWNESS, 10 | 64)
            .put(Potions.STRENGTH, 9)
            .put(Potions.STRONG_STRENGTH, 9 | 32)
            .put(Potions.LONG_STRENGTH, 9 | 64)
            .put(Potions.HEALING, 5)
            .put(Potions.STRONG_HEALING, 5 | 32)
            .put(Potions.HARMING, 12)
            .put(Potions.STRONG_HARMING, 12 | 32)
            .put(Potions.LEAPING, 11)
            .put(Potions.STRONG_LEAPING, 11 | 32)
            .put(Potions.LONG_LEAPING, 11 | 64)
            .put(Potions.REGENERATION, 1)
            .put(Potions.STRONG_REGENERATION, 1 | 32)
            .put(Potions.LONG_REGENERATION, 1 | 64)
            .put(Potions.FIRE_RESISTANCE, 3)
            .put(Potions.LONG_FIRE_RESISTANCE, 3 | 64)
            .put(Potions.WATER_BREATHING, 13)
            .put(Potions.LONG_WATER_BREATHING, 13 | 64)
            .put(Potions.INVISIBILITY, 14)
            .put(Potions.LONG_INVISIBILITY, 14 | 64)
            .put(Potions.NIGHT_VISION, 6)
            .put(Potions.LONG_NIGHT_VISION, 6 | 64)
            .put(Potions.WEAKNESS, 8)
            .put(Potions.LONG_WEAKNESS, 8 | 64)
            .put(Potions.POISON, 4)
            .put(Potions.STRONG_POISON, 4 | 32)
            .put(Potions.LONG_POISON, 4 | 64)
            .build();

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkData.class, buf -> {
            BitSet verticalStripBitmask = ChunkDataTranslator.current().getUserData(Protocol_1_17_1.VERTICAL_STRIP_BITMASK);
            int sectionCount = verticalStripBitmask.cardinality();
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
                bitsPerBlock[sec] = (byte) Math.max(4, MathHelper.ceilLog2(paletteCount));

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
        ProtocolRegistry.registerInboundTranslator(CombatEventS2CPacket_1_16_5.class, buf -> {
            buf.enablePassthroughMode();
            CombatEventS2CPacket_1_16_5.Mode mode = buf.readEnumConstant(CombatEventS2CPacket_1_16_5.Mode.class);
            if (mode != CombatEventS2CPacket_1_16_5.Mode.ENTITY_DIED) {
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
            EquipmentSlot newSlot = switch (oldSlot) {
                default -> EquipmentSlot.MAINHAND;
                case 1 -> EquipmentSlot.FEET;
                case 2 -> EquipmentSlot.LEGS;
                case 3 -> EquipmentSlot.CHEST;
                case 4 -> EquipmentSlot.HEAD;
            };
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
        ProtocolRegistry.registerInboundTranslator(MapUpdateS2CPacket_1_16_5.class, buf -> {
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
        ProtocolRegistry.registerOutboundTranslator(ClickSlotC2SPacket_1_16_5.class, buf -> {
            buf.passthroughWrite(Byte.class); // sync id
            buf.passthroughWrite(Short.class); // slot
            buf.passthroughWrite(Byte.class); // click data
            buf.passthroughWrite(Short.class); // action id
            Supplier<SlotActionType> mode = buf.skipWrite(SlotActionType.class);
            buf.pendingWrite(Byte.class, () -> {
                return switch (mode.get()) {
                    default -> (byte) 0;
                    case QUICK_MOVE -> (byte) 1;
                    case SWAP -> (byte) 2;
                    case CLONE -> (byte) 3;
                    case THROW -> (byte) 4;
                    case QUICK_CRAFT -> (byte) 5;
                    case PICKUP_ALL -> (byte) 6;
                };
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
            Supplier<PlayerInteractEntityC2SPacket.InteractType> type = buf.passthroughWrite(PlayerInteractEntityC2SPacket.InteractType.class);
            buf.whenWrite(() -> {
                if (type.get() == PlayerInteractEntityC2SPacket.InteractType.INTERACT_AT) {
                    buf.passthroughWrite(Float.class); // hit x
                    buf.passthroughWrite(Float.class); // hit y
                    buf.passthroughWrite(Float.class); // hit z
                }
                if (type.get() == PlayerInteractEntityC2SPacket.InteractType.INTERACT || type.get() == PlayerInteractEntityC2SPacket.InteractType.INTERACT_AT) {
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
                    newStack.setNbt(stack.get().getNbt());
                    return newStack;
                } else {
                    return stack.get();
                }
            }, buf::writeItemStack);
        });
    }

    public static ItemStack oldPotionItemToNew(ItemStack stack, int meta) {
        stack.setSubNbt("multiconnect:1.8/potionData", NbtShort.of((short) meta));
        boolean isSplash = (meta & 16384) != 0;
        Potion potion;
        if (meta == 0) {
            potion = Potions.WATER;
        } else if (meta == 16) {
            potion = Potions.AWKWARD;
        } else if (meta == 32) {
            potion = Potions.THICK;
        } else if (meta == 64) {
            potion = Potions.MUNDANE;
        } else if (meta == 8192) {
            potion = Potions.MUNDANE;
        } else {
            potion = POTION_METAS.inverse().getOrDefault(meta & 127, Potions.EMPTY);
        }
        if (isSplash) {
            ItemStack newStack = new ItemStack(Items.SPLASH_POTION, stack.getCount());
            newStack.setNbt(stack.getNbt());
            stack = newStack;
        }
        PotionUtil.setPotion(stack, potion);
        return stack;
    }

    public static Pair<ItemStack, Integer> newPotionItemToOld(ItemStack stack) {
        Potion potion = PotionUtil.getPotion(stack);
        NbtCompound tag = stack.getNbt();
        boolean hasForcedMeta = false;
        int forcedMeta = 0;
        if (tag != null) {
            tag.remove("Potion");
            if (tag.contains("multiconnect:1.8/potionData", 2)) { // short
                hasForcedMeta = true;
                forcedMeta = tag.getShort("multiconnect:1.8/potionData") & 0xffff;
                tag.remove("multiconnect:1.8/potionData");
            }
            if (tag.isEmpty()) {
                stack.setNbt(null);
            }
        }

        boolean isSplash = stack.getItem() == Items.SPLASH_POTION;
        if (isSplash) {
            ItemStack newStack = new ItemStack(Items.POTION, stack.getCount());
            newStack.setNbt(stack.getNbt());
            stack = newStack;
        }

        if (hasForcedMeta) {
            return Pair.of(stack, forcedMeta);
        }

        int meta;
        if (potion == Potions.WATER) {
            meta = 0;
        } else if (potion == Potions.AWKWARD) {
            meta = 16;
        } else if (potion == Potions.THICK) {
            meta = 32;
        } else if (potion == Potions.MUNDANE) {
            meta = 8192;
        } else {
            meta = POTION_METAS.getOrDefault(potion, 0);
            if (isSplash) {
                meta |= 16384;
            } else {
                meta |= 8192;
            }
        }

        return Pair.of(stack, meta);
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
        remove(packets, AckScreenActionS2CPacket_1_16_5.class);
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
        remove(packets, EntityS2CPacket_1_16_5.class);
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
        remove(packets, UpdateSelectedSlotS2CPacket.class);
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
        insertAfter(packets, PlayerPositionLookS2CPacket.class, PacketInfo.of(UpdateSelectedSlotS2CPacket.class, UpdateSelectedSlotS2CPacket::new));
        insertAfter(packets, UpdateSelectedSlotS2CPacket.class, PacketInfo.of(UseBedS2CPacket.class, UseBedS2CPacket::new));
        insertAfter(packets, UseBedS2CPacket.class, PacketInfo.of(EntityAnimationS2CPacket.class, EntityAnimationS2CPacket::new));
        insertAfter(packets, EntityAnimationS2CPacket.class, PacketInfo.of(PlayerSpawnS2CPacket.class, PlayerSpawnS2CPacket::new));
        insertAfter(packets, PlayerSpawnS2CPacket.class, PacketInfo.of(ItemPickupAnimationS2CPacket.class, ItemPickupAnimationS2CPacket::new));
        insertAfter(packets, PaintingSpawnS2CPacket.class, PacketInfo.of(ExperienceOrbSpawnS2CPacket.class, ExperienceOrbSpawnS2CPacket::new));
        insertAfter(packets, ExperienceOrbSpawnS2CPacket.class, PacketInfo.of(EntityVelocityUpdateS2CPacket.class, EntityVelocityUpdateS2CPacket::new));
        insertAfter(packets, EntityVelocityUpdateS2CPacket.class, PacketInfo.of(EntitiesDestroyS2CPacket.class, EntitiesDestroyS2CPacket::new));
        insertAfter(packets, EntitiesDestroyS2CPacket.class, PacketInfo.of(EntityS2CPacket_1_16_5.class, EntityS2CPacket_1_16_5::new));
        insertAfter(packets, EntityS2CPacket_1_16_5.class, PacketInfo.of(EntityS2CPacket.MoveRelative.class, EntityS2CPacket.MoveRelative::read));
        insertAfter(packets, EntityS2CPacket.MoveRelative.class, PacketInfo.of(EntityS2CPacket.Rotate.class, EntityS2CPacket.Rotate::read));
        insertAfter(packets, EntityS2CPacket.Rotate.class, PacketInfo.of(EntityS2CPacket.RotateAndMoveRelative.class, EntityS2CPacket.RotateAndMoveRelative::read));
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
        insertAfter(packets, ScreenHandlerPropertyUpdateS2CPacket.class, PacketInfo.of(AckScreenActionS2CPacket_1_16_5.class, AckScreenActionS2CPacket_1_16_5::new));
        insertAfter(packets, AckScreenActionS2CPacket_1_16_5.class, PacketInfo.of(UpdateSignS2CPacket.class, UpdateSignS2CPacket::new));
        insertAfter(packets, MapUpdateS2CPacket_1_16_5.class, PacketInfo.of(BlockEntityUpdateS2CPacket.class, BlockEntityUpdateS2CPacket::new));
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
        insertAfter(packets, CombatEventS2CPacket_1_16_5.class, PacketInfo.of(SetCameraEntityS2CPacket.class, SetCameraEntityS2CPacket::new));
        insertAfter(packets, TitleS2CPacket_1_16_5.class, PacketInfo.of(SetCompressionThresholdS2CPacket_1_8.class, SetCompressionThresholdS2CPacket_1_8::new));
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
        remove(packets, AckScreenActionC2SPacket_1_16_5.class);
        remove(packets, ButtonClickC2SPacket.class);
        remove(packets, ClickSlotC2SPacket_1_16_5.class);
        remove(packets, CloseHandledScreenC2SPacket.class);
        remove(packets, CustomPayloadC2SPacket_1_12_2.class);
        remove(packets, KeepAliveC2SPacket.class);
        remove(packets, PlayerMoveC2SPacket.LookAndOnGround.class);
        remove(packets, PlayerMoveC2SPacket.OnGroundOnly.class);
        remove(packets, VehicleMoveC2SPacket.class);
        remove(packets, BoatPaddleStateC2SPacket.class);
        remove(packets, UpdatePlayerAbilitiesC2SPacket.class);
        remove(packets, ResourcePackStatusC2SPacket_1_9_4.class);
        remove(packets, UpdateSelectedSlotC2SPacket.class);
        remove(packets, HandSwingC2SPacket.class);
        remove(packets, PlayerInteractBlockC2SPacket.class);
        remove(packets, PlayerInteractItemC2SPacket.class);
        packets.add(0, PacketInfo.of(KeepAliveC2SPacket.class, KeepAliveC2SPacket::new));
        insertAfter(packets, PlayerInteractEntityC2SPacket.class, PacketInfo.of(PlayerMoveC2SPacket.OnGroundOnly.class, PlayerMoveC2SPacket.OnGroundOnly::read));
        insertAfter(packets, PlayerMoveC2SPacket.PositionAndOnGround.class, PacketInfo.of(PlayerMoveC2SPacket.LookAndOnGround.class, PlayerMoveC2SPacket.LookAndOnGround::read));
        insertAfter(packets, PlayerActionC2SPacket.class, PacketInfo.of(PlayerUseItemC2SPacket_1_8.class, PlayerUseItemC2SPacket_1_8::new));
        insertAfter(packets, PlayerUseItemC2SPacket_1_8.class, PacketInfo.of(UpdateSelectedSlotC2SPacket.class, UpdateSelectedSlotC2SPacket::new));
        insertAfter(packets, UpdateSelectedSlotC2SPacket.class, PacketInfo.of(HandSwingC2SPacket.class, HandSwingC2SPacket::new));
        insertAfter(packets, PlayerInputC2SPacket.class, PacketInfo.of(CloseHandledScreenC2SPacket.class, CloseHandledScreenC2SPacket::new));
        insertAfter(packets, CloseHandledScreenC2SPacket.class, PacketInfo.of(ClickSlotC2SPacket_1_16_5.class, ClickSlotC2SPacket_1_16_5::new));
        insertAfter(packets, ClickSlotC2SPacket_1_16_5.class, PacketInfo.of(AckScreenActionC2SPacket_1_16_5.class, AckScreenActionC2SPacket_1_16_5::new));
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
    @ThreadSafe
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof TeleportConfirmC2SPacket) {
            return false;
        }
        if (packet instanceof BoatPaddleStateC2SPacket) {
            return false;
        }
        if (packet instanceof VehicleMoveC2SPacket) {
            return false;
        }
        if (packet instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) packet).getMode() == ClientCommandC2SPacket.Mode.STOP_RIDING_JUMP) {
            return false;
        }
        if (packet instanceof PlayerActionC2SPacket && ((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return false;
        }
        if (packet instanceof PlayerInteractBlockC2SPacket interactBlock) {
            MinecraftClient.getInstance().execute(() -> {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    BlockHitResult hitResult = interactBlock.getBlockHitResult();
                    BlockPos blockPos = hitResult.getBlockPos();
                    Vec3d pos = hitResult.getPos();
                    player.networkHandler.sendPacket(new PlayerUseItemC2SPacket_1_8(blockPos, hitResult.getSide().getId(), player.getMainHandStack(), (float)(pos.x - blockPos.getX()), (float)(pos.y - blockPos.getY()), (float)(pos.z - blockPos.getZ())));
                }
            });
            return false;
        }
        if (packet instanceof PlayerInteractItemC2SPacket) {
            MinecraftClient.getInstance().execute(() -> {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.networkHandler.sendPacket(new PlayerUseItemC2SPacket_1_8(player.getMainHandStack()));
                }
            });
            return false;
        }
        if (packet instanceof ClickSlotC2SPacket_1_16_5 clickSlot) {
            if (clickSlot.getSlotActionType() == SlotActionType.SWAP && clickSlot.getClickData() == 40) {
                // swap with offhand
                return false;
            }
        }
        if (packet instanceof CustomPayloadC2SPacket_1_12_2 customPayload) {
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
    public void setup(boolean resourceReload) {
        super.setup(resourceReload);
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(EntityDimensions.changing(1.5f, 0.5625f));
    }

    @Override
    public void disable() {
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(DEFAULT_BOAT_DIMENSIONS);
        super.disable();
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_8, Registry.BLOCK_KEY, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_8, Registry.ITEM_KEY, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_8, Registry.ENTITY_TYPE_KEY, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_8, Registry.ENCHANTMENT_KEY, this::mutateEnchantmentRegistry);
        mutator.mutate(Protocols.V1_8, Registry.PARTICLE_TYPE_KEY, this::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_8, Registry.BLOCK_ENTITY_TYPE_KEY, this::mutateBlockEntityTypeRegistry);
        mutator.mutate(Protocols.V1_8, Registry.MOB_EFFECT_KEY, this::mutateStatusEffectRegistry);
        mutator.mutate(Protocols.V1_8, Registry.CUSTOM_STAT_KEY, this::mutateCustomStatRegistry);
    }

    private void mutateBlockRegistry(RegistryBuilder<Block> registry) {
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
        registry.purge(Blocks.DIRT_PATH);
        registry.purge(Blocks.END_GATEWAY);
        registry.purge(Blocks.REPEATING_COMMAND_BLOCK);
        registry.purge(Blocks.CHAIN_COMMAND_BLOCK);
        registry.purge(Blocks.FROSTED_ICE);
        registry.purge(Blocks.STRUCTURE_BLOCK);
    }

    private void mutateItemRegistry(RegistryBuilder<Item> registry) {
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

    private void mutateEntityTypeRegistry(RegistryBuilder<EntityType<?>> registry) {
        registry.purge(EntityType.AREA_EFFECT_CLOUD);
        registry.purge(EntityType.SPECTRAL_ARROW);
        registry.purge(EntityType.SHULKER_BULLET);
        registry.purge(EntityType.DRAGON_FIREBALL);
        registry.purge(EntityType.SHULKER);
    }

    private void mutateEnchantmentRegistry(RegistryBuilder<Enchantment> registry) {
        registry.purge(Enchantments.FROST_WALKER);
        registry.purge(Enchantments.MENDING);
    }

    private void mutateParticleTypeRegistry(RegistryBuilder<ParticleType<?>> registry) {
        registry.rename(ParticleTypes.ITEM, "iconcrack_");
        registry.rename(ParticleTypes.BLOCK, "blockcrack_");
        registry.rename(Particles_1_12_2.BLOCK_DUST, "blockdust_");
        registry.purge(ParticleTypes.DRAGON_BREATH);
        registry.purge(ParticleTypes.END_ROD);
        registry.purge(ParticleTypes.DAMAGE_INDICATOR);
        registry.purge(ParticleTypes.SWEEP_ATTACK);
    }

    private void mutateBlockEntityTypeRegistry(RegistryBuilder<BlockEntityType<?>> registry) {
        registry.purge(BlockEntityType.STRUCTURE_BLOCK);
        registry.purge(BlockEntityType.END_GATEWAY);
    }

    private void mutateStatusEffectRegistry(RegistryBuilder<StatusEffect> registry) {
        registry.purge(StatusEffects.GLOWING);
        registry.purge(StatusEffects.LEVITATION);
        registry.purge(StatusEffects.LUCK);
        registry.purge(StatusEffects.UNLUCK);
    }

    private void mutateCustomStatRegistry(RegistryBuilder<Identifier> registry) {
        registry.unregister(Stats.SLEEP_IN_BED);
        registry.unregister(Stats.SNEAK_TIME);
        registry.unregister(Stats.AVIATE_ONE_CM);
    }

    @Override
    protected Stream<BlockState> getStatesForBlock(Block block) {
        if (block == Blocks.COMMAND_BLOCK) {
            // triggered = false, true
            return Stream.of(block.getDefaultState(), block.getDefaultState());
        }
        if (block == Blocks.FIRE) {
            List<BlockState> states = new ArrayList<>();
            for (int alt = 0; alt < 2; alt++) {
                for (int east = 0; east < 2; east++) {
                    for (int flip = 0; flip < 2; flip++) {
                        for (BlockState state : block.getStateManager().getStates()) {
                            if (!acceptBlockState(state)) continue;
                            if (state.get(FireBlock.EAST) == (east == 0)) continue;
                            states.add(state);
                            if (state.get(FireBlock.UP) && state.get(FireBlock.WEST)) {
                                states.add(state.with(FireBlock.WEST, false)); // upper = 2, west = false
                                states.add(state); // upper = 2, west = true
                            }
                        }
                    }
                }
            }
            return states.stream();
        }
        if (block == Blocks.TRIPWIRE_HOOK) {
            return block.getStateManager().getStates().stream()
                    .filter(this::acceptBlockState)
                    .flatMap(state -> Stream.of(state, state)); // suspended = false, true
        }
        if (block == Blocks.TRIPWIRE) {
            List<BlockState> states = new ArrayList<>();
            for (BlockState state : block.getStateManager().getStates()) {
                if (!acceptBlockState(state)) continue;
                states.add(state); // suspended = false
                if (state.get(TripwireBlock.WEST)) {
                    // suspended = true
                    states.add(state.with(TripwireBlock.WEST, false));
                    states.add(state);
                }
            }
            return states.stream();
        }
        return super.getStatesForBlock(block);
    }

    @Override
    protected void markChangedCollisionBoxes() {
        super.markChangedCollisionBoxes();
        markCollisionBoxChanged(Blocks.LADDER);
        markCollisionBoxChanged(Blocks.LILY_PAD);
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
            Object value = switch (serializerId) {
                case 0 -> buf.readByte();
                case 1 -> buf.readShort();
                case 2 -> buf.readInt();
                case 3 -> buf.readFloat();
                case 4 -> buf.readString(32767);
                case 5 -> buf.readItemStack();
                case 6 -> new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                case 7 -> new EulerAngle(buf.readFloat(), buf.readFloat(), buf.readFloat());
                default ->
                        // serializer id ranges from 0-7
                        throw new AssertionError();
            };

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
            if (entity instanceof OtherClientPlayerEntity player) {
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
        } else if (entity instanceof EndermanEntity enderman) {
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
            } else if (entity instanceof WitherEntity wither) {
                if (id >= 17 && id <= 19) {
                    wither.setTrackedEntityId(id - 17, data);
                } else if (id == 20) {
                    wither.setInvulTimer(data);
                }
            }
        } else if (entity instanceof BoatEntity boat) {
            if (id == 17) {
                boat.setDamageWobbleTicks(data);
            } else if (id == 18) {
                boat.setDamageWobbleSide(data);
            }
        } else if (entity instanceof EndCrystalEntity) {
            if (id == 8) {
                // TODO: health??
            }
        } else if (entity instanceof AbstractMinecartEntity minecart) {
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
        } else if (entity instanceof HorseBaseEntity horse) {
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
        } else if (entity instanceof TameableEntity tameable) {
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
                case 11 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_HEAD_ROTATION, data);
                case 12 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_BODY_ROTATION, data);
                case 13 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_LEFT_ARM_ROTATION, data);
                case 14 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_RIGHT_ARM_ROTATION, data);
                case 15 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_LEFT_LEG_ROTATION, data);
                case 16 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_RIGHT_LEG_ROTATION, data);
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

    @Override
    public int clientSlotIdToServer(ScreenHandler screenHandler, int slotId) {
        slotId = super.clientSlotIdToServer(screenHandler, slotId);
        if (slotId == -1) {
            return -1;
        }
        if (screenHandler instanceof BrewingStandScreenHandler) {
            if (slotId == 4) { // fuel slot
                return -1;
            } else if (slotId > 4) {
                slotId--;
            }
        }
        return slotId;
    }

    @Override
    public int serverSlotIdToClient(ScreenHandler screenHandler, int slotId) {
        if (screenHandler instanceof BrewingStandScreenHandler && slotId >= 4) {
            slotId++;
        }
        return super.serverSlotIdToClient(screenHandler, slotId);
    }
}
