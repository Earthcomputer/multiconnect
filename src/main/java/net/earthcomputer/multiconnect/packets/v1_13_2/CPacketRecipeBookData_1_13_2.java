package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRecipeBookData;
import net.earthcomputer.multiconnect.packets.v1_16_1.CPacketRecipeBookData_1_16_1;
import net.minecraft.resources.ResourceLocation;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public abstract class CPacketRecipeBookData_1_13_2 implements CPacketRecipeBookData {
    public CPacketRecipeBookData_1_16_1.Mode mode;

    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    @Polymorphic(stringValue = "SHOWN")
    public static class Shown extends CPacketRecipeBookData_1_13_2 implements CPacketRecipeBookData.Shown {
        public ResourceLocation recipeId;
    }

    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    @Polymorphic(stringValue = "SETTINGS")
    public static class Settings extends CPacketRecipeBookData_1_13_2 implements CPacketRecipeBookData.Settings {
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
