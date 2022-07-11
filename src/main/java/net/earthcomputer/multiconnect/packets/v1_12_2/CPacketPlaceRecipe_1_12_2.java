package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlaceRecipe;
import net.minecraft.resources.ResourceLocation;

@MessageVariant(minVersion = Protocols.V1_12_1, maxVersion = Protocols.V1_12_2)
public class CPacketPlaceRecipe_1_12_2 implements CPacketPlaceRecipe {
    public byte syncId;
    @Introduce(compute = "computeRecipeId")
    public int recipeId;
    public boolean craftAll;

    public static int computeRecipeId(@Argument("recipeId") ResourceLocation recipeId) {
        try {
            return Integer.parseInt(recipeId.getPath());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Handler(protocol = Protocols.V1_12)
    public static void drop() {
    }
}
