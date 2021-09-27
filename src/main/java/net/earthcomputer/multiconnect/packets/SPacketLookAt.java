package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

import java.util.Optional;

@Message
public class SPacketLookAt {
    public EntityAnchor anchor;
    public double targetX;
    public double targetY;
    public double targetZ;
    public Optional<EntityInfo> entityInfo;

    @NetworkEnum
    public enum EntityAnchor {
        FEET, EYES
    }

    @Message
    public static class EntityInfo {
        public int targetId;
        public EntityAnchor targetAnchor;
    }
}
