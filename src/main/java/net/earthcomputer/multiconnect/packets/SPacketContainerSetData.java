package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.CustomFix;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.protocols.generic.CurrentMenuReference;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;

@MessageVariant
public class SPacketContainerSetData {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short property;
    @CustomFix("fixValue")
    public short value;

    public static short fixValue(
        short value,
        @Argument("syncId") int syncId,
        @Argument("property") short property,
        @GlobalData @Nullable CurrentMenuReference currentMenu,
        @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MENU, value = "enchantment")) int enchantmentMenuId
    ) {
        if (currentMenu != null
            && currentMenu.syncId() == syncId
            && currentMenu.menuType() == enchantmentMenuId
            && property >= 4
            && property <= 6
        ) {
            return (short) PacketSystem.serverRawIdToClient(Registry.ENCHANTMENT, value);
        }

        return value;
    }
}
