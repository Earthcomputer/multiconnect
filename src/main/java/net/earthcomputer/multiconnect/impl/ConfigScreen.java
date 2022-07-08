package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private static final Text TITLE = Text.translatable("multiconnect.config.title");
    private static final Text ALLOW_OLD_UNSIGNED_CHAT = Text.translatable("multiconnect.config.allowOldUnsignedChat");
    private static final Text ENABLE_DEBUGKEY = Text.translatable("multiconnect.config.enableDebugKey");
    private static final Text ENABLE_PACKET_RECORDING = Text.translatable("multiconnect.config.enablePacketRecording");
    private static final Text ALLOW_OLD_UNSIGNED_CHAT_TOOLTIP = Text.translatable("multiconnect.config.allowOldUnsignedChat.tooltip");

    private final Screen parent;

    protected ConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(CyclingButtonWidget.onOffBuilder(Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.allowOldUnsignedChat))
                .tooltip(SimpleOption.<Boolean>constantTooltip(ALLOW_OLD_UNSIGNED_CHAT_TOOLTIP).apply(client))
                .build(width / 2 - 105 , 50, 210, 20, ALLOW_OLD_UNSIGNED_CHAT, (button, value) -> MulticonnectConfig.INSTANCE.allowOldUnsignedChat = value));

        addDrawableChild(CyclingButtonWidget.onOffBuilder(Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.enablePacketRecorder))
                .build(width / 2 - 105 , 80, 210, 20, ENABLE_PACKET_RECORDING, (button, value) -> MulticonnectConfig.INSTANCE.enablePacketRecorder = value));

        addDrawableChild(CyclingButtonWidget.onOffBuilder(Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.debugKey))
                .build(width / 2 - 105 , 110, 210, 20, ENABLE_DEBUGKEY, (button, value) -> MulticonnectConfig.INSTANCE.debugKey = value));

        addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> close()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, textRenderer, TITLE.asOrderedText(), width / 2, 20, 0xffffff);
    }

    @Override
    public void close() {
        assert client != null;
        MulticonnectConfig.INSTANCE.save();
        client.setScreen(parent);
    }
}
