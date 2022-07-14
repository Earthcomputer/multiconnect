package net.earthcomputer.multiconnect.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("multiconnect.config.title");
    private static final Component ALLOW_OLD_UNSIGNED_CHAT_TOOLTIP = Component.translatable("multiconnect.config.allowOldUnsignedChat.tooltip");
    private static final Component ALLOW_PACKET_RECORDER_CHAT_TOOLTIP = Component.translatable("multiconnect.config.enablePacketRecording.tooltip");

    private final Screen parent;
    private static final OptionInstance<Boolean> debugKey =
            OptionInstance.createBoolean(
                    "multiconnect.config.enableDebugKey",
                    OptionInstance.noTooltip(),
                    Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.debugKey),
                    (value) -> MulticonnectConfig.INSTANCE.debugKey = value
            );

    private static final OptionInstance<Boolean> allowOldUnsignedChat =
            OptionInstance.createBoolean(
                    "multiconnect.config.allowOldUnsignedChat",
                    OptionInstance.cachedConstantTooltip(ALLOW_OLD_UNSIGNED_CHAT_TOOLTIP),
                    Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.allowOldUnsignedChat),
                    (value) -> MulticonnectConfig.INSTANCE.allowOldUnsignedChat = value
            );

    private static final OptionInstance<Boolean> enablePacketRecorder =
            OptionInstance.createBoolean(
                    "multiconnect.config.enablePacketRecording",
                    OptionInstance.cachedConstantTooltip(ALLOW_PACKET_RECORDER_CHAT_TOOLTIP),
                    Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.enablePacketRecorder),
                    (value) -> MulticonnectConfig.INSTANCE.enablePacketRecorder = value
            );

    private OptionsList list;

    protected ConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSmall(new OptionInstance[]{debugKey, allowOldUnsignedChat, enablePacketRecorder});
        this.addWidget(this.list);
        addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> onClose()));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, font, TITLE.getVisualOrderText(), width / 2, 20, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
        List<FormattedCharSequence> list = OptionsSubScreen.tooltipAt(this.list, mouseX, mouseY);
        if (this.list != null) {
            this.renderTooltip(matrices, list, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        MulticonnectConfig.INSTANCE.save();
        minecraft.setScreen(parent);
    }
}
