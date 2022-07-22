package net.earthcomputer.multiconnect.packets.v1_19;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketServerData;

import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_19, maxVersion = Protocols.V1_19)
public class SPacketServerData_1_19 implements SPacketServerData {
    public Optional<CommonTypes.Text> description;
    public Optional<String> favicon;
    public boolean previewsChat;
}
