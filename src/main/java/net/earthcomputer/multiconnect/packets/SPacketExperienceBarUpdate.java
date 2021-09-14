package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketExperienceBarUpdate {
    public float experienceBar;
    public int level;
    public int totalExperience;
}
