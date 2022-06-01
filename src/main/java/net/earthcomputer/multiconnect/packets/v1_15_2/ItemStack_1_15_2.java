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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

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
            return new Empty();
        } else {
            var latest = (ItemStack_Latest.NonEmpty) ItemStack_Latest.fromMinecraft(stack);
            var result = new NonEmpty();
            result.present = true;
            result.itemId = latest.itemId;
            result.count = latest.count;
            result.tag = NonEmpty.translateTagServerbound(latest.itemId, net.minecraft.util.registry.Registry.ITEM.getRawId(Items.PLAYER_HEAD), latest.tag);
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
        public NbtCompound tag;

        @Override
        public int getItemId() {
            return itemId;
        }

        @Override
        public byte getCount() {
            return count;
        }

        @Override
        public NbtCompound getTag() {
            return tag;
        }

        public static NbtCompound translateTagServerbound(
                @Argument("itemId") int itemId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "player_head")) int playerHeadId,
                @Argument("tag") NbtCompound tag
        ) {
            if (itemId != playerHeadId || tag == null) {
                return tag;
            }

            if (tag.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
                NbtCompound skullOwner = tag.getCompound("SkullOwner");
                if (skullOwner.containsUuid("Id")) {
                    UUID uuid = skullOwner.getUuid("Id");
                    skullOwner.putString("Id", uuid.toString());
                }
            }

            return tag;
        }

        public static NbtCompound translateTagClientbound(
                @Argument("tag") NbtCompound tag
        ) {
            if (tag == null) {
                return null;
            }
            if (tag.contains("display", NbtElement.COMPOUND_TYPE)) {
                NbtCompound display = tag.getCompound("display");
                if (display.contains("Lore", NbtElement.LIST_TYPE)) {
                    NbtList lore = display.getList("Lore", NbtElement.STRING_TYPE);
                    display.put("multiconnect:1.13.2/oldLore", lore);
                    NbtList newLore = new NbtList();
                    for (int i = 0; i < lore.size(); i++) {
                        newLore.add(NbtString.of(Text.Serializer.toJson(new LiteralText(lore.getString(i)))));
                    }
                    display.put("Lore", newLore);
                }
            }
            return tag;
        }
    }
}
