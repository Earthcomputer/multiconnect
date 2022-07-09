package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.ItemStack_Latest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.UUID;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_15_2)
@DefaultConstruct(subType = ItemStack_1_15_2.Empty.class)
public abstract class ItemStack_1_15_2 implements CommonTypes.ItemStack {
    public boolean present;

    @Override
    public boolean isPresent() {
        return present;
    }

    public static ItemStack_1_15_2 fromMinecraft(ItemStack stack) {
        if (stack.isEmpty()) {
            return new net.earthcomputer.multiconnect.packets.v1_15_2.ItemStack_1_15_2.Empty();
        } else {
            var latest = (ItemStack_Latest.NonEmpty) ItemStack_Latest.fromMinecraft(stack);
            var result = new net.earthcomputer.multiconnect.packets.v1_15_2.ItemStack_1_15_2.NonEmpty();
            result.present = true;
            result.itemId = latest.itemId;
            result.count = latest.count;
            result.tag = net.earthcomputer.multiconnect.packets.v1_15_2.ItemStack_1_15_2.NonEmpty.translateTagServerbound(latest.itemId, net.minecraft.core.Registry.ITEM.getId(Items.PLAYER_HEAD), latest.tag);
            return result;
        }
    }

    @Polymorphic(booleanValue = false)
    @MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_15_2)
    public static class Empty extends ItemStack_1_15_2 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(booleanValue = true)
    @MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_15_2)
    public static class NonEmpty extends ItemStack_1_15_2 implements CommonTypes.ItemStack.NonEmpty {
        @Registry(Registries.ITEM)
        public int itemId;
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "translateTagServerbound")
        @Introduce(direction = Introduce.Direction.FROM_OLDER, compute = "translateTagClientbound")
        public CompoundTag tag;

        @Override
        public int getItemId() {
            return itemId;
        }

        @Override
        public byte getCount() {
            return count;
        }

        @Override
        public CompoundTag getTag() {
            return tag;
        }

        public static CompoundTag translateTagServerbound(
                @Argument("itemId") int itemId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "player_head")) int playerHeadId,
                @Argument("tag") CompoundTag tag
        ) {
            if (itemId != playerHeadId || tag == null) {
                return tag;
            }

            if (tag.contains("SkullOwner", Tag.TAG_COMPOUND)) {
                CompoundTag skullOwner = tag.getCompound("SkullOwner");
                if (skullOwner.hasUUID("Id")) {
                    UUID uuid = skullOwner.getUUID("Id");
                    skullOwner.putString("Id", uuid.toString());
                }
            }

            return tag;
        }

        public static CompoundTag translateTagClientbound(
                @Argument("tag") CompoundTag tag
        ) {
            if (tag == null) {
                return null;
            }
            if (tag.contains("display", Tag.TAG_COMPOUND)) {
                CompoundTag display = tag.getCompound("display");
                if (display.contains("Lore", Tag.TAG_LIST)) {
                    ListTag lore = display.getList("Lore", Tag.TAG_STRING);
                    display.put("multiconnect:1.13.2/oldLore", lore);
                    ListTag newLore = new ListTag();
                    for (int i = 0; i < lore.size(); i++) {
                        newLore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(lore.getString(i)))));
                    }
                    display.put("Lore", newLore);
                }
            }
            return tag;
        }
    }
}
