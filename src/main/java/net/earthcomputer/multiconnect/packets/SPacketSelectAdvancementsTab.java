package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;

@MessageVariant
public class SPacketSelectAdvancementsTab {
    public Optional<ResourceLocation> tabId;
}
