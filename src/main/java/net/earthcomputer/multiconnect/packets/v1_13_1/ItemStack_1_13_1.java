package net.earthcomputer.multiconnect.packets.v1_13_1;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_13_2.ItemStack_1_13_2;
import net.earthcomputer.multiconnect.packets.v1_18_2.Text_1_18_2;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_1)
@DefaultConstruct(subType = ItemStack_1_13_1.Empty.class)
public abstract class ItemStack_1_13_1 implements CommonTypes.ItemStack {
    @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "computeItemIdServerbound")
    @Introduce(direction = Introduce.Direction.FROM_OLDER, compute = "computeItemIdClientbound")
    public short itemId;

    public static short computeItemIdServerbound(
            @Argument("this") ItemStack_1_13_2 self
    ) {
        if (!self.present) {
            return -1;
        } else {
            return (short) ((ItemStack_1_13_2.NonEmpty) self).itemId;
        }
    }

    public static short computeItemIdClientbound(
            @Argument("this") ItemStack_1_12_2 self,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "bat_spawn_egg")) int spawnEggId
    ) {
        if (self.itemId == -1) {
            return -1;
        }

        var nonEmptySelf = (ItemStack_1_12_2.NonEmpty) self;

        if (self.itemId == spawnEggId) {
            if (nonEmptySelf.tag != null) {
                CompoundTag entityTag = nonEmptySelf.tag.getCompound("EntityTag");
                if (entityTag != null) {
                    String entityId = entityTag.getString("id");
                    ResourceLocation identifier = ResourceLocation.tryParse(entityId);
                    if (identifier != null) {
                        ResourceLocation newId = PacketSystem.serverIdToClient(Registry.ENTITY_TYPE, identifier);
                        if (newId != null) {
                            EntityType<?> entityType = Registry.ENTITY_TYPE.get(newId);
                            Item newItem = SpawnEggItem.byId(entityType);
                            int newItemId = Registry.ITEM.getId(newItem);
                            return (short) PacketSystem.clientRawIdToServer(Registry.ITEM, newItemId);
                        }
                    }
                }
            }
        }

        ResourceLocation name = PacketSystem.serverRawIdToId(Registry.ITEM, self.itemId);
        String newName = ItemStackTheFlatteningFix.updateItem(name == null ? null : name.toString(), nonEmptySelf.damage);
        if (newName != null) {
            // convert 1.13 name to server raw id
            ResourceLocation currentName = PacketSystem.serverIdToClient(Protocols.V1_13, Registry.ITEM, new ResourceLocation(newName));
            Item item = Registry.ITEM.get(currentName);
            int currentRawId = Registry.ITEM.getId(item);
            return (short) PacketSystem.clientRawIdToServer(Registry.ITEM, currentRawId);
        }

        return self.itemId;
    }

    @Override
    public boolean isPresent() {
        return itemId != -1;
    }

    public static ItemStack_1_13_1 fromMinecraft(ItemStack stack) {
        if (stack.isEmpty()) {
            var result = new ItemStack_1_13_1.Empty();
            result.itemId = -1;
            return result;
        } else {
            var later = (ItemStack_1_13_2.NonEmpty) ItemStack_1_13_2.fromMinecraft(stack);
            var result = new ItemStack_1_13_1.NonEmpty();
            result.itemId = (short) later.itemId;
            result.count = later.count;
            result.tag = later.tag;
            return result;
        }
    }

    @Polymorphic(intValue = -1)
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_1)
    public static class Empty extends ItemStack_1_13_1 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_1)
    public static class NonEmpty extends ItemStack_1_13_1 implements CommonTypes.ItemStack.NonEmpty {
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_OLDER, compute = "computeTag")
        @Nullable
        public CompoundTag tag;

        @Override
        public int getItemId() {
            return Short.toUnsignedInt(itemId);
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

        public static CompoundTag computeTag(
                @Argument("itemId") short itemId,
                @Argument("damage") short damage,
                @Argument("tag") @Nullable CompoundTag tag,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "filled_map")) int filledMapId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "enchanted_book")) int enchantedBookId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "black_banner")) int bannerId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "shield")) int shieldId
        ) {
            boolean copiedTag = false;
            if (itemId == filledMapId) {
                copiedTag = true;
                tag = tag == null ? new CompoundTag() : tag.copy();
                tag.putInt("map", damage);
            } else if (itemId == enchantedBookId) {
                if (tag != null && tag.contains("StoredEnchantments", Tag.TAG_LIST)) {
                    copiedTag = true;
                    tag = tag.copy();
                    ListTag enchantments = tag.getList("StoredEnchantments", Tag.TAG_COMPOUND);
                    oldEnchantmentListToNew(enchantments);
                }
            } else {
                int newId = PacketSystem.serverRawIdToClient(Registry.ITEM, itemId);
                Item item = Registry.ITEM.byId(newId);
                if (item.getMaxDamage() > 0) {
                    copiedTag = true;
                    tag = tag == null ? new CompoundTag() : tag.copy();
                    tag.putInt("Damage", damage);
                }
            }

            if (itemId == bannerId || itemId == shieldId) {
                if (tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                    if (!copiedTag) {
                        copiedTag = true;
                        tag = tag.copy();
                    }
                    invertBannerColors(tag);
                }
            }

            if (tag != null && tag.contains("ench", Tag.TAG_LIST)) {
                if (!copiedTag) {
                    copiedTag = true;
                    tag = tag.copy();
                }
                ListTag enchantments = tag.getList("ench", Tag.TAG_COMPOUND);
                oldEnchantmentListToNew(enchantments);
                tag.put("Enchantments", enchantments);
                tag.remove("ench");
            }

            if (tag != null && tag.contains("display", Tag.TAG_COMPOUND)) {
                if (!copiedTag) {
                    copiedTag = true;
                    tag = tag.copy();
                }
                CompoundTag display = tag.getCompound("display");
                if (display.contains("Name", Tag.TAG_STRING)) {
                    display.putString("Name", Text_1_18_2.createLiteral(display.getString("Name")).json);
                }
            }

            return tag;
        }

        public static void invertBannerColors(CompoundTag tag) {
            CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
            if (blockEntityTag.contains("Patterns", Tag.TAG_LIST)) {
                ListTag patterns = blockEntityTag.getList("Patterns", Tag.TAG_COMPOUND);
                for (int i = 0; i < patterns.size(); i++) {
                    CompoundTag pattern = patterns.getCompound(i);
                    if (pattern.contains("Color", Tag.TAG_INT)) {
                        pattern.putInt("Color", 15 - pattern.getInt("Color"));
                    }
                }
            }
        }

        private static void oldEnchantmentListToNew(ListTag enchantments) {
            for (int i = 0; i < enchantments.size(); i++) {
                CompoundTag ench = enchantments.getCompound(i);
                ResourceLocation name = PacketSystem.serverRawIdToId(Registry.ENCHANTMENT, ench.getInt("id"));
                if (name == null) {
                    enchantments.remove(i--);
                } else {
                    ench.putString("id", name.toString());
                }
            }
        }
    }
}
