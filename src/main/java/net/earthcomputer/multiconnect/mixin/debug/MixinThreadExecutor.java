package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ThreadExecutor.class)
public abstract class MixinThreadExecutor {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "executeTask", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void onTaskError(Runnable task, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance()) {
            return;
        }
        if (task.getClass().getNestHost() != NetworkThreadUtils.class) {
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
