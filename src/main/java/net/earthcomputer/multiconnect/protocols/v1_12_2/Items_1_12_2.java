package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.datafixer.fix.EntityTheRenameningBlock;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraft.item.Items.*;

public class Items_1_12_2 {

    private static Registry<Item> REGISTRY_1_13;
    private static final BiMap<Pair<Item, Integer>, Item> OLD_ITEM_TO_NEW = HashBiMap.create();

    public static ItemStack oldItemStackToNew(ItemStack stack, int meta) {
        boolean copiedTag = false;
        Item newItem = OLD_ITEM_TO_NEW.get(Pair.of(stack.getItem(), meta));
        if (newItem != null && newItem != stack.getItem()) {
            ItemStack newStack = new ItemStack(newItem, stack.getCount());
            newStack.setNbt(stack.getNbt());
            stack = newStack;
        }
        else if (stack.getItem() == FILLED_MAP) {
            stack = stack.copy();
            copiedTag = true;
            stack.getOrCreateNbt().putInt("map", meta);
        }
        else if (stack.getItem() == ENCHANTED_BOOK) {
            if (stack.getNbt() != null && stack.getNbt().contains("StoredEnchantments", 9)) {
                stack = stack.copy();
                copiedTag = true;
                assert stack.getNbt() != null;
                oldEnchantmentListToNew(stack.getNbt().getList("StoredEnchantments", 10));
            }
        }
        else if (stack.isDamageable()) {
            stack = stack.copy();
            copiedTag = true;
            stack.setDamage(meta);
        }
        else if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && stack.getItem() == POTION) {
            stack = stack.copy();
            copiedTag = true;
            stack = Protocol_1_8.oldPotionItemToNew(stack, meta);
        }
        else if (stack.getItem() == BAT_SPAWN_EGG) {
            NbtCompound entityTag = stack.getSubNbt("EntityTag");
            if (entityTag != null) {
                String entityId = entityTag.getString("id");
                Identifier identifier = Identifier.tryParse(entityId);
                if(identifier != null) {
                    EntityType<?> entityType = Registry.ENTITY_TYPE.get(identifier);
                    newItem = SpawnEggItem.forEntity(entityType);
                    if (newItem != null) {
                        ItemStack newStack = new ItemStack(newItem, stack.getCount());
                        newStack.setNbt(stack.getNbt());
                        stack = newStack;
                    }
                }
            }
        }
        else if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            if (stack.getNbt() != null && stack.getNbt().contains("BlockEntityTag", 10)) {
                stack = stack.copy();
                copiedTag = true;
                assert stack.getNbt() != null;
                NbtCompound blockEntityTag = stack.getNbt().getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Items", 9)) {
                    NbtList items = blockEntityTag.getList("Items", 10);
                    for (int i = 0; i < items.size(); i++) {
                        NbtCompound item = items.getCompound(i);
                        int itemMeta = item.getShort("Damage");
                        int slot = item.getByte("Slot");
                        ItemStack itemStack = ItemStack.fromNbt(item);
                        itemStack = oldItemStackToNew(itemStack, itemMeta);
                        NbtCompound newItemTag = itemStack.writeNbt(new NbtCompound());
                        newItemTag.putByte("Slot", (byte)slot);
                        items.set(i, newItemTag);
                    }
                }
            }
        }
        if (stack.getItem() instanceof BannerItem || stack.getItem() == SHIELD) {
            stack = invertBannerColors(stack);
        }
        if (stack.getNbt() != null && stack.getNbt().contains("ench", 9)) {
            if (!copiedTag) {
                stack = stack.copy();
                copiedTag = true;
            }
            assert stack.getNbt() != null;
            NbtList enchantments = stack.getNbt().getList("ench", 10);
            oldEnchantmentListToNew(enchantments);
            stack.getNbt().put("Enchantments", enchantments);
            stack.getNbt().remove("ench");
        }
        if (stack.hasCustomName()) {
            if (!copiedTag) {
                stack = stack.copy();
                //noinspection UnusedAssignment
                copiedTag = true;
            }
            //noinspection ConstantConditions
            String displayName = stack.getSubNbt("display").getString("Name");
            stack.setCustomName(new LiteralText(displayName));
        }
        return stack;
    }

    private static void oldEnchantmentListToNew(NbtList enchantments) {
        for (int i = 0; i < enchantments.size(); i++) {
            NbtCompound ench = enchantments.getCompound(i);
            int id = ench.getInt("id");
            Identifier name = Registry.ENCHANTMENT.getId(Registry.ENCHANTMENT.get(id));
            if (name == null) {
                enchantments.remove(i);
                i--;
            } else {
                ench.putString("id", name.toString());
            }
        }
    }

    public static Pair<ItemStack, Integer> newItemStackToOld(ItemStack stack) {
        boolean copiedTag = false;
        int meta = 0;
        Pair<Item, Integer> oldItemAndMeta = OLD_ITEM_TO_NEW.inverse().get(stack.getItem());
        if (oldItemAndMeta != null) {
            ItemStack oldStack = new ItemStack(oldItemAndMeta.getLeft(), stack.getCount());
            oldStack.setNbt(stack.getNbt());
            stack = oldStack;
            meta = oldItemAndMeta.getRight();
        }
        else if (stack.getItem() == FILLED_MAP) {
            Integer mapId = FilledMapItem.getMapId(stack);
            if (mapId != null) {
                meta = mapId;
                if (stack.getNbt() != null) {
                    stack = stack.copy();
                    copiedTag = true;
                    NbtCompound tag = stack.getNbt();
                    assert tag != null;
                    tag.remove("map");
                    if (tag.getSize() == 0)
                        stack.setNbt(null);
                }
            }
        }
        else if (stack.getItem() == ENCHANTED_BOOK) {
            NbtList enchantments = EnchantedBookItem.getEnchantmentNbt(stack);
            if (!enchantments.isEmpty()) {
                stack = stack.copy();
                copiedTag = true;
                newEnchantmentListToOld(enchantments);
            }
        }
        else if (stack.isDamageable()) {
            meta = stack.getDamage();
            if (stack.getNbt() != null) {
                stack = stack.copy();
                copiedTag = true;
                NbtCompound tag = stack.getNbt();
                assert tag != null;
                tag.remove("Damage");
                if (tag.getSize() == 0)
                    stack.setNbt(null);
            }
        }
        else if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && (stack.getItem() == POTION || stack.getItem() == SPLASH_POTION)) {
            stack = stack.copy();
            copiedTag = true;
            Pair<ItemStack, Integer> stackAndMeta = Protocol_1_8.newPotionItemToOld(stack);
            stack = stackAndMeta.getLeft();
            meta = stackAndMeta.getRight();
        }
        else if (stack.getItem() instanceof SpawnEggItem) {
            ItemStack oldStack = new ItemStack(BAT_SPAWN_EGG, stack.getCount());
            oldStack.setNbt(stack.getNbt() == null ? null : stack.getNbt().copy());
            copiedTag = true;
            NbtCompound entityTag = oldStack.getOrCreateSubNbt("EntityTag");
            if (!entityTag.contains("id", 8))
                entityTag.putString("id", Registry.ENTITY_TYPE.getId(((SpawnEggItem) stack.getItem()).getEntityType(oldStack.getNbt())).toString());
            stack = oldStack;
        }
        else if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            if (stack.getNbt() != null && stack.getNbt().contains("BlockEntityTag", 10)) {
                stack = stack.copy();
                copiedTag = true;
                assert stack.getNbt() != null;
                NbtCompound blockEntityTag = stack.getNbt().getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Items", 9)) {
                    NbtList items = blockEntityTag.getList("Items", 10);
                    for (int i = 0; i < items.size(); i++) {
                        NbtCompound item = items.getCompound(i);
                        int slot = item.getByte("Slot");
                        ItemStack itemStack = ItemStack.fromNbt(item);
                        Pair<ItemStack, Integer> itemStackAndMeta = newItemStackToOld(itemStack);
                        itemStack = itemStackAndMeta.getLeft();
                        NbtCompound newItemTag = itemStack.writeNbt(new NbtCompound());
                        newItemTag.putShort("Damage", itemStackAndMeta.getRight().shortValue());
                        newItemTag.putByte("Slot", (byte)slot);
                        items.set(i, newItemTag);
                    }
                }
            }
        }
        if (stack.getItem() instanceof BannerItem || stack.getItem() == SHIELD) {
            stack = invertBannerColors(stack);
        }
        if (stack.hasEnchantments()) {
            if (!copiedTag) {
                stack = stack.copy();
                copiedTag = true;
            }
            NbtList enchantments = stack.getEnchantments();
            newEnchantmentListToOld(enchantments);
            assert stack.getNbt() != null;
            stack.getNbt().put("ench", enchantments);
            stack.getNbt().remove("Enchantments");
        }
        if (stack.hasCustomName()) {
            if (!copiedTag) {
                stack = stack.copy();
                //noinspection UnusedAssignment
                copiedTag = true;
            }
            String displayName = stack.getName().asString();
            //noinspection ConstantConditions
            stack.getSubNbt("display").putString("Name", displayName);
        }
        return Pair.of(stack, meta);
    }

    private static void newEnchantmentListToOld(NbtList enchantments) {
        for (int i = 0; i < enchantments.size(); i++) {
            NbtCompound ench = enchantments.getCompound(i);
            Identifier name = Identifier.tryParse(ench.getString("id"));
            Enchantment enchObj = Registry.ENCHANTMENT.get(name);
            if (enchObj == null) {
                enchantments.remove(i);
                i--;
            } else {
                ench.putInt("id", Registry.ENCHANTMENT.getRawId(enchObj));
            }
        }
    }

    private static ItemStack invertBannerColors(ItemStack stack) {
        stack = stack.copy();
        NbtCompound blockEntityTag = stack.getSubNbt("BlockEntityTag");
        if (blockEntityTag != null && blockEntityTag.contains("Patterns", 9)) {
            NbtList patterns = blockEntityTag.getList("Patterns", 10);
            for (NbtElement t : patterns) {
                NbtCompound pattern = (NbtCompound) t;
                if (pattern.contains("Color", 3))
                    pattern.putInt("Color", 15 - pattern.getInt("Color"));
            }
        }
        return stack;
    }

    private static void register(ISimpleRegistry<Item> registry, Item item, int id, String name) {
        RegistryKey<Item> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(name));
        registry.registerInPlace(item, id, key, false);
    }

    private static void registerBlockItem(ISimpleRegistry<Item> registry, Block block) {
        RegistryKey<Item> key = RegistryKey.of(registry.getRegistryKey(), Registry.BLOCK.getId(block));
        registry.registerInPlace(Item.BLOCK_ITEMS.getOrDefault(block, AIR), Registry.BLOCK.getRawId(block), key, false);
    }

    private static void registerAliases(ISimpleRegistry<Item> registry) {
        for (int meta = 1; meta < 16; meta++) {
            for (int itemId = 1; itemId < 453; itemId++) {
                Item baseItem = Registry.ITEM.get(itemId);
                String baseName = Registry.ITEM.getId(baseItem).toString();
                String newName = ItemInstanceTheFlatteningFix.getItem(baseName, meta);
                newName = EntityTheRenameningBlock.ITEMS.getOrDefault(newName, newName);
                if (newName != null) {
                    Item subItem = REGISTRY_1_13.get(new Identifier(newName));
                    if (subItem == null)
                        subItem = Registry.ITEM.get(new Identifier(newName));
                    if (subItem != AIR && Registry.ITEM.getRawId(subItem) == 0) {
                        RegistryKey<Item> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(newName));
                        registry.registerInPlace(subItem, meta << 16 | itemId, key, false);
                        OLD_ITEM_TO_NEW.put(Pair.of(baseItem, meta), subItem);
                    }
                }
            }
        }

        int nextHighBits = 16;
        int spawnEggId = Registry.ITEM.getRawId(BAT_SPAWN_EGG);
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            SpawnEggItem item = SpawnEggItem.forEntity(entityType);
            if (item != null && item != BAT_SPAWN_EGG) {
                var key = REGISTRY_1_13.getKey(item);
                if (key.isPresent()) {
                    registry.registerInPlace(item, nextHighBits << 16 | spawnEggId, key.get(), false);
                    nextHighBits++;
                }
            }
        }
    }

    public static void registerItems(ISimpleRegistry<Item> registry) {
        if (REGISTRY_1_13 == null)
            REGISTRY_1_13 = registry.copy();

        registry.clear(false);
        OLD_ITEM_TO_NEW.clear();

        registerBlockItem(registry, Blocks.AIR);
        registerBlockItem(registry, Blocks.STONE);
        registerBlockItem(registry, Blocks.GRASS_BLOCK);
        registerBlockItem(registry, Blocks.DIRT);
        registerBlockItem(registry, Blocks.COBBLESTONE);
        registerBlockItem(registry, Blocks.OAK_PLANKS);
        registerBlockItem(registry, Blocks.OAK_SAPLING);
        registerBlockItem(registry, Blocks.BEDROCK);
        registerBlockItem(registry, Blocks.SAND);
        registerBlockItem(registry, Blocks.GRAVEL);
        registerBlockItem(registry, Blocks.GOLD_ORE);
        registerBlockItem(registry, Blocks.IRON_ORE);
        registerBlockItem(registry, Blocks.COAL_ORE);
        registerBlockItem(registry, Blocks.OAK_LOG);
        registerBlockItem(registry, Blocks.ACACIA_LOG);
        registerBlockItem(registry, Blocks.OAK_LEAVES);
        registerBlockItem(registry, Blocks.ACACIA_LEAVES);
        registerBlockItem(registry, Blocks.SPONGE);
        registerBlockItem(registry, Blocks.GLASS);
        registerBlockItem(registry, Blocks.LAPIS_ORE);
        registerBlockItem(registry, Blocks.LAPIS_BLOCK);
        registerBlockItem(registry, Blocks.DISPENSER);
        registerBlockItem(registry, Blocks.SANDSTONE);
        registerBlockItem(registry, Blocks.NOTE_BLOCK);
        registerBlockItem(registry, Blocks.POWERED_RAIL);
        registerBlockItem(registry, Blocks.DETECTOR_RAIL);
        registerBlockItem(registry, Blocks.STICKY_PISTON);
        registerBlockItem(registry, Blocks.COBWEB);
        registerBlockItem(registry, Blocks.TALL_GRASS);
        registerBlockItem(registry, Blocks.DEAD_BUSH);
        registerBlockItem(registry, Blocks.PISTON);
        registerBlockItem(registry, Blocks.WHITE_WOOL);
        registerBlockItem(registry, Blocks.DANDELION);
        registerBlockItem(registry, Blocks.POPPY);
        registerBlockItem(registry, Blocks.BROWN_MUSHROOM);
        registerBlockItem(registry, Blocks.RED_MUSHROOM);
        registerBlockItem(registry, Blocks.GOLD_BLOCK);
        registerBlockItem(registry, Blocks.IRON_BLOCK);
        registerBlockItem(registry, Blocks.SMOOTH_STONE_SLAB);
        registerBlockItem(registry, Blocks.BRICKS);
        registerBlockItem(registry, Blocks.TNT);
        registerBlockItem(registry, Blocks.BOOKSHELF);
        registerBlockItem(registry, Blocks.MOSSY_COBBLESTONE);
        registerBlockItem(registry, Blocks.OBSIDIAN);
        registerBlockItem(registry, Blocks.TORCH);
        registerBlockItem(registry, Blocks.END_ROD);
        registerBlockItem(registry, Blocks.CHORUS_PLANT);
        registerBlockItem(registry, Blocks.CHORUS_FLOWER);
        registerBlockItem(registry, Blocks.PURPUR_BLOCK);
        registerBlockItem(registry, Blocks.PURPUR_PILLAR);
        registerBlockItem(registry, Blocks.PURPUR_STAIRS);
        registerBlockItem(registry, Blocks.PURPUR_SLAB);
        registerBlockItem(registry, Blocks.SPAWNER);
        registerBlockItem(registry, Blocks.OAK_STAIRS);
        registerBlockItem(registry, Blocks.CHEST);
        registerBlockItem(registry, Blocks.DIAMOND_ORE);
        registerBlockItem(registry, Blocks.DIAMOND_BLOCK);
        registerBlockItem(registry, Blocks.CRAFTING_TABLE);
        registerBlockItem(registry, Blocks.FARMLAND);
        registerBlockItem(registry, Blocks.FURNACE);
        registerBlockItem(registry, Blocks.LADDER);
        registerBlockItem(registry, Blocks.RAIL);
        registerBlockItem(registry, Blocks.COBBLESTONE_STAIRS);
        registerBlockItem(registry, Blocks.LEVER);
        registerBlockItem(registry, Blocks.STONE_PRESSURE_PLATE);
        registerBlockItem(registry, Blocks.OAK_PRESSURE_PLATE);
        registerBlockItem(registry, Blocks.REDSTONE_ORE);
        registerBlockItem(registry, Blocks.REDSTONE_TORCH);
        registerBlockItem(registry, Blocks.STONE_BUTTON);
        registerBlockItem(registry, Blocks.SNOW);
        registerBlockItem(registry, Blocks.ICE);
        registerBlockItem(registry, Blocks.SNOW_BLOCK);
        registerBlockItem(registry, Blocks.CACTUS);
        registerBlockItem(registry, Blocks.CLAY);
        registerBlockItem(registry, Blocks.JUKEBOX);
        registerBlockItem(registry, Blocks.OAK_FENCE);
        registerBlockItem(registry, Blocks.SPRUCE_FENCE);
        registerBlockItem(registry, Blocks.BIRCH_FENCE);
        registerBlockItem(registry, Blocks.JUNGLE_FENCE);
        registerBlockItem(registry, Blocks.DARK_OAK_FENCE);
        registerBlockItem(registry, Blocks.ACACIA_FENCE);
        registerBlockItem(registry, Blocks.CARVED_PUMPKIN);
        registerBlockItem(registry, Blocks.NETHERRACK);
        registerBlockItem(registry, Blocks.SOUL_SAND);
        registerBlockItem(registry, Blocks.GLOWSTONE);
        registerBlockItem(registry, Blocks.JACK_O_LANTERN);
        registerBlockItem(registry, Blocks.OAK_TRAPDOOR);
        registerBlockItem(registry, Blocks.INFESTED_STONE);
        registerBlockItem(registry, Blocks.STONE_BRICKS);
        registerBlockItem(registry, Blocks.BROWN_MUSHROOM_BLOCK);
        registerBlockItem(registry, Blocks.RED_MUSHROOM_BLOCK);
        registerBlockItem(registry, Blocks.IRON_BARS);
        registerBlockItem(registry, Blocks.GLASS_PANE);
        registerBlockItem(registry, Blocks.MELON);
        registerBlockItem(registry, Blocks.VINE);
        registerBlockItem(registry, Blocks.OAK_FENCE_GATE);
        registerBlockItem(registry, Blocks.SPRUCE_FENCE_GATE);
        registerBlockItem(registry, Blocks.BIRCH_FENCE_GATE);
        registerBlockItem(registry, Blocks.JUNGLE_FENCE_GATE);
        registerBlockItem(registry, Blocks.DARK_OAK_FENCE_GATE);
        registerBlockItem(registry, Blocks.ACACIA_FENCE_GATE);
        registerBlockItem(registry, Blocks.BRICK_STAIRS);
        registerBlockItem(registry, Blocks.STONE_BRICK_STAIRS);
        registerBlockItem(registry, Blocks.MYCELIUM);
        registerBlockItem(registry, Blocks.LILY_PAD);
        registerBlockItem(registry, Blocks.NETHER_BRICKS);
        registerBlockItem(registry, Blocks.NETHER_BRICK_FENCE);
        registerBlockItem(registry, Blocks.NETHER_BRICK_STAIRS);
        registerBlockItem(registry, Blocks.ENCHANTING_TABLE);
        registerBlockItem(registry, Blocks.END_PORTAL_FRAME);
        registerBlockItem(registry, Blocks.END_STONE);
        registerBlockItem(registry, Blocks.END_STONE_BRICKS);
        registerBlockItem(registry, Blocks.DRAGON_EGG);
        registerBlockItem(registry, Blocks.REDSTONE_LAMP);
        registerBlockItem(registry, Blocks.OAK_SLAB);
        registerBlockItem(registry, Blocks.SANDSTONE_STAIRS);
        registerBlockItem(registry, Blocks.EMERALD_ORE);
        registerBlockItem(registry, Blocks.ENDER_CHEST);
        registerBlockItem(registry, Blocks.TRIPWIRE_HOOK);
        registerBlockItem(registry, Blocks.EMERALD_BLOCK);
        registerBlockItem(registry, Blocks.SPRUCE_STAIRS);
        registerBlockItem(registry, Blocks.BIRCH_STAIRS);
        registerBlockItem(registry, Blocks.JUNGLE_STAIRS);
        registerBlockItem(registry, Blocks.COMMAND_BLOCK);
        registerBlockItem(registry, Blocks.BEACON);
        registerBlockItem(registry, Blocks.COBBLESTONE_WALL);
        registerBlockItem(registry, Blocks.OAK_BUTTON);
        registerBlockItem(registry, Blocks.ANVIL);
        registerBlockItem(registry, Blocks.TRAPPED_CHEST);
        registerBlockItem(registry, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        registerBlockItem(registry, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        registerBlockItem(registry, Blocks.DAYLIGHT_DETECTOR);
        registerBlockItem(registry, Blocks.REDSTONE_BLOCK);
        registerBlockItem(registry, Blocks.NETHER_QUARTZ_ORE);
        registerBlockItem(registry, Blocks.HOPPER);
        registerBlockItem(registry, Blocks.QUARTZ_BLOCK);
        registerBlockItem(registry, Blocks.QUARTZ_STAIRS);
        registerBlockItem(registry, Blocks.ACTIVATOR_RAIL);
        registerBlockItem(registry, Blocks.DROPPER);
        registerBlockItem(registry, Blocks.WHITE_TERRACOTTA);
        registerBlockItem(registry, Blocks.BARRIER);
        registerBlockItem(registry, Blocks.IRON_TRAPDOOR);
        registerBlockItem(registry, Blocks.HAY_BLOCK);
        registerBlockItem(registry, Blocks.WHITE_CARPET);
        registerBlockItem(registry, Blocks.TERRACOTTA);
        registerBlockItem(registry, Blocks.COAL_BLOCK);
        registerBlockItem(registry, Blocks.PACKED_ICE);
        registerBlockItem(registry, Blocks.ACACIA_STAIRS);
        registerBlockItem(registry, Blocks.DARK_OAK_STAIRS);
        registerBlockItem(registry, Blocks.SLIME_BLOCK);
        registerBlockItem(registry, Blocks.DIRT_PATH);
        registerBlockItem(registry, Blocks.SUNFLOWER);
        registerBlockItem(registry, Blocks.WHITE_STAINED_GLASS);
        registerBlockItem(registry, Blocks.WHITE_STAINED_GLASS_PANE);
        registerBlockItem(registry, Blocks.PRISMARINE);
        registerBlockItem(registry, Blocks.SEA_LANTERN);
        registerBlockItem(registry, Blocks.RED_SANDSTONE);
        registerBlockItem(registry, Blocks.RED_SANDSTONE_STAIRS);
        registerBlockItem(registry, Blocks.RED_SANDSTONE_SLAB);
        registerBlockItem(registry, Blocks.REPEATING_COMMAND_BLOCK);
        registerBlockItem(registry, Blocks.CHAIN_COMMAND_BLOCK);
        registerBlockItem(registry, Blocks.MAGMA_BLOCK);
        registerBlockItem(registry, Blocks.NETHER_WART_BLOCK);
        registerBlockItem(registry, Blocks.RED_NETHER_BRICKS);
        registerBlockItem(registry, Blocks.BONE_BLOCK);
        registerBlockItem(registry, Blocks.STRUCTURE_VOID);
        registerBlockItem(registry, Blocks.OBSERVER);
        registerBlockItem(registry, Blocks.WHITE_SHULKER_BOX);
        registerBlockItem(registry, Blocks.ORANGE_SHULKER_BOX);
        registerBlockItem(registry, Blocks.MAGENTA_SHULKER_BOX);
        registerBlockItem(registry, Blocks.LIGHT_BLUE_SHULKER_BOX);
        registerBlockItem(registry, Blocks.YELLOW_SHULKER_BOX);
        registerBlockItem(registry, Blocks.LIME_SHULKER_BOX);
        registerBlockItem(registry, Blocks.PINK_SHULKER_BOX);
        registerBlockItem(registry, Blocks.GRAY_SHULKER_BOX);
        registerBlockItem(registry, Blocks.LIGHT_GRAY_SHULKER_BOX);
        registerBlockItem(registry, Blocks.CYAN_SHULKER_BOX);
        registerBlockItem(registry, Blocks.PURPLE_SHULKER_BOX);
        registerBlockItem(registry, Blocks.BLUE_SHULKER_BOX);
        registerBlockItem(registry, Blocks.BROWN_SHULKER_BOX);
        registerBlockItem(registry, Blocks.GREEN_SHULKER_BOX);
        registerBlockItem(registry, Blocks.RED_SHULKER_BOX);
        registerBlockItem(registry, Blocks.BLACK_SHULKER_BOX);
        registerBlockItem(registry, Blocks.WHITE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.ORANGE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.MAGENTA_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.YELLOW_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.LIME_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.PINK_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.GRAY_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.CYAN_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.PURPLE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.BLUE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.BROWN_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.GREEN_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.RED_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.BLACK_GLAZED_TERRACOTTA);
        registerBlockItem(registry, Blocks.WHITE_CONCRETE);
        registerBlockItem(registry, Blocks.WHITE_CONCRETE_POWDER);
        registerBlockItem(registry, Blocks.STRUCTURE_BLOCK);
        register(registry, IRON_SHOVEL, 256, "iron_shovel");
        register(registry, IRON_PICKAXE, 257, "iron_pickaxe");
        register(registry, IRON_AXE, 258, "iron_axe");
        register(registry, FLINT_AND_STEEL, 259, "flint_and_steel");
        register(registry, APPLE, 260, "apple");
        register(registry, BOW, 261, "bow");
        register(registry, ARROW, 262, "arrow");
        register(registry, COAL, 263, "coal");
        register(registry, DIAMOND, 264, "diamond");
        register(registry, IRON_INGOT, 265, "iron_ingot");
        register(registry, GOLD_INGOT, 266, "gold_ingot");
        register(registry, IRON_SWORD, 267, "iron_sword");
        register(registry, WOODEN_SWORD, 268, "wooden_sword");
        register(registry, WOODEN_SHOVEL, 269, "wooden_shovel");
        register(registry, WOODEN_PICKAXE, 270, "wooden_pickaxe");
        register(registry, WOODEN_AXE, 271, "wooden_axe");
        register(registry, STONE_SWORD, 272, "stone_sword");
        register(registry, STONE_SHOVEL, 273, "stone_shovel");
        register(registry, STONE_PICKAXE, 274, "stone_pickaxe");
        register(registry, STONE_AXE, 275, "stone_axe");
        register(registry, DIAMOND_SWORD, 276, "diamond_sword");
        register(registry, DIAMOND_SHOVEL, 277, "diamond_shovel");
        register(registry, DIAMOND_PICKAXE, 278, "diamond_pickaxe");
        register(registry, DIAMOND_AXE, 279, "diamond_axe");
        register(registry, STICK, 280, "stick");
        register(registry, BOWL, 281, "bowl");
        register(registry, MUSHROOM_STEW, 282, "mushroom_stew");
        register(registry, GOLDEN_SWORD, 283, "golden_sword");
        register(registry, GOLDEN_SHOVEL, 284, "golden_shovel");
        register(registry, GOLDEN_PICKAXE, 285, "golden_pickaxe");
        register(registry, GOLDEN_AXE, 286, "golden_axe");
        register(registry, STRING, 287, "string");
        register(registry, FEATHER, 288, "feather");
        register(registry, GUNPOWDER, 289, "gunpowder");
        register(registry, WOODEN_HOE, 290, "wooden_hoe");
        register(registry, STONE_HOE, 291, "stone_hoe");
        register(registry, IRON_HOE, 292, "iron_hoe");
        register(registry, DIAMOND_HOE, 293, "diamond_hoe");
        register(registry, GOLDEN_HOE, 294, "golden_hoe");
        register(registry, WHEAT_SEEDS, 295, "wheat_seeds");
        register(registry, WHEAT, 296, "wheat");
        register(registry, BREAD, 297, "bread");
        register(registry, LEATHER_HELMET, 298, "leather_helmet");
        register(registry, LEATHER_CHESTPLATE, 299, "leather_chestplate");
        register(registry, LEATHER_LEGGINGS, 300, "leather_leggings");
        register(registry, LEATHER_BOOTS, 301, "leather_boots");
        register(registry, CHAINMAIL_HELMET, 302, "chainmail_helmet");
        register(registry, CHAINMAIL_CHESTPLATE, 303, "chainmail_chestplate");
        register(registry, CHAINMAIL_LEGGINGS, 304, "chainmail_leggings");
        register(registry, CHAINMAIL_BOOTS, 305, "chainmail_boots");
        register(registry, IRON_HELMET, 306, "iron_helmet");
        register(registry, IRON_CHESTPLATE, 307, "iron_chestplate");
        register(registry, IRON_LEGGINGS, 308, "iron_leggings");
        register(registry, IRON_BOOTS, 309, "iron_boots");
        register(registry, DIAMOND_HELMET, 310, "diamond_helmet");
        register(registry, DIAMOND_CHESTPLATE, 311, "diamond_chestplate");
        register(registry, DIAMOND_LEGGINGS, 312, "diamond_leggings");
        register(registry, DIAMOND_BOOTS, 313, "diamond_boots");
        register(registry, GOLDEN_HELMET, 314, "golden_helmet");
        register(registry, GOLDEN_CHESTPLATE, 315, "golden_chestplate");
        register(registry, GOLDEN_LEGGINGS, 316, "golden_leggings");
        register(registry, GOLDEN_BOOTS, 317, "golden_boots");
        register(registry, FLINT, 318, "flint");
        register(registry, PORKCHOP, 319, "porkchop");
        register(registry, COOKED_PORKCHOP, 320, "cooked_porkchop");
        register(registry, PAINTING, 321, "painting");
        register(registry, GOLDEN_APPLE, 322, "golden_apple");
        register(registry, OAK_SIGN, 323, "sign");
        register(registry, OAK_DOOR, 324, "wooden_door");
        register(registry, BUCKET, 325, "bucket");
        register(registry, WATER_BUCKET, 326, "water_bucket");
        register(registry, LAVA_BUCKET, 327, "lava_bucket");
        register(registry, MINECART, 328, "minecart");
        register(registry, SADDLE, 329, "saddle");
        register(registry, IRON_DOOR, 330, "iron_door");
        register(registry, REDSTONE, 331, "redstone");
        register(registry, SNOWBALL, 332, "snowball");
        register(registry, OAK_BOAT, 333, "boat");
        register(registry, LEATHER, 334, "leather");
        register(registry, MILK_BUCKET, 335, "milk_bucket");
        register(registry, BRICK, 336, "brick");
        register(registry, CLAY_BALL, 337, "clay_ball");
        register(registry, SUGAR_CANE, 338, "reeds");
        register(registry, PAPER, 339, "paper");
        register(registry, BOOK, 340, "book");
        register(registry, SLIME_BALL, 341, "slime_ball");
        register(registry, CHEST_MINECART, 342, "chest_minecart");
        register(registry, FURNACE_MINECART, 343, "furnace_minecart");
        register(registry, EGG, 344, "egg");
        register(registry, COMPASS, 345, "compass");
        register(registry, FISHING_ROD, 346, "fishing_rod");
        register(registry, CLOCK, 347, "clock");
        register(registry, GLOWSTONE_DUST, 348, "glowstone_dust");
        register(registry, COD, 349, "fish");
        register(registry, COOKED_COD, 350, "cooked_fish");
        register(registry, INK_SAC, 351, "dye");
        register(registry, BONE, 352, "bone");
        register(registry, SUGAR, 353, "sugar");
        register(registry, CAKE, 354, "cake");
        register(registry, WHITE_BED, 355, "bed");
        register(registry, REPEATER, 356, "repeater");
        register(registry, COOKIE, 357, "cookie");
        register(registry, FILLED_MAP, 358, "filled_map");
        register(registry, SHEARS, 359, "shears");
        register(registry, MELON_SLICE, 360, "melon");
        register(registry, PUMPKIN_SEEDS, 361, "pumpkin_seeds");
        register(registry, MELON_SEEDS, 362, "melon_seeds");
        register(registry, BEEF, 363, "beef");
        register(registry, COOKED_BEEF, 364, "cooked_beef");
        register(registry, CHICKEN, 365, "chicken");
        register(registry, COOKED_CHICKEN, 366, "cooked_chicken");
        register(registry, ROTTEN_FLESH, 367, "rotten_flesh");
        register(registry, ENDER_PEARL, 368, "ender_pearl");
        register(registry, BLAZE_ROD, 369, "blaze_rod");
        register(registry, GHAST_TEAR, 370, "ghast_tear");
        register(registry, GOLD_NUGGET, 371, "gold_nugget");
        register(registry, NETHER_WART, 372, "nether_wart");
        register(registry, POTION, 373, "potion");
        register(registry, GLASS_BOTTLE, 374, "glass_bottle");
        register(registry, SPIDER_EYE, 375, "spider_eye");
        register(registry, FERMENTED_SPIDER_EYE, 376, "fermented_spider_eye");
        register(registry, BLAZE_POWDER, 377, "blaze_powder");
        register(registry, MAGMA_CREAM, 378, "magma_cream");
        register(registry, BREWING_STAND, 379, "brewing_stand");
        register(registry, CAULDRON, 380, "cauldron");
        register(registry, ENDER_EYE, 381, "ender_eye");
        register(registry, GLISTERING_MELON_SLICE, 382, "speckled_melon");
        register(registry, BAT_SPAWN_EGG, 383, "spawn_egg");
        register(registry, EXPERIENCE_BOTTLE, 384, "experience_bottle");
        register(registry, FIRE_CHARGE, 385, "fire_charge");
        register(registry, WRITABLE_BOOK, 386, "writable_book");
        register(registry, WRITTEN_BOOK, 387, "written_book");
        register(registry, EMERALD, 388, "emerald");
        register(registry, ITEM_FRAME, 389, "item_frame");
        register(registry, FLOWER_POT, 390, "flower_pot");
        register(registry, CARROT, 391, "carrot");
        register(registry, POTATO, 392, "potato");
        register(registry, BAKED_POTATO, 393, "baked_potato");
        register(registry, POISONOUS_POTATO, 394, "poisonous_potato");
        register(registry, MAP, 395, "map");
        register(registry, GOLDEN_CARROT, 396, "golden_carrot");
        register(registry, SKELETON_SKULL, 397, "skull");
        register(registry, CARROT_ON_A_STICK, 398, "carrot_on_a_stick");
        register(registry, NETHER_STAR, 399, "nether_star");
        register(registry, PUMPKIN_PIE, 400, "pumpkin_pie");
        register(registry, FIREWORK_ROCKET, 401, "fireworks");
        register(registry, FIREWORK_STAR, 402, "firework_charge");
        register(registry, ENCHANTED_BOOK, 403, "enchanted_book");
        register(registry, COMPARATOR, 404, "comparator");
        register(registry, NETHER_BRICK, 405, "netherbrick");
        register(registry, QUARTZ, 406, "quartz");
        register(registry, TNT_MINECART, 407, "tnt_minecart");
        register(registry, HOPPER_MINECART, 408, "hopper_minecart");
        register(registry, PRISMARINE_SHARD, 409, "prismarine_shard");
        register(registry, PRISMARINE_CRYSTALS, 410, "prismarine_crystals");
        register(registry, RABBIT, 411, "rabbit");
        register(registry, COOKED_RABBIT, 412, "cooked_rabbit");
        register(registry, RABBIT_STEW, 413, "rabbit_stew");
        register(registry, RABBIT_FOOT, 414, "rabbit_foot");
        register(registry, RABBIT_HIDE, 415, "rabbit_hide");
        register(registry, ARMOR_STAND, 416, "armor_stand");
        register(registry, IRON_HORSE_ARMOR, 417, "iron_horse_armor");
        register(registry, GOLDEN_HORSE_ARMOR, 418, "golden_horse_armor");
        register(registry, DIAMOND_HORSE_ARMOR, 419, "diamond_horse_armor");
        register(registry, LEAD, 420, "lead");
        register(registry, NAME_TAG, 421, "name_tag");
        register(registry, COMMAND_BLOCK_MINECART, 422, "command_block_minecart");
        register(registry, MUTTON, 423, "mutton");
        register(registry, COOKED_MUTTON, 424, "cooked_mutton");
        register(registry, BLACK_BANNER, 425, "banner");
        register(registry, END_CRYSTAL, 426, "end_crystal");
        register(registry, SPRUCE_DOOR, 427, "spruce_door");
        register(registry, BIRCH_DOOR, 428, "birch_door");
        register(registry, JUNGLE_DOOR, 429, "jungle_door");
        register(registry, ACACIA_DOOR, 430, "acacia_door");
        register(registry, DARK_OAK_DOOR, 431, "dark_oak_door");
        register(registry, CHORUS_FRUIT, 432, "chorus_fruit");
        register(registry, POPPED_CHORUS_FRUIT, 433, "chorus_fruit_popped");
        register(registry, BEETROOT, 434, "beetroot");
        register(registry, BEETROOT_SEEDS, 435, "beetroot_seeds");
        register(registry, BEETROOT_SOUP, 436, "beetroot_soup");
        register(registry, DRAGON_BREATH, 437, "dragon_breath");
        register(registry, SPLASH_POTION, 438, "splash_potion");
        register(registry, SPECTRAL_ARROW, 439, "spectral_arrow");
        register(registry, TIPPED_ARROW, 440, "tipped_arrow");
        register(registry, LINGERING_POTION, 441, "lingering_potion");
        register(registry, SHIELD, 442, "shield");
        register(registry, ELYTRA, 443, "elytra");
        register(registry, SPRUCE_BOAT, 444, "spruce_boat");
        register(registry, BIRCH_BOAT, 445, "birch_boat");
        register(registry, JUNGLE_BOAT, 446, "jungle_boat");
        register(registry, ACACIA_BOAT, 447, "acacia_boat");
        register(registry, DARK_OAK_BOAT, 448, "dark_oak_boat");
        register(registry, TOTEM_OF_UNDYING, 449, "totem_of_undying");
        register(registry, SHULKER_SHELL, 450, "shulker_shell");
        register(registry, IRON_NUGGET, 452, "iron_nugget");
        register(registry, KNOWLEDGE_BOOK, 453, "knowledge_book");
        register(registry, MUSIC_DISC_13, 2256, "record_13");
        register(registry, MUSIC_DISC_CAT, 2257, "record_cat");
        register(registry, MUSIC_DISC_BLOCKS, 2258, "record_blocks");
        register(registry, MUSIC_DISC_CHIRP, 2259, "record_chirp");
        register(registry, MUSIC_DISC_FAR, 2260, "record_far");
        register(registry, MUSIC_DISC_MALL, 2261, "record_mall");
        register(registry, MUSIC_DISC_MELLOHI, 2262, "record_mellohi");
        register(registry, MUSIC_DISC_STAL, 2263, "record_stal");
        register(registry, MUSIC_DISC_STRAD, 2264, "record_strad");
        register(registry, MUSIC_DISC_WARD, 2265, "record_ward");
        register(registry, MUSIC_DISC_11, 2266, "record_11");
        register(registry, MUSIC_DISC_WAIT, 2267, "record_wait");

        registerAliases(registry);
    }

}
