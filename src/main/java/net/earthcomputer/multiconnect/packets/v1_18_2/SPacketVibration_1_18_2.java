package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.SPacketParticle_Latest;

@MessageVariant(minVersion = Protocols.V1_17, maxVersion = Protocols.V1_18_2)
public class SPacketVibration_1_18_2 {
    public CommonTypes.BlockPos pos;
    public CommonTypes.PositionSource positionSource;
    public int arrivalTicks;

    @Handler
    public static SPacketParticle_Latest handle(
            @Argument("pos") CommonTypes.BlockPos pos,
            @Argument("positionSource") CommonTypes.PositionSource positionSource,
            @Argument("arrivalTicks") int arrivalTicks,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PARTICLE_TYPE, value = "vibration")) int vibrationId
    ) {
        var packet = new SPacketParticle_Latest();
        packet.particleId = vibrationId;
        packet.count = 1;
        packet.longDistance = true;
        var mcPos = pos.toMinecraft();
        packet.x = mcPos.getX() + 0.5;
        packet.y = mcPos.getY() + 0.5;
        packet.z = mcPos.getZ() + 0.5;
        var particle = new CommonTypes.Particle_Latest.Vibration();
        particle.particleId = vibrationId;
        var path = new CommonTypes.VibrationPath_Latest();
        path.source = positionSource;
        path.ticks = arrivalTicks;
        particle.path = path;
        packet.particle = particle;
        return packet;
    }
}
