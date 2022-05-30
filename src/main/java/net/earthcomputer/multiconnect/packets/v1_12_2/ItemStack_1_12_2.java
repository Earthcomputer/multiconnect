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
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.ItemInstanceTheFlatteningFixAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class ItemStack_1_12_2 implements CommonTypes.ItemStack {
    private static final Map<String, ObjectIntPair<Identifier>> REVERSE_FLATTENING_MAP = Util.make(new HashMap<>(), map -> {
        ItemInstanceTheFlatteningFixAccessor.getFlatteningMap().forEach((oldItem, newItem) -> {
            int dotIndex = oldItem.lastIndexOf('.');
            map.put(newItem, ObjectIntPair.of(new Identifier(oldItem.substring(0, dotIndex)), Integer.parseInt(oldItem.substring(dotIndex + 1))));
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
        Item item = Registry.ITEM.get(newItemId);
        if (item instanceof SpawnEggItem) {
            return (short) spawnEggId;
        }

        Identifier newName = Registry.ITEM.getId(item);
        Identifier name = PacketSystem.clientIdToServer(Registry.ITEM, newName);
        if (name != null) {
            ObjectIntPair<Identifier> pair = REVERSE_FLATTENING_MAP.get(name.toString());
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
        public NbtCompound tag;

        @Override
        public int getItemId() {
            return Short.toUnsignedInt(itemId);
        }

        @Override
        public byte getCount() {
            return count;
        }

        @Override
        public NbtCompound getTag() {
            return tag;
        }

        public static short computeDamage(
                @Argument("itemId") short itemId,
                @Argument("tag") @Nullable NbtCompound tag,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "filled_map")) int filledMapId
        ) {
            Identifier itemName = PacketSystem.serverRawIdToId(Registry.ITEM, itemId);
            if (itemName != null) {
                ObjectIntPair<Identifier> pair = REVERSE_FLATTENING_MAP.get(itemName.toString());
                if (pair != null) {
                    return (short) pair.rightInt();
                }
            }

            if (itemId == filledMapId) {
                return tag == null ? 0 : (short) tag.getInt("map");
            }

            int newItemId = PacketSystem.serverRawIdToClient(Registry.ITEM, itemId);
            Item item = Registry.ITEM.get(newItemId);
            if (item.getMaxDamage() > 0) {
                return tag == null ? 0 : (short) tag.getInt("Damage");
            }

            return 0;
        }

        public static NbtCompound computeTag(
                @Argument("itemId") short itemId,
                @Argument("tag") @Nullable NbtCompound tag,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "filled_map")) int filledMapId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "enchanted_book")) int enchantedBookId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "shield")) int shieldId
        ) {
            boolean copiedTag = false;

            int newItemId = PacketSystem.serverRawIdToClient(Registry.ITEM, itemId);
            Item item = Registry.ITEM.get(newItemId);
            if (item instanceof SpawnEggItem spawnEggItem) {
                EntityType<?> entityType = spawnEggItem.getEntityType(null);
                Identifier newEntityId = Registry.ENTITY_TYPE.getId(entityType);
                Identifier id = PacketSystem.clientIdToServer(Registry.ENTITY_TYPE, newEntityId);
                if (id != null) {
                    copiedTag = true;
                    tag = tag == null ? new NbtCompound() : tag.copy();
                    if (!tag.contains("EntityTag", NbtElement.COMPOUND_TYPE)) {
                        tag.put("EntityTag", new NbtCompound());
                    }
                    NbtCompound entityTag = tag.getCompound("EntityTag");
                    if (!entityTag.contains("id", NbtElement.STRING_TYPE)) {
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
                if (tag != null && tag.contains("StoredEnchantments", NbtElement.LIST_TYPE)) {
                    copiedTag = true;
                    tag = tag.copy();
                    NbtList enchantments = tag.getList("StoredEnchantments", NbtElement.COMPOUND_TYPE);
                    newEnchantmentListToOld(enchantments);
                }
            }

            if (item instanceof BannerItem || itemId == shieldId) {
                if (tag != null && tag.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)) {
                    if (!copiedTag) {
                        copiedTag = true;
                        tag = tag.copy();
                    }
                    ItemStack_1_13_1.NonEmpty.invertBannerColors(tag);
                }
            }

            if (tag != null && tag.contains("Enchantments", NbtElement.LIST_TYPE)) {
                if (!copiedTag) {
                    copiedTag = true;
                    tag = tag.copy();
                }
                NbtList enchantments = tag.getList("Enchantments", NbtElement.COMPOUND_TYPE);
                newEnchantmentListToOld(enchantments);
                tag.put("ench", enchantments);
                tag.remove("Enchantments");
            }

            if (tag != null && tag.contains("display", NbtElement.COMPOUND_TYPE)) {
                if (!copiedTag) {
                    copiedTag = true;
                    tag = tag.copy();
                }
                NbtCompound display = tag.getCompound("display");
                if (display.contains("Name", NbtElement.STRING_TYPE)) {
                    try {
                        MutableText name = Text.Serializer.fromJson(display.getString("Name"));
                        if (name != null) {
                            display.putString("Name", name.asString());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return tag;
        }

        private static void newEnchantmentListToOld(NbtList enchantments) {
            for (int i = 0; i < enchantments.size(); i++) {
                NbtCompound enchantment = enchantments.getCompound(i);
                Identifier name = Identifier.tryParse(enchantment.getString("id"));
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
