package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_1.CPacketRecipeBookSeenRecipe_1_16_1;

@MessageVariant(minVersion = Protocols.V1_16_2)
public class CPacketRecipeBookChangeSettings {
    public Category category;
    public boolean bookOpen;
    public boolean filterActive;

    @Handler(protocol = Protocols.V1_16_1)
    public static CPacketRecipeBookSeenRecipe_1_16_1 handle(
            @DefaultConstruct CPacketRecipeBookSeenRecipe_1_16_1.Settings packet
    ) {
        return packet;
    }

    @NetworkEnum
    public enum Category {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
