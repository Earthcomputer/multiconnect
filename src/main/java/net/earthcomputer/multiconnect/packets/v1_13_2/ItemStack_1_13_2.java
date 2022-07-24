package net.earthcomputer.multiconnect.packets.v1_13_2;

import com.google.gson.JsonParseException;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_15_2.ItemStack_1_15_2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13_2, maxVersion = Protocols.V1_13_2)
@DefaultConstruct(subType = ItemStack_1_13_2.Empty.class)
public abstract class ItemStack_1_13_2 implements CommonTypes.ItemStack {
    @Introduce(direction = Introduce.Direction.FROM_OLDER, compute = "computePresent")
    public boolean present;

    public static boolean computePresent(@Argument("itemId") short itemId) {
        return itemId != -1;
    }

    @Override
    public boolean isPresent() {
        return present;
    }

    public static ItemStack_1_13_2 fromMinecraft(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemStack_1_13_2.Empty();
        } else {
            var later = (ItemStack_1_15_2.NonEmpty) ItemStack_1_15_2.fromMinecraft(stack);
            var result = new ItemStack_1_13_2.NonEmpty();
            result.present = true;
            result.itemId = later.itemId;
            result.count = later.count;
            result.tag = ItemStack_1_13_2.NonEmpty.translateTagServerbound(later.tag);
            return result;
        }
    }

    @Polymorphic(booleanValue = false)
    @MessageVariant(minVersion = Protocols.V1_13_2, maxVersion = Protocols.V1_13_2)
    public static class Empty extends ItemStack_1_13_2 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(booleanValue = true)
    @MessageVariant(minVersion = Protocols.V1_13_2, maxVersion = Protocols.V1_13_2)
    public static class NonEmpty extends ItemStack_1_13_2 implements CommonTypes.ItemStack.NonEmpty {
        @Registry(Registries.ITEM)
        public int itemId;
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "translateTagServerbound")
        @Nullable
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
        @Nullable
        public CompoundTag getTag() {
            return tag;
        }

        public static CompoundTag translateTagServerbound(
                @Argument("tag") @Nullable CompoundTag tag
        ) {
            if (tag == null) {
                return null;
            }
            if (tag.contains("display", Tag.TAG_COMPOUND)) {
                CompoundTag display = tag.getCompound("display");
                if (display.contains("multiconnect:1.13.2/oldLore", Tag.TAG_LIST) || display.contains("Lore", Tag.TAG_LIST)) {
                    ListTag lore = display.contains("multiconnect:1.13.2/oldLore", Tag.TAG_LIST) ? display.getList("multiconnect:1.13.2/oldLore", Tag.TAG_STRING) : display.getList("Lore", Tag.TAG_STRING);
                    ListTag newLore = new ListTag();
                    for (int i = 0; i < lore.size(); i++) {
                        try {
                            Component text = Component.Serializer.fromJson(lore.getString(i));
                            if (text == null) {
                                throw new JsonParseException("text null");
                            }
                            newLore.add(StringTag.valueOf(text.getString()));
                        } catch (JsonParseException e) {
                            newLore.add(lore.get(i));
                        }
                    }
                    display.put("Lore", newLore);
                    display.remove("multiconnect:1.13.2/oldLore");
                }
            }
            return tag;
        }
    }
}
