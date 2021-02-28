package net.earthcomputer.multiconnect.protocols.v1_16_1;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.util.Identifier;

public class RecipeBookDataC2SPacket_1_16_1 implements Packet<ServerPlayPacketListener> {
    private final Mode mode;
    private Identifier recipeId;
    private boolean guiOpen;
    private boolean filteringCraftable;
    private boolean furnaceGuiOpen;
    private boolean furnaceFilteringCraftable;
    private boolean blastFurnaceGuiOpen;
    private boolean blastFurnaceFilteringCraftable;
    private boolean smokerGuiOpen;
    private boolean smokerGuiFilteringCraftable;

    public RecipeBookDataC2SPacket_1_16_1(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public RecipeBookDataC2SPacket_1_16_1(RecipeBookDataC2SPacket packet) {
        this.mode = Mode.SHOWN;
        this.recipeId = packet.getRecipeId();
    }

    public RecipeBookDataC2SPacket_1_16_1(RecipeCategoryOptionsC2SPacket packet) {
        this.mode = Mode.SETTINGS;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            RecipeBookOptions bookOptions = player.getRecipeBook().getOptions();
            guiOpen = isGuiOpen(packet, bookOptions, RecipeBookCategory.CRAFTING);
            filteringCraftable = isFilteringCraftable(packet, bookOptions, RecipeBookCategory.CRAFTING);
            furnaceGuiOpen = isGuiOpen(packet, bookOptions, RecipeBookCategory.FURNACE);
            furnaceFilteringCraftable = isFilteringCraftable(packet, bookOptions, RecipeBookCategory.FURNACE);
            blastFurnaceGuiOpen = isGuiOpen(packet, bookOptions, RecipeBookCategory.BLAST_FURNACE);
            blastFurnaceFilteringCraftable = isFilteringCraftable(packet, bookOptions, RecipeBookCategory.BLAST_FURNACE);
            smokerGuiOpen = isGuiOpen(packet, bookOptions, RecipeBookCategory.SMOKER);
            smokerGuiFilteringCraftable = isFilteringCraftable(packet, bookOptions, RecipeBookCategory.SMOKER);
        }
    }

    private static boolean isGuiOpen(RecipeCategoryOptionsC2SPacket packet, RecipeBookOptions bookOptions, RecipeBookCategory category) {
        if (packet.getCategory() == category) {
            return packet.isGuiOpen();
        } else {
            return bookOptions.isGuiOpen(category);
        }
    }

    private static boolean isFilteringCraftable(RecipeCategoryOptionsC2SPacket packet, RecipeBookOptions bookOptions, RecipeBookCategory category) {
        if (packet.getCategory() == category) {
            return packet.isFilteringCraftable();
        } else {
            return bookOptions.isFilteringCraftable(category);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(mode);
        if (mode == Mode.SHOWN) {
            buf.writeIdentifier(recipeId);
        } else if (mode == Mode.SETTINGS) {
            buf.writeBoolean(guiOpen);
            buf.writeBoolean(filteringCraftable);
            buf.writeBoolean(furnaceGuiOpen);
            buf.writeBoolean(furnaceFilteringCraftable);
            buf.writeBoolean(blastFurnaceGuiOpen);
            buf.writeBoolean(blastFurnaceFilteringCraftable);
            buf.writeBoolean(smokerGuiOpen);
            buf.writeBoolean(smokerGuiFilteringCraftable);
        }
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }

    public Mode getMode() {
        return mode;
    }

    public Identifier getRecipeId() {
        return recipeId;
    }

    public boolean isGuiOpen() {
        return guiOpen;
    }

    public boolean isFilteringCraftable() {
        return filteringCraftable;
    }

    public boolean isFurnaceGuiOpen() {
        return furnaceGuiOpen;
    }

    public boolean isFurnaceFilteringCraftable() {
        return furnaceFilteringCraftable;
    }

    public boolean isBlastFurnaceGuiOpen() {
        return blastFurnaceGuiOpen;
    }

    public boolean isBlastFurnaceFilteringCraftable() {
        return blastFurnaceFilteringCraftable;
    }

    public boolean isSmokerGuiOpen() {
        return smokerGuiOpen;
    }

    public boolean isSmokerGuiFilteringCraftable() {
        return smokerGuiFilteringCraftable;
    }

    public enum Mode {
        SHOWN, SETTINGS
    }
}
