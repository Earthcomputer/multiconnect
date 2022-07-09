package net.earthcomputer.multiconnect.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("multiconnect.config.title");
    private static final Component ALLOW_OLD_UNSIGNED_CHAT = Component.translatable("multiconnect.config.allowOldUnsignedChat");
    private static final Component ENABLE_DEBUGKEY = Component.translatable("multiconnect.config.enableDebugKey");
    private static final Component ENABLE_PACKET_RECORDING = Component.translatable("multiconnect.config.enablePacketRecording");
    private static final Component ALLOW_OLD_UNSIGNED_CHAT_TOOLTIP = Component.translatable("multiconnect.config.allowOldUnsignedChat.tooltip");

    private final Screen parent;

    protected ConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(CycleButton.onOffBuilder(Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.allowOldUnsignedChat))
                .withTooltip(OptionInstance.<Boolean>cachedConstantTooltip(ALLOW_OLD_UNSIGNED_CHAT_TOOLTIP).apply(minecraft))
                .create(width / 2 - 105 , 50, 210, 20, ALLOW_OLD_UNSIGNED_CHAT, (button, value) -> MulticonnectConfig.INSTANCE.allowOldUnsignedChat = value));

        addRenderableWidget(CycleButton.onOffBuilder(Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.enablePacketRecorder))
                .create(width / 2 - 105 , 80, 210, 20, ENABLE_PACKET_RECORDING, (button, value) -> MulticonnectConfig.INSTANCE.enablePacketRecorder = value));

        addRenderableWidget(CycleButton.onOffBuilder(Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.debugKey))
                .create(width / 2 - 105 , 110, 210, 20, ENABLE_DEBUGKEY, (button, value) -> MulticonnectConfig.INSTANCE.debugKey = value));

        addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> onClose()));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, font, TITLE.getVisualOrderText(), width / 2, 20, 0xffffff);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        MulticonnectConfig.INSTANCE.save();
        minecraft.setScreen(parent);
    }
}
