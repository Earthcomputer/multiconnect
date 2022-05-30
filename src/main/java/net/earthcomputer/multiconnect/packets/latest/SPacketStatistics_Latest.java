package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.SPacketStatistics;
import net.earthcomputer.multiconnect.packets.v1_12_2.SPacketStatistics_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.StatsCounterFixAccessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_13)
public class SPacketStatistics_Latest implements SPacketStatistics {
    @Introduce(compute = "computeStatistics")
    public List<StatWithValue> statistics;

    public static List<StatWithValue> computeStatistics(
            @Argument("statistics") List<SPacketStatistics_1_12_2.StatWithValue> statistics,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "custom")) int customId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "mined")) int minedId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "crafted")) int craftedId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "used")) int usedId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "broken")) int brokenId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "picked_up")) int pickedUpId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "dropped")) int droppedId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "killed")) int killedId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.STAT_TYPE, value = "killed_by")) int killedById
    ) {
        List<StatWithValue> result = new ArrayList<>(statistics.size());
        for (var stat : statistics) {
            Statistic newStat = translateStat(stat.stat, customId, minedId, craftedId, usedId, brokenId, pickedUpId, droppedId, killedId, killedById);
            if (newStat != null) {
                result.add(new StatWithValue(newStat, stat.value));
            }
        }
        return result;
    }

    @Nullable
    private static Statistic translateStat(
            String statName,
            int customId,
            int minedId,
            int craftedId,
            int usedId,
            int brokenId,
            int pickedUpId,
            int droppedId,
            int killedId,
            int killedById
    ) {
        String[] parts = statName.split("\\.");
        if (parts.length < 2 || !parts[0].equals("stat")) {
            return null;
        }

        if (parts.length == 2) {
            String customStat = translateCustomStat(parts[1]);
            if (customStat == null) {
                return null;
            }

            Identifier customStatId = new Identifier(customStat);
            Integer customStatRawId = PacketSystem.serverIdToRawId(net.minecraft.util.registry.Registry.CUSTOM_STAT, customStatId);
            if (customStatRawId == null) {
                throw new AssertionError("translateCustomStat returned value not in registry");
            }
            var stat = new CustomStatistic();
            stat.category = customId;
            stat.statId = customStatRawId;
            return stat;
        }

        Statistic statistic;
        int category = switch (parts[1]) {
            case "mineBlock" -> {
                statistic = new BlockStatistic();
                yield minedId;
            }
            case "craftItem" -> {
                statistic = new ItemStatistic();
                yield craftedId;
            }
            case "useItem" -> {
                statistic = new ItemStatistic();
                yield usedId;
            }
            case "breakItem" -> {
                statistic = new ItemStatistic();
                yield brokenId;
            }
            case "pickup" -> {
                statistic = new ItemStatistic();
                yield pickedUpId;
            }
            case "drop" -> {
                statistic = new ItemStatistic();
                yield droppedId;
            }
            case "killEntity" -> {
                statistic = new EntityStatistic();
                yield killedId;
            }
            case "entityKilledBy" -> {
                statistic = new EntityStatistic();
                yield killedById;
            }
            default -> {
                statistic = null;
                yield -1;
            }
        };
        if (category == -1) {
            return null;
        }
        statistic.category = category;

        if (statistic instanceof EntityStatistic entityStat) {
            String renamed = StatsCounterFixAccessor.getRenamedEntities().get(parts[2]);
            if (renamed == null) {
                return null;
            }
            Integer rawId = PacketSystem.serverIdToRawId(net.minecraft.util.registry.Registry.ENTITY_TYPE, new Identifier(renamed));
            if (rawId == null) {
                throw new AssertionError("getRenamedEntities returned value not in registry");
            }
            entityStat.entityType = rawId;
            return statistic;
        }

        if (parts.length < 4) {
            return null;
        }
        Identifier id = Identifier.tryParse(parts[2] + ":" + String.join(".", Arrays.asList(parts).subList(3, parts.length)));
        if (id == null) {
            return null;
        }

        if (statistic instanceof BlockStatistic blockStat) {
            Integer rawId = PacketSystem.serverIdToRawId(net.minecraft.util.registry.Registry.BLOCK, id);
            if (rawId == null) {
                return null;
            }
            blockStat.block = rawId;
        } else {
            ItemStatistic itemStat = (ItemStatistic) statistic;
            Integer rawId = PacketSystem.serverIdToRawId(net.minecraft.util.registry.Registry.ITEM, id);
            if (rawId == null) {
                return null;
            }
            itemStat.item = rawId;
        }

        return statistic;
    }

    private static String translateCustomStat(String id) {
        return switch (id) {
            case "jump" -> "jump";
            case "drop" -> "drop";
            case "deaths" -> "deaths";
            case "mobKills" -> "mob_kills";
            case "pigOneCm" -> "pig_one_cm";
            case "flyOneCm" -> "fly_one_cm";
            case "leaveGame" -> "leave_game";
            case "diveOneCm" -> "walk_under_water_one_cm";
            case "swimOneCm" -> "swim_one_cm";
            case "fallOneCm" -> "fall_one_cm";
            case "walkOneCm" -> "walk_one_cm";
            case "boatOneCm" -> "boat_one_cm";
            case "sneakTime" -> "sneak_time";
            case "horseOneCm" -> "horse_one_cm";
            case "sleepInBed" -> "sleep_in_bed";
            case "fishCaught" -> "fish_caught";
            case "climbOneCm" -> "climb_one_cm";
            case "aviateOneCm" -> "aviate_one_cm";
            case "crouchOneCm" -> "crouch_one_cm";
            case "sprintOneCm" -> "sprint_one_cm";
            case "animalsBred" -> "animals_bred";
            case "chestOpened" -> "open_chest";
            case "damageTaken" -> "damage_taken";
            case "damageDealt" -> "damage_dealt";
            case "playerKills" -> "player_kills";
            case "armorCleaned" -> "clean_armor";
            case "flowerPotted" -> "pot_flower";
            case "recordPlayed" -> "play_record";
            case "cauldronUsed" -> "use_cauldron";
            case "bannerCleaned" -> "clean_banner";
            case "itemEnchanted" -> "enchant_item";
            case "playOneMinute" -> "play_one_minute";
            case "minecartOneCm" -> "minecart_one_cm";
            case "timeSinceDeath" -> "time_since_death";
            case "cauldronFilled" -> "fill_cauldron";
            case "noteblockTuned" -> "tune_noteblock";
            case "noteblockPlayed" -> "play_noteblock";
            case "cakeSlicesEaten" -> "eat_cake_slice";
            case "hopperInspected" -> "inspect_hopper";
            case "shulkerBoxOpened" -> "open_shulker_box";
            case "talkedToVillager" -> "talked_to_villager";
            case "enderchestOpened" -> "open_enderchest";
            case "dropperInspected" -> "inspect_dropper";
            case "beaconInteraction" -> "interact_with_beacon";
            case "furnaceInteraction" -> "interact_with_furnace";
            case "dispenserInspected" -> "inspect_dispenser";
            case "tradedWithVillager" -> "traded_with_villager";
            case "trappedChestTriggered" -> "trigger_trapped_chest";
            case "brewingstandInteraction" -> "interact_with_brewingstand";
            case "craftingTableInteraction" -> "interact_with_crafting_table";
            case "junkFished" -> "junk_fished";
            case "treasureFished" -> "treasure_fished";
            default -> null;
        };
    }

    @MessageVariant
    public static class StatWithValue {
        public Statistic stat;
        public int value;

        public StatWithValue() {
        }

        public StatWithValue(Statistic stat, int value) {
            this.stat = stat;
            this.value = value;
        }
    }

    @Polymorphic
    @MessageVariant
    public static abstract class Statistic {
        @Registry(Registries.STAT_TYPE)
        public int category;
    }

    @Polymorphic(stringValue = "mined")
    @MessageVariant
    public static class BlockStatistic extends Statistic {
        @Registry(Registries.BLOCK)
        public int block;
    }

    @Polymorphic(stringValue = {"crafted", "used", "broken", "picked_up", "dropped"})
    @MessageVariant
    public static class ItemStatistic extends Statistic {
        @Registry(Registries.ITEM)
        public int item;
    }

    @Polymorphic(stringValue = {"killed", "killed_by"})
    @MessageVariant
    public static class EntityStatistic extends Statistic {
        @Registry(Registries.ENTITY_TYPE)
        public int entityType;
    }

    @Polymorphic(stringValue = "custom")
    @MessageVariant
    public static class CustomStatistic extends Statistic {
        @Registry(Registries.CUSTOM_STAT)
        public int statId;
    }
}
