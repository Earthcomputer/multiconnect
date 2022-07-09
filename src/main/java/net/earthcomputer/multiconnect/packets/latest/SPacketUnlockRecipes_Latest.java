package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUnlockRecipes;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_16_2)
@Polymorphic
public abstract class SPacketUnlockRecipes_Latest implements SPacketUnlockRecipes {
    public Mode mode;
    public boolean craftingBookOpen;
    public boolean craftingBookFilterActive;
    public boolean smeltingBookOpen;
    public boolean smeltingBookFilterActive;
    // These will be fixed by an on-thread mixin, to ensure a race condition doesn't happen with recipe book access
    @Introduce(booleanValue = false)
    public boolean blastFurnaceBookOpen;
    @Introduce(booleanValue = false)
    public boolean blastFurnaceBookFilterActive;
    @Introduce(booleanValue = false)
    public boolean smokerBookOpen;
    @Introduce(booleanValue = false)
    public boolean smokerBookFilterActive;
    public List<ResourceLocation> recipeIdsToChange;

    @Polymorphic(stringValue = "INIT")
    @MessageVariant(minVersion = Protocols.V1_16_2)
    public static class Init extends SPacketUnlockRecipes_Latest implements SPacketUnlockRecipes.Init {
        public List<ResourceLocation> recipeIdsToInit;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_16_2)
    public static class Other extends SPacketUnlockRecipes_Latest implements SPacketUnlockRecipes.Other {
    }

    @NetworkEnum
    public enum Mode {
        INIT, ADD, REMOVE
    }
}
