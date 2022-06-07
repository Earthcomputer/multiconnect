package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketExperienceBarUpdate {
    public float experienceBar;
    public int level;
    public int totalExperience;
}
