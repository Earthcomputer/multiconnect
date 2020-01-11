package net.earthcomputer.multiconnect.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.earthcomputer.multiconnect.api.EnumProtocol;
import net.earthcomputer.multiconnect.impl.IServerInfo;
import net.earthcomputer.multiconnect.impl.ServersExt;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Mixin(ServerInfo.class)
public class MixinServerInfo implements IServerInfo {

    @Shadow public String address;

    @Unique private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Unique private static final File serversExtFile = new File(FabricLoader.getInstance().getConfigDirectory(), "multiconnect/servers_ext.json");
    @Unique private static ServersExt serversExt;
    @Unique private EnumProtocol forcedVersion = EnumProtocol.AUTO;

    @Override
    public EnumProtocol multiconnect_getForcedVersion() {
        return forcedVersion;
    }

    @Override
    public void multiconnect_setForcedVersion(EnumProtocol forcedVersion) {
        this.forcedVersion = forcedVersion;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(String name, String address, boolean local, CallbackInfo ci) {
        IServerInfo.INSTANCES.add((ServerInfo) (Object) this);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void onCopyFrom(ServerInfo other, CallbackInfo ci) {
        this.forcedVersion = ((IServerInfo) other).multiconnect_getForcedVersion();
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private void onSerialize(CallbackInfoReturnable<CompoundTag> ci) {
        ServersExt.ServerExt serverExt = getServersExt().servers.computeIfAbsent(address, k -> new ServersExt.ServerExt());
        serverExt.forcedProtocol = forcedVersion.getValue();
        //noinspection ResultOfMethodCallIgnored
        serversExtFile.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(serversExtFile);
            GSON.toJson(serversExt, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LogManager.getLogger("multiconnect").error("Failed to write ext server file", e);
        }
    }

    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void onDeserialize(CompoundTag tag, CallbackInfoReturnable<ServerInfo> ci) {
        String address = ci.getReturnValue().address;
        ServersExt.ServerExt serverExt = getServersExt().servers.get(address);
        if (serverExt != null && ProtocolRegistry.isSupported(serverExt.forcedProtocol)) {
            ((IServerInfo) ci.getReturnValue()).multiconnect_setForcedVersion(EnumProtocol.byValue(serverExt.forcedProtocol));
        }
    }

    @Unique
    private static ServersExt getServersExt() {
        if (serversExt == null) {
            if (serversExtFile.exists()) {
                try {
                    serversExt = GSON.fromJson(new FileReader(serversExtFile), ServersExt.class);
                    if (serversExt == null)
                        serversExt = new ServersExt();
                } catch (IOException e) {
                    LogManager.getLogger("multiconnect").error("Failed to read ext server file", e);
                    serversExt = new ServersExt();
                }
            } else {
                serversExt = new ServersExt();
            }
        }
        return serversExt;
    }


}
