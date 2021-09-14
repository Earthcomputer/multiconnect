package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketResourcePackSend_1_16_5;

import java.util.Optional;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_16_5, type = SPacketResourcePackSend_1_16_5.class))
public class SPacketResourcePackSend {
    public String url;
    public String hash;
    @Introduce(booleanValue = false)
    public boolean forced;
    @Introduce(defaultConstruct = true)
    public Optional<CommonTypes.Text> promptMessage;
}
