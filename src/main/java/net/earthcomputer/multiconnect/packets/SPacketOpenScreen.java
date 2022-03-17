package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@MessageVariant
public class SPacketOpenScreen {
    public int syncId;
    @Registry(Registries.SCREEN_HANDLER)
    public int screenHandlerType;
    public CommonTypes.Text title;
}
