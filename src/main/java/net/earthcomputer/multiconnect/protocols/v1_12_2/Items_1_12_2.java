package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
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

    private static void registerBlockItem(RegistryBuilder<Item> registry, RegistryBuilder<Block> blockRegistry, Block block) {
        registry.registerInPlace(blockRegistry.getRawId(block), Item.BLOCK_ITEMS.getOrDefault(block, AIR), blockRegistry.getId(block));
    }

    private static void registerAliases(RegistryBuilder<Item> registry) {
        Item[] itemsByRawId = new Item[452];
        for (Item item : registry.getEntries()) {
            int id = registry.getRawId(item);
            if (id == 0) {
                continue;
            }
            if (id >= 453) {
                break;
            }
            itemsByRawId[id - 1] = item;
        }

        for (int meta = 1; meta < 16; meta++) {
            for (int itemId = 1; itemId < 453; itemId++) {
                Item baseItem = itemsByRawId[itemId - 1];
                String baseName = registry.getId(baseItem).toString();
                String newName = ItemInstanceTheFlatteningFix.getItem(baseName, meta);
                newName = EntityTheRenameningBlock.ITEMS.getOrDefault(newName, newName);
                if (newName != null) {
                    Identifier newId = new Identifier(newName);
                    Item subItem = REGISTRY_1_13.get(newId);
                    if (subItem == null) {
                        System.out.println("FIXME: item for " + newName + " is null!");
                        continue;
                    }
                    if (!registry.contains(subItem)) {
                        registry.registerInPlace(meta << 16 | itemId, subItem, newId);
                        OLD_ITEM_TO_NEW.put(Pair.of(baseItem, meta), subItem);
                    }
                }
            }
        }

        int nextHighBits = 16;
        int spawnEggId = registry.getRawId(BAT_SPAWN_EGG);
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            SpawnEggItem item = SpawnEggItem.forEntity(entityType);
            if (item != null && item != BAT_SPAWN_EGG) {
                var key = REGISTRY_1_13.getKey(item);
                if (key.isPresent()) {
                    registry.registerInPlace(nextHighBits << 16 | spawnEggId, item, key.get().getValue());
                    nextHighBits++;
                }
            }
        }
    }

    public static void registerItems(RegistryBuilder<Item> registry) {
        if (REGISTRY_1_13 == null)
            REGISTRY_1_13 = registry.createCopiedRegistry();

        registry.disableSideEffects();
        registry.clear();
        OLD_ITEM_TO_NEW.clear();

        RegistryBuilder<Block> blockRegistry = registry.getOtherBuilder(Registry.BLOCK_KEY);

        registerBlockItem(registry, blockRegistry, Blocks.AIR);
        registerBlockItem(registry, blockRegistry, Blocks.STONE);
        registerBlockItem(registry, blockRegistry, Blocks.GRASS_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.DIRT);
        registerBlockItem(registry, blockRegistry, Blocks.COBBLESTONE);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_PLANKS);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_SAPLING);
        registerBlockItem(registry, blockRegistry, Blocks.BEDROCK);
        registerBlockItem(registry, blockRegistry, Blocks.SAND);
        registerBlockItem(registry, blockRegistry, Blocks.GRAVEL);
        registerBlockItem(registry, blockRegistry, Blocks.GOLD_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.IRON_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.COAL_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_LOG);
        registerBlockItem(registry, blockRegistry, Blocks.ACACIA_LOG);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_LEAVES);
        registerBlockItem(registry, blockRegistry, Blocks.ACACIA_LEAVES);
        registerBlockItem(registry, blockRegistry, Blocks.SPONGE);
        registerBlockItem(registry, blockRegistry, Blocks.GLASS);
        registerBlockItem(registry, blockRegistry, Blocks.LAPIS_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.LAPIS_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.DISPENSER);
        registerBlockItem(registry, blockRegistry, Blocks.SANDSTONE);
        registerBlockItem(registry, blockRegistry, Blocks.NOTE_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.POWERED_RAIL);
        registerBlockItem(registry, blockRegistry, Blocks.DETECTOR_RAIL);
        registerBlockItem(registry, blockRegistry, Blocks.STICKY_PISTON);
        registerBlockItem(registry, blockRegistry, Blocks.COBWEB);
        registerBlockItem(registry, blockRegistry, Blocks.TALL_GRASS);
        registerBlockItem(registry, blockRegistry, Blocks.DEAD_BUSH);
        registerBlockItem(registry, blockRegistry, Blocks.PISTON);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_WOOL);
        registerBlockItem(registry, blockRegistry, Blocks.DANDELION);
        registerBlockItem(registry, blockRegistry, Blocks.POPPY);
        registerBlockItem(registry, blockRegistry, Blocks.BROWN_MUSHROOM);
        registerBlockItem(registry, blockRegistry, Blocks.RED_MUSHROOM);
        registerBlockItem(registry, blockRegistry, Blocks.GOLD_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.IRON_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.SMOOTH_STONE_SLAB);
        registerBlockItem(registry, blockRegistry, Blocks.BRICKS);
        registerBlockItem(registry, blockRegistry, Blocks.TNT);
        registerBlockItem(registry, blockRegistry, Blocks.BOOKSHELF);
        registerBlockItem(registry, blockRegistry, Blocks.MOSSY_COBBLESTONE);
        registerBlockItem(registry, blockRegistry, Blocks.OBSIDIAN);
        registerBlockItem(registry, blockRegistry, Blocks.TORCH);
        registerBlockItem(registry, blockRegistry, Blocks.END_ROD);
        registerBlockItem(registry, blockRegistry, Blocks.CHORUS_PLANT);
        registerBlockItem(registry, blockRegistry, Blocks.CHORUS_FLOWER);
        registerBlockItem(registry, blockRegistry, Blocks.PURPUR_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.PURPUR_PILLAR);
        registerBlockItem(registry, blockRegistry, Blocks.PURPUR_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.PURPUR_SLAB);
        registerBlockItem(registry, blockRegistry, Blocks.SPAWNER);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.CHEST);
        registerBlockItem(registry, blockRegistry, Blocks.DIAMOND_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.DIAMOND_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.CRAFTING_TABLE);
        registerBlockItem(registry, blockRegistry, Blocks.FARMLAND);
        registerBlockItem(registry, blockRegistry, Blocks.FURNACE);
        registerBlockItem(registry, blockRegistry, Blocks.LADDER);
        registerBlockItem(registry, blockRegistry, Blocks.RAIL);
        registerBlockItem(registry, blockRegistry, Blocks.COBBLESTONE_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.LEVER);
        registerBlockItem(registry, blockRegistry, Blocks.STONE_PRESSURE_PLATE);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_PRESSURE_PLATE);
        registerBlockItem(registry, blockRegistry, Blocks.REDSTONE_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.REDSTONE_TORCH);
        registerBlockItem(registry, blockRegistry, Blocks.STONE_BUTTON);
        registerBlockItem(registry, blockRegistry, Blocks.SNOW);
        registerBlockItem(registry, blockRegistry, Blocks.ICE);
        registerBlockItem(registry, blockRegistry, Blocks.SNOW_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.CACTUS);
        registerBlockItem(registry, blockRegistry, Blocks.CLAY);
        registerBlockItem(registry, blockRegistry, Blocks.JUKEBOX);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.SPRUCE_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.BIRCH_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.JUNGLE_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.DARK_OAK_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.ACACIA_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.CARVED_PUMPKIN);
        registerBlockItem(registry, blockRegistry, Blocks.NETHERRACK);
        registerBlockItem(registry, blockRegistry, Blocks.SOUL_SAND);
        registerBlockItem(registry, blockRegistry, Blocks.GLOWSTONE);
        registerBlockItem(registry, blockRegistry, Blocks.JACK_O_LANTERN);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_TRAPDOOR);
        registerBlockItem(registry, blockRegistry, Blocks.INFESTED_STONE);
        registerBlockItem(registry, blockRegistry, Blocks.STONE_BRICKS);
        registerBlockItem(registry, blockRegistry, Blocks.BROWN_MUSHROOM_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.RED_MUSHROOM_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.IRON_BARS);
        registerBlockItem(registry, blockRegistry, Blocks.GLASS_PANE);
        registerBlockItem(registry, blockRegistry, Blocks.MELON);
        registerBlockItem(registry, blockRegistry, Blocks.VINE);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_FENCE_GATE);
        registerBlockItem(registry, blockRegistry, Blocks.SPRUCE_FENCE_GATE);
        registerBlockItem(registry, blockRegistry, Blocks.BIRCH_FENCE_GATE);
        registerBlockItem(registry, blockRegistry, Blocks.JUNGLE_FENCE_GATE);
        registerBlockItem(registry, blockRegistry, Blocks.DARK_OAK_FENCE_GATE);
        registerBlockItem(registry, blockRegistry, Blocks.ACACIA_FENCE_GATE);
        registerBlockItem(registry, blockRegistry, Blocks.BRICK_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.STONE_BRICK_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.MYCELIUM);
        registerBlockItem(registry, blockRegistry, Blocks.LILY_PAD);
        registerBlockItem(registry, blockRegistry, Blocks.NETHER_BRICKS);
        registerBlockItem(registry, blockRegistry, Blocks.NETHER_BRICK_FENCE);
        registerBlockItem(registry, blockRegistry, Blocks.NETHER_BRICK_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.ENCHANTING_TABLE);
        registerBlockItem(registry, blockRegistry, Blocks.END_PORTAL_FRAME);
        registerBlockItem(registry, blockRegistry, Blocks.END_STONE);
        registerBlockItem(registry, blockRegistry, Blocks.END_STONE_BRICKS);
        registerBlockItem(registry, blockRegistry, Blocks.DRAGON_EGG);
        registerBlockItem(registry, blockRegistry, Blocks.REDSTONE_LAMP);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_SLAB);
        registerBlockItem(registry, blockRegistry, Blocks.SANDSTONE_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.EMERALD_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.ENDER_CHEST);
        registerBlockItem(registry, blockRegistry, Blocks.TRIPWIRE_HOOK);
        registerBlockItem(registry, blockRegistry, Blocks.EMERALD_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.SPRUCE_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.BIRCH_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.JUNGLE_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.COMMAND_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.BEACON);
        registerBlockItem(registry, blockRegistry, Blocks.COBBLESTONE_WALL);
        registerBlockItem(registry, blockRegistry, Blocks.OAK_BUTTON);
        registerBlockItem(registry, blockRegistry, Blocks.ANVIL);
        registerBlockItem(registry, blockRegistry, Blocks.TRAPPED_CHEST);
        registerBlockItem(registry, blockRegistry, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        registerBlockItem(registry, blockRegistry, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        registerBlockItem(registry, blockRegistry, Blocks.DAYLIGHT_DETECTOR);
        registerBlockItem(registry, blockRegistry, Blocks.REDSTONE_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.NETHER_QUARTZ_ORE);
        registerBlockItem(registry, blockRegistry, Blocks.HOPPER);
        registerBlockItem(registry, blockRegistry, Blocks.QUARTZ_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.QUARTZ_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.ACTIVATOR_RAIL);
        registerBlockItem(registry, blockRegistry, Blocks.DROPPER);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.BARRIER);
        registerBlockItem(registry, blockRegistry, Blocks.IRON_TRAPDOOR);
        registerBlockItem(registry, blockRegistry, Blocks.HAY_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_CARPET);
        registerBlockItem(registry, blockRegistry, Blocks.TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.COAL_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.PACKED_ICE);
        registerBlockItem(registry, blockRegistry, Blocks.ACACIA_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.DARK_OAK_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.SLIME_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.DIRT_PATH);
        registerBlockItem(registry, blockRegistry, Blocks.SUNFLOWER);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_STAINED_GLASS);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_STAINED_GLASS_PANE);
        registerBlockItem(registry, blockRegistry, Blocks.PRISMARINE);
        registerBlockItem(registry, blockRegistry, Blocks.SEA_LANTERN);
        registerBlockItem(registry, blockRegistry, Blocks.RED_SANDSTONE);
        registerBlockItem(registry, blockRegistry, Blocks.RED_SANDSTONE_STAIRS);
        registerBlockItem(registry, blockRegistry, Blocks.RED_SANDSTONE_SLAB);
        registerBlockItem(registry, blockRegistry, Blocks.REPEATING_COMMAND_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.CHAIN_COMMAND_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.MAGMA_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.NETHER_WART_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.RED_NETHER_BRICKS);
        registerBlockItem(registry, blockRegistry, Blocks.BONE_BLOCK);
        registerBlockItem(registry, blockRegistry, Blocks.STRUCTURE_VOID);
        registerBlockItem(registry, blockRegistry, Blocks.OBSERVER);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.ORANGE_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.MAGENTA_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.LIGHT_BLUE_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.YELLOW_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.LIME_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.PINK_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.GRAY_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.LIGHT_GRAY_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.CYAN_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.PURPLE_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.BLUE_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.BROWN_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.GREEN_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.RED_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.BLACK_SHULKER_BOX);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.ORANGE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.MAGENTA_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.YELLOW_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.LIME_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.PINK_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.GRAY_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.CYAN_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.PURPLE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.BLUE_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.BROWN_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.GREEN_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.RED_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.BLACK_GLAZED_TERRACOTTA);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_CONCRETE);
        registerBlockItem(registry, blockRegistry, Blocks.WHITE_CONCRETE_POWDER);
        registerBlockItem(registry, blockRegistry, Blocks.STRUCTURE_BLOCK);
        registry.registerInPlace(256, IRON_SHOVEL, "iron_shovel");
        registry.registerInPlace(257, IRON_PICKAXE, "iron_pickaxe");
        registry.registerInPlace(258, IRON_AXE, "iron_axe");
        registry.registerInPlace(259, FLINT_AND_STEEL, "flint_and_steel");
        registry.registerInPlace(260, APPLE, "apple");
        registry.registerInPlace(261, BOW, "bow");
        registry.registerInPlace(262, ARROW, "arrow");
        registry.registerInPlace(263, COAL, "coal");
        registry.registerInPlace(264, DIAMOND, "diamond");
        registry.registerInPlace(265, IRON_INGOT, "iron_ingot");
        registry.registerInPlace(266, GOLD_INGOT, "gold_ingot");
        registry.registerInPlace(267, IRON_SWORD, "iron_sword");
        registry.registerInPlace(268, WOODEN_SWORD, "wooden_sword");
        registry.registerInPlace(269, WOODEN_SHOVEL, "wooden_shovel");
        registry.registerInPlace(270, WOODEN_PICKAXE, "wooden_pickaxe");
        registry.registerInPlace(271, WOODEN_AXE, "wooden_axe");
        registry.registerInPlace(272, STONE_SWORD, "stone_sword");
        registry.registerInPlace(273, STONE_SHOVEL, "stone_shovel");
        registry.registerInPlace(274, STONE_PICKAXE, "stone_pickaxe");
        registry.registerInPlace(275, STONE_AXE, "stone_axe");
        registry.registerInPlace(276, DIAMOND_SWORD, "diamond_sword");
        registry.registerInPlace(277, DIAMOND_SHOVEL, "diamond_shovel");
        registry.registerInPlace(278, DIAMOND_PICKAXE, "diamond_pickaxe");
        registry.registerInPlace(279, DIAMOND_AXE, "diamond_axe");
        registry.registerInPlace(280, STICK, "stick");
        registry.registerInPlace(281, BOWL, "bowl");
        registry.registerInPlace(282, MUSHROOM_STEW, "mushroom_stew");
        registry.registerInPlace(283, GOLDEN_SWORD, "golden_sword");
        registry.registerInPlace(284, GOLDEN_SHOVEL, "golden_shovel");
        registry.registerInPlace(285, GOLDEN_PICKAXE, "golden_pickaxe");
        registry.registerInPlace(286, GOLDEN_AXE, "golden_axe");
        registry.registerInPlace(287, STRING, "string");
        registry.registerInPlace(288, FEATHER, "feather");
        registry.registerInPlace(289, GUNPOWDER, "gunpowder");
        registry.registerInPlace(290, WOODEN_HOE, "wooden_hoe");
        registry.registerInPlace(291, STONE_HOE, "stone_hoe");
        registry.registerInPlace(292, IRON_HOE, "iron_hoe");
        registry.registerInPlace(293, DIAMOND_HOE, "diamond_hoe");
        registry.registerInPlace(294, GOLDEN_HOE, "golden_hoe");
        registry.registerInPlace(295, WHEAT_SEEDS, "wheat_seeds");
        registry.registerInPlace(296, WHEAT, "wheat");
        registry.registerInPlace(297, BREAD, "bread");
        registry.registerInPlace(298, LEATHER_HELMET, "leather_helmet");
        registry.registerInPlace(299, LEATHER_CHESTPLATE, "leather_chestplate");
        registry.registerInPlace(300, LEATHER_LEGGINGS, "leather_leggings");
        registry.registerInPlace(301, LEATHER_BOOTS, "leather_boots");
        registry.registerInPlace(302, CHAINMAIL_HELMET, "chainmail_helmet");
        registry.registerInPlace(303, CHAINMAIL_CHESTPLATE, "chainmail_chestplate");
        registry.registerInPlace(304, CHAINMAIL_LEGGINGS, "chainmail_leggings");
        registry.registerInPlace(305, CHAINMAIL_BOOTS, "chainmail_boots");
        registry.registerInPlace(306, IRON_HELMET, "iron_helmet");
        registry.registerInPlace(307, IRON_CHESTPLATE, "iron_chestplate");
        registry.registerInPlace(308, IRON_LEGGINGS, "iron_leggings");
        registry.registerInPlace(309, IRON_BOOTS, "iron_boots");
        registry.registerInPlace(310, DIAMOND_HELMET, "diamond_helmet");
        registry.registerInPlace(311, DIAMOND_CHESTPLATE, "diamond_chestplate");
        registry.registerInPlace(312, DIAMOND_LEGGINGS, "diamond_leggings");
        registry.registerInPlace(313, DIAMOND_BOOTS, "diamond_boots");
        registry.registerInPlace(314, GOLDEN_HELMET, "golden_helmet");
        registry.registerInPlace(315, GOLDEN_CHESTPLATE, "golden_chestplate");
        registry.registerInPlace(316, GOLDEN_LEGGINGS, "golden_leggings");
        registry.registerInPlace(317, GOLDEN_BOOTS, "golden_boots");
        registry.registerInPlace(318, FLINT, "flint");
        registry.registerInPlace(319, PORKCHOP, "porkchop");
        registry.registerInPlace(320, COOKED_PORKCHOP, "cooked_porkchop");
        registry.registerInPlace(321, PAINTING, "painting");
        registry.registerInPlace(322, GOLDEN_APPLE, "golden_apple");
        registry.registerInPlace(323, OAK_SIGN, "sign");
        registry.registerInPlace(324, OAK_DOOR, "wooden_door");
        registry.registerInPlace(325, BUCKET, "bucket");
        registry.registerInPlace(326, WATER_BUCKET, "water_bucket");
        registry.registerInPlace(327, LAVA_BUCKET, "lava_bucket");
        registry.registerInPlace(328, MINECART, "minecart");
        registry.registerInPlace(329, SADDLE, "saddle");
        registry.registerInPlace(330, IRON_DOOR, "iron_door");
        registry.registerInPlace(331, REDSTONE, "redstone");
        registry.registerInPlace(332, SNOWBALL, "snowball");
        registry.registerInPlace(333, OAK_BOAT, "boat");
        registry.registerInPlace(334, LEATHER, "leather");
        registry.registerInPlace(335, MILK_BUCKET, "milk_bucket");
        registry.registerInPlace(336, BRICK, "brick");
        registry.registerInPlace(337, CLAY_BALL, "clay_ball");
        registry.registerInPlace(338, SUGAR_CANE, "reeds");
        registry.registerInPlace(339, PAPER, "paper");
        registry.registerInPlace(340, BOOK, "book");
        registry.registerInPlace(341, SLIME_BALL, "slime_ball");
        registry.registerInPlace(342, CHEST_MINECART, "chest_minecart");
        registry.registerInPlace(343, FURNACE_MINECART, "furnace_minecart");
        registry.registerInPlace(344, EGG, "egg");
        registry.registerInPlace(345, COMPASS, "compass");
        registry.registerInPlace(346, FISHING_ROD, "fishing_rod");
        registry.registerInPlace(347, CLOCK, "clock");
        registry.registerInPlace(348, GLOWSTONE_DUST, "glowstone_dust");
        registry.registerInPlace(349, COD, "fish");
        registry.registerInPlace(350, COOKED_COD, "cooked_fish");
        registry.registerInPlace(351, INK_SAC, "dye");
        registry.registerInPlace(352, BONE, "bone");
        registry.registerInPlace(353, SUGAR, "sugar");
        registry.registerInPlace(354, CAKE, "cake");
        registry.registerInPlace(355, WHITE_BED, "bed");
        registry.registerInPlace(356, REPEATER, "repeater");
        registry.registerInPlace(357, COOKIE, "cookie");
        registry.registerInPlace(358, FILLED_MAP, "filled_map");
        registry.registerInPlace(359, SHEARS, "shears");
        registry.registerInPlace(360, MELON_SLICE, "melon");
        registry.registerInPlace(361, PUMPKIN_SEEDS, "pumpkin_seeds");
        registry.registerInPlace(362, MELON_SEEDS, "melon_seeds");
        registry.registerInPlace(363, BEEF, "beef");
        registry.registerInPlace(364, COOKED_BEEF, "cooked_beef");
        registry.registerInPlace(365, CHICKEN, "chicken");
        registry.registerInPlace(366, COOKED_CHICKEN, "cooked_chicken");
        registry.registerInPlace(367, ROTTEN_FLESH, "rotten_flesh");
        registry.registerInPlace(368, ENDER_PEARL, "ender_pearl");
        registry.registerInPlace(369, BLAZE_ROD, "blaze_rod");
        registry.registerInPlace(370, GHAST_TEAR, "ghast_tear");
        registry.registerInPlace(371, GOLD_NUGGET, "gold_nugget");
        registry.registerInPlace(372, NETHER_WART, "nether_wart");
        registry.registerInPlace(373, POTION, "potion");
        registry.registerInPlace(374, GLASS_BOTTLE, "glass_bottle");
        registry.registerInPlace(375, SPIDER_EYE, "spider_eye");
        registry.registerInPlace(376, FERMENTED_SPIDER_EYE, "fermented_spider_eye");
        registry.registerInPlace(377, BLAZE_POWDER, "blaze_powder");
        registry.registerInPlace(378, MAGMA_CREAM, "magma_cream");
        registry.registerInPlace(379, BREWING_STAND, "brewing_stand");
        registry.registerInPlace(380, CAULDRON, "cauldron");
        registry.registerInPlace(381, ENDER_EYE, "ender_eye");
        registry.registerInPlace(382, GLISTERING_MELON_SLICE, "speckled_melon");
        registry.registerInPlace(383, BAT_SPAWN_EGG, "spawn_egg");
        registry.registerInPlace(384, EXPERIENCE_BOTTLE, "experience_bottle");
        registry.registerInPlace(385, FIRE_CHARGE, "fire_charge");
        registry.registerInPlace(386, WRITABLE_BOOK, "writable_book");
        registry.registerInPlace(387, WRITTEN_BOOK, "written_book");
        registry.registerInPlace(388, EMERALD, "emerald");
        registry.registerInPlace(389, ITEM_FRAME, "item_frame");
        registry.registerInPlace(390, FLOWER_POT, "flower_pot");
        registry.registerInPlace(391, CARROT, "carrot");
        registry.registerInPlace(392, POTATO, "potato");
        registry.registerInPlace(393, BAKED_POTATO, "baked_potato");
        registry.registerInPlace(394, POISONOUS_POTATO, "poisonous_potato");
        registry.registerInPlace(395, MAP, "map");
        registry.registerInPlace(396, GOLDEN_CARROT, "golden_carrot");
        registry.registerInPlace(397, SKELETON_SKULL, "skull");
        registry.registerInPlace(398, CARROT_ON_A_STICK, "carrot_on_a_stick");
        registry.registerInPlace(399, NETHER_STAR, "nether_star");
        registry.registerInPlace(400, PUMPKIN_PIE, "pumpkin_pie");
        registry.registerInPlace(401, FIREWORK_ROCKET, "fireworks");
        registry.registerInPlace(402, FIREWORK_STAR, "firework_charge");
        registry.registerInPlace(403, ENCHANTED_BOOK, "enchanted_book");
        registry.registerInPlace(404, COMPARATOR, "comparator");
        registry.registerInPlace(405, NETHER_BRICK, "netherbrick");
        registry.registerInPlace(406, QUARTZ, "quartz");
        registry.registerInPlace(407, TNT_MINECART, "tnt_minecart");
        registry.registerInPlace(408, HOPPER_MINECART, "hopper_minecart");
        registry.registerInPlace(409, PRISMARINE_SHARD, "prismarine_shard");
        registry.registerInPlace(410, PRISMARINE_CRYSTALS, "prismarine_crystals");
        registry.registerInPlace(411, RABBIT, "rabbit");
        registry.registerInPlace(412, COOKED_RABBIT, "cooked_rabbit");
        registry.registerInPlace(413, RABBIT_STEW, "rabbit_stew");
        registry.registerInPlace(414, RABBIT_FOOT, "rabbit_foot");
        registry.registerInPlace(415, RABBIT_HIDE, "rabbit_hide");
        registry.registerInPlace(416, ARMOR_STAND, "armor_stand");
        registry.registerInPlace(417, IRON_HORSE_ARMOR, "iron_horse_armor");
        registry.registerInPlace(418, GOLDEN_HORSE_ARMOR, "golden_horse_armor");
        registry.registerInPlace(419, DIAMOND_HORSE_ARMOR, "diamond_horse_armor");
        registry.registerInPlace(420, LEAD, "lead");
        registry.registerInPlace(421, NAME_TAG, "name_tag");
        registry.registerInPlace(422, COMMAND_BLOCK_MINECART, "command_block_minecart");
        registry.registerInPlace(423, MUTTON, "mutton");
        registry.registerInPlace(424, COOKED_MUTTON, "cooked_mutton");
        registry.registerInPlace(425, BLACK_BANNER, "banner");
        registry.registerInPlace(426, END_CRYSTAL, "end_crystal");
        registry.registerInPlace(427, SPRUCE_DOOR, "spruce_door");
        registry.registerInPlace(428, BIRCH_DOOR, "birch_door");
        registry.registerInPlace(429, JUNGLE_DOOR, "jungle_door");
        registry.registerInPlace(430, ACACIA_DOOR, "acacia_door");
        registry.registerInPlace(431, DARK_OAK_DOOR, "dark_oak_door");
        registry.registerInPlace(432, CHORUS_FRUIT, "chorus_fruit");
        registry.registerInPlace(433, POPPED_CHORUS_FRUIT, "chorus_fruit_popped");
        registry.registerInPlace(434, BEETROOT, "beetroot");
        registry.registerInPlace(435, BEETROOT_SEEDS, "beetroot_seeds");
        registry.registerInPlace(436, BEETROOT_SOUP, "beetroot_soup");
        registry.registerInPlace(437, DRAGON_BREATH, "dragon_breath");
        registry.registerInPlace(438, SPLASH_POTION, "splash_potion");
        registry.registerInPlace(439, SPECTRAL_ARROW, "spectral_arrow");
        registry.registerInPlace(440, TIPPED_ARROW, "tipped_arrow");
        registry.registerInPlace(441, LINGERING_POTION, "lingering_potion");
        registry.registerInPlace(442, SHIELD, "shield");
        registry.registerInPlace(443, ELYTRA, "elytra");
        registry.registerInPlace(444, SPRUCE_BOAT, "spruce_boat");
        registry.registerInPlace(445, BIRCH_BOAT, "birch_boat");
        registry.registerInPlace(446, JUNGLE_BOAT, "jungle_boat");
        registry.registerInPlace(447, ACACIA_BOAT, "acacia_boat");
        registry.registerInPlace(448, DARK_OAK_BOAT, "dark_oak_boat");
        registry.registerInPlace(449, TOTEM_OF_UNDYING, "totem_of_undying");
        registry.registerInPlace(450, SHULKER_SHELL, "shulker_shell");
        registry.registerInPlace(452, IRON_NUGGET, "iron_nugget");
        registry.registerInPlace(453, KNOWLEDGE_BOOK, "knowledge_book");
        registry.registerInPlace(2256, MUSIC_DISC_13, "record_13");
        registry.registerInPlace(2257, MUSIC_DISC_CAT, "record_cat");
        registry.registerInPlace(2258, MUSIC_DISC_BLOCKS, "record_blocks");
        registry.registerInPlace(2259, MUSIC_DISC_CHIRP, "record_chirp");
        registry.registerInPlace(2260, MUSIC_DISC_FAR, "record_far");
        registry.registerInPlace(2261, MUSIC_DISC_MALL, "record_mall");
        registry.registerInPlace(2262, MUSIC_DISC_MELLOHI, "record_mellohi");
        registry.registerInPlace(2263, MUSIC_DISC_STAL, "record_stal");
        registry.registerInPlace(2264, MUSIC_DISC_STRAD, "record_strad");
        registry.registerInPlace(2265, MUSIC_DISC_WARD, "record_ward");
        registry.registerInPlace(2266, MUSIC_DISC_11, "record_11");
        registry.registerInPlace(2267, MUSIC_DISC_WAIT, "record_wait");

        registerAliases(registry);

        registry.enableSideEffects();
    }

}
