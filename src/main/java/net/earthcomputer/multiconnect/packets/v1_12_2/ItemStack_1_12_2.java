package net.earthcomputer.multiconnect.packets.v1_12_2;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
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
import net.earthcomputer.multiconnect.packets.v1_13_1.ItemStack_1_13_1;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.ItemStackTheFlatteningFixAccessor;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class ItemStack_1_12_2 implements CommonTypes.ItemStack {
    private static final Map<String, ObjectIntPair<ResourceLocation>> REVERSE_FLATTENING_MAP = Util.make(new HashMap<>(), map -> {
        ItemStackTheFlatteningFixAccessor.getMap().forEach((oldItem, newItem) -> {
            int dotIndex = oldItem.lastIndexOf('.');
            map.put(newItem, ObjectIntPair.of(new ResourceLocation(oldItem.substring(0, dotIndex)), Integer.parseInt(oldItem.substring(dotIndex + 1))));
        });
    });

    @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "computeItemId")
    public short itemId;

    public static short computeItemId(
            @Argument("itemId") short itemId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "bat_spawn_egg")) int spawnEggId
    ) {
        if (itemId == -1) {
            return -1;
        }

        int newItemId = PacketSystem.serverRawIdToClient(Registry.ITEM, itemId);
        Item item = Registry.ITEM.byId(newItemId);
        if (item instanceof SpawnEggItem) {
            return (short) spawnEggId;
        }

        ResourceLocation newName = Registry.ITEM.getKey(item);
        ResourceLocation name = PacketSystem.clientIdToServer(Protocols.V1_13, Registry.ITEM, newName);
        if (name != null) {
            ObjectIntPair<ResourceLocation> pair = REVERSE_FLATTENING_MAP.get(name.toString());
            if (pair != null) {
                Integer rawId = PacketSystem.serverIdToRawId(Registry.ITEM, pair.first());
                if (rawId == null) {
                    throw new AssertionError("REVERSE_FLATTENING_MAP contains value not in registry");
                }
                return rawId.shortValue();
            }
        }

        return itemId;
    }

    @Override
    public boolean isPresent() {
        return itemId != -1;
    }

    public static ItemStack_1_12_2 fromMinecraft(ItemStack stack) {
        if (stack.isEmpty()) {
            var result = new net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2.Empty();
            result.itemId = -1;
            return result;
        } else {
            var later = (ItemStack_1_13_1.NonEmpty) ItemStack_1_13_1.fromMinecraft(stack);
            var result = new net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2.NonEmpty();
            result.itemId = computeItemId(later.itemId, PacketSystem.clientRawIdToServer(Registry.ITEM, Registry.ITEM.getId(Items.BAT_SPAWN_EGG)));
            result.count = later.count;
            int filledMapId = PacketSystem.clientRawIdToServer(Registry.ITEM, Registry.ITEM.getId(Items.FILLED_MAP));
            result.damage = net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2.NonEmpty.computeDamage(result.itemId, result.tag, filledMapId);
            result.tag = net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2.NonEmpty.computeTag(
                    result.itemId,
                    result.tag,
                    filledMapId,
                    PacketSystem.clientRawIdToServer(Registry.ITEM, Registry.ITEM.getId(Items.ENCHANTED_BOOK)),
                    PacketSystem.clientRawIdToServer(Registry.ITEM, Registry.ITEM.getId(Items.SHIELD))
            );
            return result;
        }
    }

    @Polymorphic(intValue = -1)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Empty extends ItemStack_1_12_2 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class NonEmpty extends ItemStack_1_12_2 implements CommonTypes.ItemStack.NonEmpty {
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "computeDamage")
        public short damage;
        @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "computeTag")
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
        public CompoundTag getTag() {
            return tag;
        }

        public static short computeDamage(
                @Argument("itemId") short itemId,
                @Argument("tag") @Nullable CompoundTag tag,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "filled_map")) int filledMapId
        ) {
            ResourceLocation itemName = PacketSystem.serverRawIdToId(Registry.ITEM, itemId);
            if (itemName != null) {
                ObjectIntPair<ResourceLocation> pair = REVERSE_FLATTENING_MAP.get(itemName.toString());
                if (pair != null) {
                    return (short) pair.rightInt();
                }
            }

            if (itemId == filledMapId) {
                return tag == null ? 0 : (short) tag.getInt("map");
            }

            int newItemId = PacketSystem.serverRawIdToClient(Registry.ITEM, itemId);
            Item item = Registry.ITEM.byId(newItemId);
            if (item.getMaxDamage() > 0) {
                return tag == null ? 0 : (short) tag.getInt("Damage");
            }

            return 0;
        }

        public static CompoundTag computeTag(
                @Argument("itemId") short itemId,
                @Argument("tag") @Nullable CompoundTag tag,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "filled_map")) int filledMapId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "enchanted_book")) int enchantedBookId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "shield")) int shieldId
        ) {
            boolean copiedTag = false;

            int newItemId = PacketSystem.serverRawIdToClient(Registry.ITEM, itemId);
            Item item = Registry.ITEM.byId(newItemId);
            if (item instanceof SpawnEggItem spawnEggItem) {
                EntityType<?> entityType = spawnEggItem.getType(null);
                ResourceLocation newEntityId = Registry.ENTITY_TYPE.getKey(entityType);
                ResourceLocation id = PacketSystem.clientIdToServer(Registry.ENTITY_TYPE, newEntityId);
                if (id != null) {
                    copiedTag = true;
                    tag = tag == null ? new CompoundTag() : tag.copy();
                    if (!tag.contains("EntityTag", Tag.TAG_COMPOUND)) {
                        tag.put("EntityTag", new CompoundTag());
                    }
                    CompoundTag entityTag = tag.getCompound("EntityTag");
                    if (!entityTag.contains("id", Tag.TAG_STRING)) {
                        entityTag.putString("id", id.toString());
                    }
                }
            } else if (item.getMaxDamage() > 0) {
                if (tag != null) {
                    copiedTag = true;
                    tag = tag.copy();
                    tag.remove("Damage");
                    if (tag.isEmpty()) {
                        tag = null;
                    }
                }
            } else if (itemId == filledMapId) {
                if (tag != null) {
                    copiedTag = true;
                    tag = tag.copy();
                    tag.remove("map");
                    if (tag.isEmpty()) {
                        tag = null;
                    }
                }
            } else if (itemId == enchantedBookId) {
                if (tag != null && tag.contains("StoredEnchantments", Tag.TAG_LIST)) {
                    copiedTag = true;
                    tag = tag.copy();
                    ListTag enchantments = tag.getList("StoredEnchantments", Tag.TAG_COMPOUND);
                    newEnchantmentListToOld(enchantments);
                }
            }

            if (item instanceof BannerItem || itemId == shieldId) {
                if (tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                    if (!copiedTag) {
                        copiedTag = true;
                        tag = tag.copy();
                    }
                    ItemStack_1_13_1.NonEmpty.invertBannerColors(tag);
                }
            }

            if (tag != null && tag.contains("Enchantments", Tag.TAG_LIST)) {
                if (!copiedTag) {
                    copiedTag = true;
                    tag = tag.copy();
                }
                ListTag enchantments = tag.getList("Enchantments", Tag.TAG_COMPOUND);
                newEnchantmentListToOld(enchantments);
                tag.put("ench", enchantments);
                tag.remove("Enchantments");
            }

            if (tag != null && tag.contains("display", Tag.TAG_COMPOUND)) {
                if (!copiedTag) {
                    copiedTag = true;
                    tag = tag.copy();
                }
                CompoundTag display = tag.getCompound("display");
                if (display.contains("Name", Tag.TAG_STRING)) {
                    try {
                        MutableComponent name = Component.Serializer.fromJson(display.getString("Name"));
                        if (name != null) {
                            display.putString("Name", name.getString());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return tag;
        }

        private static void newEnchantmentListToOld(ListTag enchantments) {
            for (int i = 0; i < enchantments.size(); i++) {
                CompoundTag enchantment = enchantments.getCompound(i);
                ResourceLocation name = ResourceLocation.tryParse(enchantment.getString("id"));
                boolean valid = false;
                if (name != null) {
                    Integer id = PacketSystem.serverIdToRawId(Registry.ENCHANTMENT, name);
                    if (id != null) {
                        enchantment.putInt("id", id);
                        valid = true;
                    }
                }

                if (!valid) {
                    enchantments.remove(i--);
                }
            }
        }
    }
}
