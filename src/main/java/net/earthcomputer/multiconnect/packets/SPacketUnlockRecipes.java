package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.minecraft.util.Identifier;

import java.util.List;

@MessageVariant
@Polymorphic
public abstract class SPacketUnlockRecipes {
    public Mode mode;
    public boolean craftingBookOpen;
    public boolean craftingBookFilterActive;
    public boolean smeltingBookOpen;
    public boolean smeltingBookFilterActive;
    public boolean blastFurnaceBookOpen;
    public boolean blastFurnaceBookFilterActive;
    public boolean smokerBookOpen;
    public boolean smokerBookFilterActive;
    public List<Identifier> recipeIdsToChange;

    @Polymorphic(stringValue = "INIT")
    @MessageVariant
    public static class Init extends SPacketUnlockRecipes {
        public List<Identifier> recipeIdsToInit;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class Other extends SPacketUnlockRecipes {
    }

    @NetworkEnum
    public enum Mode {
        INIT, ADD, REMOVE
    }
}
