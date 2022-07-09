package net.earthcomputer.multiconnect.packets.v1_16_1;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUnlockRecipes;
import net.earthcomputer.multiconnect.packets.latest.SPacketUnlockRecipes_Latest;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_16_1)
@Polymorphic
public abstract class SPacketUnlockRecipes_1_16_1 implements SPacketUnlockRecipes {
    public SPacketUnlockRecipes_Latest.Mode mode;
    public boolean craftingBookOpen;
    public boolean craftingBookFilterActive;
    @Introduce(booleanValue = false)
    public boolean smeltingBookOpen;
    @Introduce(booleanValue = false)
    public boolean smeltingBookFilterActive;
    @Introduce(compute = "computeRecipeIds")
    public List<ResourceLocation> recipeIdsToChange;

    public static List<ResourceLocation> computeRecipeIds(@Argument("recipeIdsToChange") IntList recipeIds) {
        List<ResourceLocation> result = new ArrayList<>(recipeIds.size());
        for (int i = 0; i < recipeIds.size(); i++) {
            result.add(new ResourceLocation(String.valueOf(recipeIds.getInt(i))));
        }
        return result;
    }

    @Polymorphic(stringValue = "INIT")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_16_1)
    public static class Init extends SPacketUnlockRecipes_1_16_1 implements SPacketUnlockRecipes.Init {
        @Introduce(compute = "computeRecipeIdsToInit")
        public List<ResourceLocation> recipeIdsToInit;

        public static List<ResourceLocation> computeRecipeIdsToInit(@Argument("recipeIdsToInit") IntList recipeIds) {
            return computeRecipeIds(recipeIds);
        }
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_16_1)
    public static class Other extends SPacketUnlockRecipes_1_16_1 implements SPacketUnlockRecipes.Other {
    }
}
