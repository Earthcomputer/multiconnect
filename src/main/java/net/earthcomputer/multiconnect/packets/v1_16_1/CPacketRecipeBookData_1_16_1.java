package net.earthcomputer.multiconnect.packets.v1_16_1;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.minecraft.util.Identifier;

@MessageVariant
@Polymorphic
public abstract class CPacketRecipeBookData_1_16_1 {
    public Mode mode;

    @MessageVariant
    @Polymorphic(stringValue = "SHOWN")
    public static class Shown extends CPacketRecipeBookData_1_16_1 {
        public Identifier recipeId;
    }

    @MessageVariant
    @Polymorphic(stringValue = "SETTINGS")
    public static class Settings extends CPacketRecipeBookData_1_16_1 {
        public boolean guiOpen;
        public boolean filteringCraftable;
        public boolean furnaceGuiOpen;
        public boolean furnaceFilteringCraftable;
        public boolean blastFurnaceGuiOpen;
        public boolean blastFurnaceFilteringCraftable;
        public boolean smokerGuiOpen;
        public boolean smokerFilteringCraftable;
    }

    @NetworkEnum
    public enum Mode {
        SHOWN, SETTINGS
    }
}
