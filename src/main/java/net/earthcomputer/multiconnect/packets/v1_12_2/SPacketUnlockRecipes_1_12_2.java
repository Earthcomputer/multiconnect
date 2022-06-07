package net.earthcomputer.multiconnect.packets.v1_12_2;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUnlockRecipes;
import net.earthcomputer.multiconnect.packets.latest.SPacketUnlockRecipes_Latest;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class SPacketUnlockRecipes_1_12_2 implements SPacketUnlockRecipes {
    public SPacketUnlockRecipes_Latest.Mode mode;
    public boolean craftingBookOpen;
    public boolean craftingBookFilterActive;
    public IntList recipeIdsToChange;

    @Polymorphic(stringValue = "INIT")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Init extends SPacketUnlockRecipes_1_12_2 implements SPacketUnlockRecipes.Init {
        public IntList recipeIdsToInit;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Other extends SPacketUnlockRecipes_1_12_2 implements SPacketUnlockRecipes.Other {
    }
}
