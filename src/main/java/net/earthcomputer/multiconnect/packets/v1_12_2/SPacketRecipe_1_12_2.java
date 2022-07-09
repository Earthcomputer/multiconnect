package net.earthcomputer.multiconnect.packets.v1_12_2;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRecipe;
import net.earthcomputer.multiconnect.packets.latest.SPacketRecipe_Latest;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class SPacketRecipe_1_12_2 implements SPacketRecipe {
    public SPacketRecipe_Latest.Mode mode;
    public boolean craftingBookOpen;
    public boolean craftingBookFilterActive;
    public IntList recipeIdsToChange;

    @Polymorphic(stringValue = "INIT")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Init extends SPacketRecipe_1_12_2 implements SPacketRecipe.Init {
        public IntList recipeIdsToInit;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Other extends SPacketRecipe_1_12_2 implements SPacketRecipe.Other {
    }
}
