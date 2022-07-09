package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketCraftFailedResponse;
import net.minecraft.resources.ResourceLocation;

@MessageVariant(minVersion = Protocols.V1_13)
public class SPacketCraftFailedResponse_Latest implements SPacketCraftFailedResponse {
    public byte syncId;
    @Introduce(compute = "computeRecipe")
    public ResourceLocation recipe;

    public static ResourceLocation computeRecipe(@Argument("recipe") int recipe) {
        return new ResourceLocation(String.valueOf(recipe));
    }
}
