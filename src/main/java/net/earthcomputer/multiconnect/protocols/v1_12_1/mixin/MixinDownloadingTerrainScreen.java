package net.earthcomputer.multiconnect.protocols.v1_12_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen extends Screen {

    @Unique private int tickCounter;

    protected MixinDownloadingTerrainScreen(Text title) {
        super(title);
    }

    @Override
    public void tick() {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_1) {
            tickCounter++;

            if (tickCounter % 20 == 0) {
                //noinspection ConstantConditions
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(new KeepAliveC2SPacket(0));
            }
        }
    }
}
