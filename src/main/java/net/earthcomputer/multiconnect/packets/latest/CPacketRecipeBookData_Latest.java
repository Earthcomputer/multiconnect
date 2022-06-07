package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRecipeBookData;
import net.earthcomputer.multiconnect.packets.v1_16_1.CPacketRecipeBookData_1_16_1;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_16_2)
public class CPacketRecipeBookData_Latest implements CPacketRecipeBookData {
    public Identifier displayedRecipe;

    @Handler(protocol = Protocols.V1_16_1)
    public static CPacketRecipeBookData_1_16_1 handle(
            @Argument("displayedRecipe") Identifier displayedRecipe,
            @DefaultConstruct CPacketRecipeBookData_1_16_1.Shown packet
    ) {
        packet.recipeId = displayedRecipe;
        return packet;
    }
}
