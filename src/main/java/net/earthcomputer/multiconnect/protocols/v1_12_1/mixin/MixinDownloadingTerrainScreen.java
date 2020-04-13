package net.earthcomputer.multiconnect.protocols.v1_12_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DownloadTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen extends Screen {

    @Unique private int tickCounter;

    protected MixinDownloadingTerrainScreen(ITextComponent title) {
        super(title);
    }

    @Override
    public void tick() {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_1) {
            tickCounter++;

            if (tickCounter % 20 == 0) {
                //noinspection ConstantConditions
                Minecraft.getInstance().getConnection().sendPacket(new CKeepAlivePacket());
            }
        }
    }
}
