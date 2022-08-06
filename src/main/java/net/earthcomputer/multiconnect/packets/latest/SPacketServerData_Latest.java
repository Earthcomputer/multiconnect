package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketServerData;

import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_19_2)
public class SPacketServerData_Latest implements SPacketServerData {
    public Optional<CommonTypes.Text> description;
    public Optional<String> favicon;
    public boolean previewsChat;
    @Introduce(booleanValue = false)
    public boolean enforcesSecureChat;
}
