package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntitySpawn;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_14)
public class SPacketEntitySpawn_Latest implements SPacketEntitySpawn {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.ENTITY_TYPE)
    @Introduce(compute = "translateTypeId")
    public int type;
    public double x;
    public double y;
    public double z;
    public byte pitch;
    public byte yaw;
    @Type(Types.INT)
    public int data;
    public short velocityX;
    public short velocityY;
    public short velocityZ;

    public static int translateTypeId(
            @Argument("type") byte type,
            @Argument("data") int data,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "chest_minecart")) int chestMinecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "furnace_minecart")) int furnaceMinecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "tnt_minecart")) int tntMinecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "spawner_minecart")) int spawnerMinecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "hopper_minecart")) int hopperMinecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "command_block_minecart")) int commandBlockMinecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "minecart")) int minecartId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "fishing_bobber")) int fishingBobberId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "arrow")) int arrowId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "spectral_arrow")) int spectralArrowId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "trident")) int tridentId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "snowball")) int snowballId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "llama_spit")) int llamaSpitId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "item_frame")) int itemFrameId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "leash_knot")) int leashKnotId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "ender_pearl")) int enderPearlId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "eye_of_ender")) int eyeOfEnderId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "firework_rocket")) int fireworkRocketId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "fireball")) int fireballId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "dragon_fireball")) int dragonFireballId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "small_fireball")) int smallFireballId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "wither_skull")) int witherSkullId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "shulker_bullet")) int shulkerBulletId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "egg")) int eggId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "evoker_fangs")) int evokerFangsId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "potion")) int potionId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "experience_bottle")) int experienceBottleId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "boat")) int boatId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "tnt")) int tntId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "armor_stand")) int armorStandId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "end_crystal")) int endCrystalId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "item")) int itemId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "falling_block")) int fallingBlockId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "area_effect_cloud")) int areaEffectCloudId
    ) {
        return switch (type) {
            case 10 -> switch (data) {
                case 1 -> chestMinecartId;
                case 2 -> furnaceMinecartId;
                case 3 -> tntMinecartId;
                case 4 -> spawnerMinecartId;
                case 5 -> hopperMinecartId;
                case 6 -> commandBlockMinecartId;
                default -> minecartId;
            };
            case 90 -> fishingBobberId;
            case 60 -> arrowId;
            case 91 -> spectralArrowId;
            case 94 -> tridentId;
            case 61 -> snowballId;
            case 68 -> llamaSpitId;
            case 71 -> itemFrameId;
            case 77 -> leashKnotId;
            case 65 -> enderPearlId;
            case 72 -> eyeOfEnderId;
            case 76 -> fireworkRocketId;
            case 63 -> fireballId;
            case 93 -> dragonFireballId;
            case 64 -> smallFireballId;
            case 66 -> witherSkullId;
            case 67 -> shulkerBulletId;
            case 62 -> eggId;
            case 79 -> evokerFangsId;
            case 73 -> potionId;
            case 75 -> experienceBottleId;
            case 1 -> boatId;
            case 50 -> tntId;
            case 78 -> armorStandId;
            case 51 -> endCrystalId;
            case 2 -> itemId;
            case 70 -> fallingBlockId;
            case 3 -> areaEffectCloudId;
            default -> type;
        };
    }
}
