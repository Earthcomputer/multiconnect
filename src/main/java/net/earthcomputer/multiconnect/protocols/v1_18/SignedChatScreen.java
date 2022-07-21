package net.earthcomputer.multiconnect.protocols.v1_18;

import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class SignedChatScreen extends Screen {
    private static final Component TITLE = Component.translatable("multiconnect.signedChatScreen.title");
    private static final Component LINE1 = Component.translatable("multiconnect.signedChatScreen.line1");
    private static final Component LINE2 = Component.translatable("multiconnect.signedChatScreen.line2");

    private final Screen parent;

    public SignedChatScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(width / 2 - 100, height - 50, 80, 20, CommonComponents.GUI_YES, button -> {
            MulticonnectConfig.INSTANCE.allowOldUnsignedChat = true;
            MulticonnectConfig.INSTANCE.save();
            onClose();
        }));
        addRenderableWidget(new Button(width / 2 + 20, height - 50, 80, 20, CommonComponents.GUI_NO, button -> {
            MulticonnectConfig.INSTANCE.allowOldUnsignedChat = false;
            MulticonnectConfig.INSTANCE.save();
            onClose();
        }));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        int y = 20;

        drawCenteredString(matrices, font, TITLE.getVisualOrderText(), width / 2, y, 0xffffff);
        y += font.lineHeight + 10;

        for (FormattedCharSequence line : font.split(LINE1, width - 40)) {
            drawCenteredString(matrices, font, line, width / 2, y, 0xe0e0e0);
            y += font.lineHeight;
        }
        y += 10;

        for (FormattedCharSequence line : font.split(LINE2, width - 40)) {
            drawCenteredString(matrices, font, line, width / 2, y, 0xe0e0e0);
            y += font.lineHeight;
        }
    }

    @Override
    protected void updateNarrationState(NarrationElementOutput builder) {
        super.updateNarrationState(builder);
        builder.nest().add(NarratedElementType.TITLE, LINE1);
        builder.nest().add(NarratedElementType.TITLE, LINE2);
    }
}
