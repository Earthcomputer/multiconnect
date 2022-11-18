package net.earthcomputer.multiconnect.impl.via;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.minecraft.client.Minecraft;

public class ViaMulticonnectTranslator implements IMulticonnectTranslator {
    @Override
    public boolean isApplicableInEnvironment(IMulticonnectTranslatorApi api) {
        return !api.isModLoaded("viafabric");
    }

    @Override
    public void init(IMulticonnectTranslatorApi api) {
        var manager = ViaManagerImpl.builder()
            .injector(new MulticonnectInjector())
            .loader(new MulticonnectLoader(api))
            .platform(new MulticonnectPlatform(api))
            .build();
        Via.init(manager);
        manager.init();
    }

    @Override
    public void inject(Channel channel) {
        // Singleplayer doesnt include encoding
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }

        UserConnection info = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(info);
        channel.pipeline()
            .addBefore("encoder", "multiconnect_serverbound_translator", new MulticonnectServerboundTranslator(info))
            .addBefore("decoder", "multiconnect_clientbound_translator", new MulticonnectClientboundTranslator(info));
    }

    @Override
    public void postPipelineModifiers(Channel channel) {
        // reinstall transformers to make sure they're in the right order compared to the decoders
        if (channel.pipeline().context("multiconnect_serverbound_translator") != null) {
            var translator = channel.pipeline().remove("multiconnect_serverbound_translator");
            channel.pipeline().addBefore("encoder", "multiconnect_serverbound_translator", translator);
        }
        if (channel.pipeline().context("multiconnect_clientbound_translator") != null) {
            var translator = channel.pipeline().remove("multiconnect_clientbound_translator");
            channel.pipeline().addBefore("decoder", "multiconnect_clientbound_translator", translator);
        }
    }
}
