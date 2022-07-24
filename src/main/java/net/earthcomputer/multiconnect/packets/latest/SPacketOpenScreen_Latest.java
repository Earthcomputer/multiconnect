package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PartialHandler;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketOpenScreen;
import net.earthcomputer.multiconnect.protocols.generic.CurrentMenuReference;

import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_14)
public class SPacketOpenScreen_Latest implements SPacketOpenScreen {
    public int syncId;
    @Registry(Registries.MENU)
    public int menuType;
    public CommonTypes.Text title;

    @PartialHandler
    public static void saveOpenMenu(
        @Argument("syncId") int syncId,
        @Argument("menuType") int menuType,
        @GlobalData Consumer<CurrentMenuReference> currentMenuSetter
    ) {
        currentMenuSetter.accept(new CurrentMenuReference(syncId, menuType));
    }
}
