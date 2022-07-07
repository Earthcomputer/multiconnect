package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.protocols.generic.Key;

@Message
public interface ChunkData {
    @Message
    interface Section {
    }

    @Message
    interface BlockStatePalettedContainer {
        Key<Boolean> HAS_EXPANDED_REGISTRY_PALETTE = Key.create("hasExpandedRegistryPalette", false);
    }
}
