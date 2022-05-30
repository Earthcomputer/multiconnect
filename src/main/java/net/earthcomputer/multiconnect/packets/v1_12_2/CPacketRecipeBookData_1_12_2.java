package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRecipeBookData;
import net.earthcomputer.multiconnect.packets.v1_16_1.CPacketRecipeBookData_1_16_1;
import net.minecraft.util.Identifier;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class CPacketRecipeBookData_1_12_2 implements CPacketRecipeBookData {
    public CPacketRecipeBookData_1_16_1.Mode mode;

    @MessageVariant(maxVersion = Protocols.V1_12_2)
    @Polymorphic(stringValue = "SHOWN")
    public static class Shown extends CPacketRecipeBookData_1_12_2 implements CPacketRecipeBookData.Shown {
        @Type(Types.INT)
        @Introduce(compute = "computeRecipeId")
        public int recipeId;

        public static int computeRecipeId(@Argument("recipeId") Identifier recipeId) {
            try {
                return Integer.parseInt(recipeId.getPath());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    @MessageVariant(maxVersion = Protocols.V1_12_2)
    @Polymorphic(stringValue = "SETTINGS")
    public static class Settings extends CPacketRecipeBookData_1_12_2 implements CPacketRecipeBookData.Settings {
        public boolean guiOpen;
        public boolean filteringCraftable;
    }

    @NetworkEnum
    public enum Mode {
        SHOWN, SETTINGS
    }
}
