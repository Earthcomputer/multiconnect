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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_13_2)
public abstract class ItemStack_1_13_2 implements CommonTypes.ItemStack {
    public boolean present;

    @Override
    public boolean isPresent() {
        return present;
    }

    @Polymorphic(booleanValue = false)
    @MessageVariant(maxVersion = Protocols.V1_13_2)
    public static class Empty extends ItemStack_1_13_2 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(booleanValue = true)
    @MessageVariant(maxVersion = Protocols.V1_13_2)
    public static class NonEmpty extends ItemStack_1_13_2 implements CommonTypes.ItemStack.NonEmpty {
        @Registry(Registries.ITEM)
        public int itemId;
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "translateTagServerbound")
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
                @Argument("tag") NbtCompound tag
        ) {
            if (tag == null) {
                return null;
            }
            if (tag.contains("display", NbtElement.COMPOUND_TYPE)) {
                NbtCompound display = tag.getCompound("display");
                if (display.contains("multiconnect:1.13.2/oldLore", NbtElement.LIST_TYPE) || display.contains("Lore", NbtElement.LIST_TYPE)) {
                    NbtList lore = display.contains("multiconnect:1.13.2/oldLore", NbtElement.LIST_TYPE) ? display.getList("multiconnect:1.13.2/oldLore", NbtElement.STRING_TYPE) : display.getList("Lore", NbtElement.STRING_TYPE);
                    NbtList newLore = new NbtList();
                    for (int i = 0; i < lore.size(); i++) {
                        try {
                            Text text = Text.Serializer.fromJson(lore.getString(i));
                            if (text == null) {
                                throw new JsonParseException("text null");
                            }
                            newLore.add(NbtString.of(text.asString()));
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
