package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRecipeBookSeenRecipe;
import net.earthcomputer.multiconnect.packets.v1_16_1.CPacketRecipeBookSeenRecipe_1_16_1;
import net.minecraft.resources.ResourceLocation;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public abstract class CPacketRecipeBookSeenRecipe_1_13_2 implements CPacketRecipeBookSeenRecipe {
    public CPacketRecipeBookSeenRecipe_1_16_1.Mode mode;

    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    @Polymorphic(stringValue = "SHOWN")
    public static class Shown extends CPacketRecipeBookSeenRecipe_1_13_2 implements CPacketRecipeBookSeenRecipe.Shown {
        public ResourceLocation recipeId;
    }

    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    @Polymorphic(stringValue = "SETTINGS")
    public static class Settings extends CPacketRecipeBookSeenRecipe_1_13_2 implements CPacketRecipeBookSeenRecipe.Settings {
        public boolean guiOpen;
        public boolean filteringCraftable;
        public boolean furnaceGuiOpen;
        public boolean furnaceFilteringCraftable;
    }

    @NetworkEnum
    public enum Mode {
        SHOWN, SETTINGS
    }
}
