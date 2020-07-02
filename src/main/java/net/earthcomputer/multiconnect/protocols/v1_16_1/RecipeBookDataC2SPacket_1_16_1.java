package net.earthcomputer.multiconnect.protocols.v1_16_1;

import net.minecraft.class_5411;
import net.minecraft.class_5421;
import net.minecraft.class_5427;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.util.Identifier;

public class RecipeBookDataC2SPacket_1_16_1 implements Packet<ServerPlayPacketListener> {
    private Mode mode;
    private Identifier recipeId;
    private boolean guiOpen;
    private boolean filteringCraftable;
    private boolean furnaceGuiOpen;
    private boolean furnaceFilteringCraftable;
    private boolean blastFurnaceGuiOpen;
    private boolean blastFurnaceFilteringCraftable;
    private boolean smokerGuiOpen;
    private boolean smokerGuiFilteringCraftable;

    public RecipeBookDataC2SPacket_1_16_1() {
    }

    public RecipeBookDataC2SPacket_1_16_1(RecipeBookDataC2SPacket packet) {
        this.mode = Mode.SHOWN;
        this.recipeId = packet.getRecipeId();
    }

    public RecipeBookDataC2SPacket_1_16_1(class_5427 packet) {
        this.mode = Mode.SETTINGS;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            class_5411 bookSettings = player.getRecipeBook().method_30173();
            guiOpen = isGuiOpen(packet, bookSettings, class_5421.field_25763);
            filteringCraftable = isFilteringCraftable(packet, bookSettings, class_5421.field_25763);
            furnaceGuiOpen = isGuiOpen(packet, bookSettings, class_5421.field_25764);
            furnaceFilteringCraftable = isFilteringCraftable(packet, bookSettings, class_5421.field_25764);
            blastFurnaceGuiOpen = isGuiOpen(packet, bookSettings, class_5421.field_25765);
            blastFurnaceFilteringCraftable = isFilteringCraftable(packet, bookSettings, class_5421.field_25765);
            smokerGuiOpen = isGuiOpen(packet, bookSettings, class_5421.field_25766);
            smokerGuiFilteringCraftable = isFilteringCraftable(packet, bookSettings, class_5421.field_25766);
        }
    }

    private static boolean isGuiOpen(class_5427 packet, class_5411 bookSettings, class_5421 category) {
        if (packet.method_30305() == category) {
            return packet.method_30306();
        } else {
            return bookSettings.method_30180(category);
        }
    }

    private static boolean isFilteringCraftable(class_5427 packet, class_5411 bookSettings, class_5421 category) {
        if (packet.method_30305() == category) {
            return packet.method_30307();
        } else {
            return bookSettings.method_30187(category);
        }
    }

    @Override
    public void read(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
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
