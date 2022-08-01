package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PartialHandler;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.protocols.generic.CurrentMenuReference;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@MessageVariant
public class SPacketContainerClose {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;

    @PartialHandler
    public static void saveOpenMenu(
            @Argument("syncId") int syncId,
            @GlobalData @Nullable CurrentMenuReference currentMenu,
            @GlobalData Consumer<CurrentMenuReference> currentMenuSetter
    ) {
        if (currentMenu != null && currentMenu.syncId() == syncId) {
            currentMenuSetter.accept(null);
        }
    }
}
