package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;

import java.util.Optional;

@Message(minVersion = Protocols.V1_17)
public class SPacketResourcePackSend {
    public String url;
    public String hash;
    @Introduce(booleanValue = false)
    public boolean forced;
    @Introduce(defaultConstruct = true)
    public Optional<CommonTypes.Text> promptMessage;
}
