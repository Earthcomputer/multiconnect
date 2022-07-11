package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRecipeBookSeenRecipe;
import net.earthcomputer.multiconnect.packets.v1_16_1.CPacketRecipeBookSeenRecipe_1_16_1;
import net.minecraft.resources.ResourceLocation;

@MessageVariant(minVersion = Protocols.V1_16_2)
public class CPacketRecipeBookSeenRecipe_Latest implements CPacketRecipeBookSeenRecipe {
    public ResourceLocation displayedRecipe;

    @Handler(protocol = Protocols.V1_16_1)
    public static CPacketRecipeBookSeenRecipe_1_16_1 handle(
            @Argument("displayedRecipe") ResourceLocation displayedRecipe,
            @DefaultConstruct CPacketRecipeBookSeenRecipe_1_16_1.Shown packet
    ) {
        packet.recipeId = displayedRecipe;
        return packet;
    }
}
