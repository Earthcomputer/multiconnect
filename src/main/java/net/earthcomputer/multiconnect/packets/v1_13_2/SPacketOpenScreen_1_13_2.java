package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketOpenHorseScreen;
import net.earthcomputer.multiconnect.packets.SPacketOpenScreen;
import net.earthcomputer.multiconnect.packets.latest.SPacketOpenScreen_Latest;

import java.util.ArrayList;
import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketOpenScreen_1_13_2 implements SPacketOpenScreen {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public String type;
    public CommonTypes.Text title;
    @Type(Types.UNSIGNED_BYTE)
    public int slotCount;
    @Type(Types.INT)
    @OnlyIf("isHorse")
    public int horseId;

    public static boolean isHorse(@Argument("type") String type) {
        return type.equals("EntityHorse");
    }

    @ReturnType(SPacketOpenScreen.class)
    @ReturnType(SPacketOpenHorseScreen.class)
    @Handler
    public static List<Object> handle(
            @Argument("syncId") int syncId,
            @Argument("type") String type,
            @Argument("title") CommonTypes.Text title,
            @Argument("slotCount") int slotCount,
            @Argument("horseId") int horseId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_9x1")) int generic9x1Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_9x2")) int generic9x2Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_9x3")) int generic9x3Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_9x4")) int generic9x4Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_9x5")) int generic9x5Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_9x6")) int generic9x6Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "merchant")) int merchantId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "crafting")) int craftingId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "enchantment")) int enchantmentId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "anvil")) int anvilId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "hopper")) int hopperId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "furnace")) int furnaceId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "brewing_stand")) int brewingStandId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "beacon")) int beaconId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "generic_3x3")) int generic3x3Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "shulker_box")) int shulkerBoxId
    ) {
        int newType;
        if ("minecraft:container".equals(type)) {
            newType = getBestContainerType(slotCount, generic9x1Id, generic9x2Id, generic9x3Id, generic9x4Id, generic9x5Id, generic9x6Id);
        } else if ("minecraft:villager".equals(type)) {
            newType = merchantId;
        } else if ("EntityHorse".equals(type)) {
            var packet = new SPacketOpenHorseScreen();
            packet.syncId = (byte) syncId;
            packet.slotCount = slotCount;
            packet.entityId = horseId;
            List<Object> packets = new ArrayList<>(1);
            packets.add(packet);
            return packets;
        } else if (slotCount <= 0) {
            newType = switch (type) {
                case "minecraft:crafting_table" -> craftingId;
                case "minecraft:enchanting_table" -> enchantmentId;
                case "minecraft:anvil" -> anvilId;
                default -> generic9x1Id;
            };
        } else {
            newType = switch (type) {
                case "minecraft:hopper" -> hopperId;
                case "minecraft:furnace" -> furnaceId;
                case "minecraft:brewing_stand" -> brewingStandId;
                case "minecraft:beacon" -> beaconId;
                case "minecraft:dispenser", "minecraft:dropper" -> generic3x3Id;
                case "minecraft:shulker_box" -> shulkerBoxId;
                default -> getBestContainerType(slotCount, generic9x1Id, generic9x2Id, generic9x3Id, generic9x4Id, generic9x5Id, generic9x6Id);
            };
        }

        var packet = new SPacketOpenScreen_Latest();
        packet.syncId = syncId;
        packet.screenHandlerType = newType;
        packet.title = title;

        List<Object> packets = new ArrayList<>(1);
        packets.add(packet);
        return packets;
    }

    private static int getBestContainerType(
            int slotCount,
            int generic9x1Id,
            int generic9x2Id,
            int generic9x3Id,
            int generic9x4Id,
            int generic9x5Id,
            int generic9x6Id
    ) {
        if (slotCount <= 9) {
            return generic9x1Id;
        } else if (slotCount <= 18) {
            return generic9x2Id;
        } else if (slotCount <= 27) {
            return generic9x3Id;
        } else if (slotCount <= 36) {
            return generic9x4Id;
        } else if (slotCount <= 45) {
            return generic9x5Id;
        } else {
            return generic9x6Id;
        }
    }
}
