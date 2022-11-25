package net.earthcomputer.multiconnect.impl.via;

import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.ints.IntLinkedOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import com.viaversion.viaversion.libs.gson.JsonObject;

public class MulticonnectInjector implements ViaInjector {
    @Override
    public void inject() {
        // happens in mixins
    }

    @Override
    public void uninject() {
        // not possible, do nothing
    }

    @Override
    public int getServerProtocolVersion() {
        return getServerProtocolVersions().firstInt();
    }

    @Override
    public IntSortedSet getServerProtocolVersions() {
        IntSortedSet versions = new IntLinkedOpenHashSet();
        for (ProtocolVersion protocol : ProtocolVersion.getProtocols()) {
            versions.add(protocol.getOriginalVersion());
        }
        return versions;
    }

    @Override
    public JsonObject getDump() {
        JsonObject dump = new JsonObject();
        dump.addProperty("multiconnect", true);
        return dump;
    }

    @Override
    public String getEncoderName() {
        return "multiconnect_serverbound_translator";
    }

    @Override
    public String getDecoderName() {
        return "multiconnect_clientbound_translator";
    }
}
