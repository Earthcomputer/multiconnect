package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {
    @Unique private Button protocolSelector;

    protected MixinMultiplayerScreen(ITextComponent title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void createButtons(CallbackInfo ci) {
        protocolSelector = new Button(width - 80, 5, 70, 20, ConnectionInfo.globalForcedProtocolVersion.getName(), (buttonWidget_1) ->
                ConnectionInfo.globalForcedProtocolVersion = ConnectionInfo.globalForcedProtocolVersion.next()
        );

        addButton(protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format("multiconnect.changeForcedProtocol") + " ->", width - 85 - Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format("multiconnect.changeForcedProtocol") + " ->"), 11, 0xFFFFFF);
        protocolSelector.setMessage(ConnectionInfo.globalForcedProtocolVersion.getName());
    }
}
