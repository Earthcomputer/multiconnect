package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({
        ChunkDataS2CPacket.class,
        CustomPayloadS2CPacket.class,
        MobSpawnS2CPacket.class,
        PlayerSpawnS2CPacket.class,
        StatisticsS2CPacket.class,
        GameJoinS2CPacket.class,
        PlayerRespawnS2CPacket.class
})
public class MixinUserDataHolders implements IUserDataHolder {
    @Unique private final TypedMap userData = new TypedMap();

    @Override
    public TypedMap multiconnect_getUserData() {
        return userData;
    }
}
