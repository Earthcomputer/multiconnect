package net.earthcomputer.multiconnect.packets.v1_16_1;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUnlockRecipes;
import net.earthcomputer.multiconnect.packets.latest.SPacketUnlockRecipes_Latest;
import net.minecraft.util.Identifier;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_16_1)
@Polymorphic
public abstract class SPacketUnlockRecipes_1_16_1 implements SPacketUnlockRecipes {
    public SPacketUnlockRecipes_Latest.Mode mode;
    public boolean craftingBookOpen;
    public boolean craftingBookFilterActive;
    public boolean smeltingBookOpen;
    public boolean smeltingBookFilterActive;
    public List<Identifier> recipeIdsToChange;

    @Polymorphic(stringValue = "INIT")
    @MessageVariant(maxVersion = Protocols.V1_16_1)
    public static class Init extends SPacketUnlockRecipes_1_16_1 implements SPacketUnlockRecipes.Init {
        public List<Identifier> recipeIdsToInit;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_16_1)
    public static class Other extends SPacketUnlockRecipes_1_16_1 implements SPacketUnlockRecipes.Other {
    }
}
