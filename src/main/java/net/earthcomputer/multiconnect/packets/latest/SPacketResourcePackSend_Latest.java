package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketResourcePackSend;

import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketResourcePackSend_Latest implements SPacketResourcePackSend {
    public String url;
    public String hash;
    @Introduce(booleanValue = false)
    public boolean forced;
    @Introduce(defaultConstruct = true)
    public Optional<CommonTypes.Text> promptMessage;
}
