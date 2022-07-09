package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(BlockableEventLoop.class)
public abstract class BlockableEventLoopMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "doRunTask", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void onTaskError(Runnable task, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance()) {
            return;
        }
        if (task.getClass().getNestHost() != PacketUtils.class) {
            return;
        }
        Field field = null;
        for (Field f : task.getClass().getDeclaredFields()) {
            if (f.getType() == Packet.class) {
                field = f;
                break;
            }
        }
        if (field == null) {
            return;
        }
        field.setAccessible(true);
        Packet<?> packet;
        try {
            packet = (Packet<?>) field.get(task);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        if (!DebugUtils.STORE_BUFS_FOR_HANDLER) {
            LOGGER.error("Note: to get a more complete error, run with JVM argument -Dmulticonnect.storeBufsForHandler=true");
            return;
        }
        byte[] buf = PacketSystem.getUserData(packet).get(DebugUtils.STORED_BUF);
        if (buf == null) {
            return;
        }
        DebugUtils.logPacketError(buf);
    }
}
