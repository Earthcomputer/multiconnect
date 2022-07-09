package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketContainerSetContent;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17_1)
public class SPacketContainerSetContent_Latest implements SPacketContainerSetContent {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    @Introduce(intValue = 0)
    public int revision;
    public List<CommonTypes.ItemStack> slots;
    @Introduce(defaultConstruct = true)
    public CommonTypes.ItemStack cursorStack;
}
