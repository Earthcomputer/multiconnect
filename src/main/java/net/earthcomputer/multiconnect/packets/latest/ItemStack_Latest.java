package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_16)
@DefaultConstruct(subType = ItemStack_Latest.Empty.class)
public abstract class ItemStack_Latest implements CommonTypes.ItemStack {
    public boolean present;

    @Override
    public boolean isPresent() {
        return present;
    }

    public static ItemStack_Latest fromMinecraft(ItemStack stack) {
        if (stack.isEmpty()) {
            return new net.earthcomputer.multiconnect.packets.latest.ItemStack_Latest.Empty();
        } else {
            var result = new net.earthcomputer.multiconnect.packets.latest.ItemStack_Latest.NonEmpty();
            result.present = true;
            var itemRegistry = net.minecraft.core.Registry.ITEM;
            result.itemId = PacketSystem.clientRawIdToServer(itemRegistry, itemRegistry.getId(stack.getItem()));
            result.count = (byte) stack.getCount();
            result.tag = stack.getTag();
            return result;
        }
    }

    @Polymorphic(booleanValue = false)
    @MessageVariant(minVersion = Protocols.V1_16)
    public static class Empty extends ItemStack_Latest implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(booleanValue = true)
    @MessageVariant(minVersion = Protocols.V1_16)
    public static class NonEmpty extends ItemStack_Latest implements CommonTypes.ItemStack.NonEmpty {
        @Registry(Registries.ITEM)
        public int itemId;
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_OLDER, compute = "translateTagClientbound")
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

        public static CompoundTag translateTagClientbound(
                @Argument("itemId") int itemId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "player_head")) int playerHeadId,
                @Argument("tag") @Nullable CompoundTag tag
        ) {
            if (itemId != playerHeadId || tag == null) {
                return tag;
            }
            if (tag.contains("SkullOwner", Tag.TAG_COMPOUND)) {
                CompoundTag skullOwner = tag.getCompound("SkullOwner");
                if (skullOwner.contains("Id", Tag.TAG_STRING)) {
                    try {
                        UUID uuid = UUID.fromString(skullOwner.getString("Id"));
                        skullOwner.putUUID("Id", uuid);
                    } catch (IllegalArgumentException e) {
                        // uuid failed to parse
                    }
                }
            }

            return tag;
        }
    }
}
