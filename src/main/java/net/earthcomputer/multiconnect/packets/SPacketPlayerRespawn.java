package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@Message
public class SPacketPlayerRespawn {
    @Datafix(DatafixTypes.DIMENSION)
    public NbtCompound dimension;
    public Identifier dimensionId;
    @Type(Types.LONG)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.UNSIGNED_BYTE)
    public int previousGamemode;
    public boolean isDebug;
    public boolean isFlat;
    public boolean copyMetadata;
}
