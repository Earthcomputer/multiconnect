package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.Queues;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.IMinecraftClient;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.search.SearchManager;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> implements IMinecraftClient {

    public MixinMinecraftClient(String name) {
        super(name);
    }

    @Unique private Queue<Runnable> runAnywayTasks = Queues.newConcurrentLinkedQueue();

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        ConnectionInfo.ip = null;
        ConnectionInfo.port = -1;
        ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
        ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
        ConnectionInfo.protocol.setup(false);
    }

    @ModifyVariable(method = "render", ordinal = 0, at = @At("HEAD"))
    private boolean shouldTick(boolean oldShouldTick) {
        if (ConnectionInfo.reloadingResources && ConnectionInfo.protocol != ProtocolRegistry.latest()) {
            return false;
        }
        return oldShouldTick;
    }

    @ModifyArg(method = "reloadResources", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManager;beginMonitoredReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReloadMonitor;"))
    private Executor fixApplyExecutor(Executor oldExecutor) {
        assert oldExecutor == this;
        return task -> {
            if (shouldExecuteAsync()) {
                runAnywayTasks.add(createTask(task));
                LockSupport.unpark(getThread());
            } else {
                task.run();
            }
        };
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/DisableableProfiler;push(Ljava/lang/String;)V", args = "ldc=tick"))
    private void executeRunAnywayTasks(boolean tick, CallbackInfo ci) {
        while (true) {
            Runnable task = runAnywayTasks.peek();
            if (task == null || !canExecute(task))
                break;
            executeTask(runAnywayTasks.remove());
        }
    }

    @Invoker
    @Override
    public abstract void callInitializeSearchableContainers();

    @Accessor
    @Override
    public abstract SearchManager getSearchManager();
}
