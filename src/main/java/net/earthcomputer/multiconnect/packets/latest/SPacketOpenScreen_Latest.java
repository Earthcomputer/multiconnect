package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketOpenScreen;

@MessageVariant(minVersion = Protocols.V1_14)
public class SPacketOpenScreen_Latest implements SPacketOpenScreen {
    public int syncId;
    @Registry(Registries.SCREEN_HANDLER)
    public int screenHandlerType;
    public CommonTypes.Text title;
}
