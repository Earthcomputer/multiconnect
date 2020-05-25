package net.earthcomputer.multiconnect.protocols.v1_11_2;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class Achievements_1_11_2 {

    private static int minX = 0, minY = 0;
    public static final Map<String, Advancement> ACHIEVEMENTS = new LinkedHashMap<>();
    public static final Advancement OPEN_INVENTORY = create("openInventory", 0, 0, Items.BOOK, null);
    public static final Advancement MINE_WOOD = create("mineWood", 2, 1, Blocks.OAK_LOG, OPEN_INVENTORY);
    public static final Advancement BUILD_WORK_BENCH = create("buildWorkBench", 4, -1, Blocks.CRAFTING_TABLE, MINE_WOOD);
    public static final Advancement BUILD_PICKAXE = create("buildPickaxe", 4, 2, Items.WOODEN_PICKAXE, BUILD_WORK_BENCH);
    public static final Advancement BUILD_FURNACE = create("buildFurnace", 3, 4, Blocks.FURNACE, BUILD_PICKAXE);
    public static final Advancement ACQUIRE_IRON = create("acquireIron", 1, 4, Items.IRON_INGOT, BUILD_FURNACE);
    public static final Advancement BUILD_HOE = create("buildHoe", 2, -3, Items.WOODEN_HOE, BUILD_WORK_BENCH);
    public static final Advancement MAKE_BREAD = create("makeBread", -1, -3, Items.BREAD, BUILD_HOE);
    public static final Advancement BAKE_CAKE = create("bakeCake", 0, -5, Items.CAKE, BUILD_HOE);
    public static final Advancement BUILD_BETTER_PICKAXE = create("buildBetterPickaxe", 6, 2, Items.STONE_PICKAXE, BUILD_PICKAXE);
    public static final Advancement COOK_FISH = create("cookFish", 2, 6, Items.COOKED_COD, BUILD_FURNACE);
    public static final Advancement ON_A_RAIL = create("onARail", 2, 3, Blocks.RAIL, ACQUIRE_IRON);
    public static final Advancement BUILD_SWORD = create("buildSword", 6, -1, Items.WOODEN_SWORD, BUILD_WORK_BENCH);
    public static final Advancement KILL_ENEMY = create("killEnemy", 8, -1, Items.BONE, BUILD_SWORD);
    public static final Advancement KILL_COW = create("killCow", 7, -3, Items.LEATHER, BUILD_SWORD);
    public static final Advancement FLY_PIG = create("flyPig", 9, -3, Items.SADDLE, KILL_COW, true);
    public static final Advancement SNIPE_SKELETON = create("snipeSkeleton", 7, 0, Items.BOW, KILL_ENEMY, true);
    public static final Advancement DIAMONDS = create("diamonds", -1, 5, Blocks.DIAMOND_ORE, ACQUIRE_IRON);
    public static final Advancement DIAMONDS_TO_YOU = create("diamondsToYou", -1, 2, Items.DIAMOND, DIAMONDS);
    public static final Advancement PORTAL = create("portal", -1, 7, Blocks.OBSIDIAN, DIAMONDS);
    public static final Advancement GHAST = create("ghast", -4, 8, Items.GHAST_TEAR, PORTAL, true);
    public static final Advancement BLAZE_ROD = create("blazeRod", 0, 9, Items.BLAZE_ROD, PORTAL);
    public static final Advancement POTION = create("potion", 2, 8, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION), BLAZE_ROD, false);
    public static final Advancement THE_END = create("theEnd", 3, 10, Items.ENDER_EYE, BLAZE_ROD, true);
    public static final Advancement THE_END2 = create("theEnd2", 4, 13, Blocks.DRAGON_EGG, THE_END, true);
    public static final Advancement ENCHANTMENTS = create("enchantments", -4, 4, Blocks.ENCHANTING_TABLE, DIAMONDS);
    public static final Advancement OVERKILL = create("overkill", -4, 1, Items.DIAMOND_SWORD, ENCHANTMENTS, true);
    public static final Advancement BOOKCASE = create("bookcase", -3, 6, Blocks.BOOKSHELF, ENCHANTMENTS);
    public static final Advancement BREED_COW = create("breedCow", 7, -5, Items.WHEAT, KILL_COW);
    public static final Advancement SPAWN_WITHER = create("spawnWither", 7, 12, Items.WITHER_SKELETON_SKULL, THE_END2);
    public static final Advancement KILL_WITHER = create("killWither", 7, 10, Items.NETHER_STAR, SPAWN_WITHER);
    public static final Advancement FULL_BEACON = create("fullBeacon", 7, 8, Blocks.BEACON, KILL_WITHER, true);
    public static final Advancement EXPLORE_ALL_BIOMES = create("exploreAllBiomes", 4, 8, Items.DIAMOND_BOOTS, THE_END, true);
    public static final Advancement OVERPOWERED = create("overpowered", 6, 4, Items.ENCHANTED_GOLDEN_APPLE, BUILD_BETTER_PICKAXE, true);
    static {
        for (Advancement advancement : ACHIEVEMENTS.values()) {
            assert advancement.getDisplay() != null;
            advancement.getDisplay().setPosition(advancement.getDisplay().getX() - minX, advancement.getDisplay().getY() - minY);
        }
    }

    private static Advancement create(String name, int x, int y, ItemConvertible icon, Advancement parent) {
        return create(name, x, y, icon, parent, false);
    }

    private static Advancement create(String name, int x, int y, ItemConvertible icon, Advancement parent, boolean special) {
        return create(name, x, y, new ItemStack(icon), parent, special);
    }

    private static Advancement create(String name, int x, int y, ItemStack icon, Advancement parent, boolean special) {
        if (x < minX) {
            minX = x;
        }
        if (y < minY) {
            minY = y;
        }
        Identifier id = new Identifier(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));
        AdvancementDisplay display = new AdvancementDisplay(
                icon,
                new TranslatableText("achievement." + name),
                new TranslatableText("achievement." + name + ".desc"),
                parent == null ? new Identifier("textures/gui/advancements/backgrounds/stone.png") : null,
                special ? AdvancementFrame.CHALLENGE : AdvancementFrame.TASK,
                true,
                true,
                false
        );
        display.setPosition(x, y);
        Map<String, AdvancementCriterion> criteria = ImmutableMap.of(AchievementManager.REQUIREMENT, new AdvancementCriterion(new ImpossibleCriterion.Conditions()));
        String[][] requirements = {{AchievementManager.REQUIREMENT}};
        Advancement advancement = new Advancement(id, parent, display, AdvancementRewards.NONE, criteria, requirements);
        ACHIEVEMENTS.put(name, advancement);
        return advancement;
    }

}
