package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.Optional;

@MessageVariant
public class SPacketServerMetadata {
    public Optional<CommonTypes.Text> description;
    public Optional<String> favicon;
    public boolean previewsChat;
}
