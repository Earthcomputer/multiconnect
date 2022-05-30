package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketCraftRequest;
import net.minecraft.util.Identifier;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class CPacketCraftRequest_1_12_2 implements CPacketCraftRequest {
    public byte syncId;
    @Introduce(compute = "computeRecipeId")
    public int recipeId;
    public boolean craftAll;

    public static int computeRecipeId(@Argument("recipeId") Identifier recipeId) {
        try {
            return Integer.parseInt(recipeId.getPath());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
