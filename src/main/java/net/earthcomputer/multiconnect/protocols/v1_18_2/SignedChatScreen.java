package net.earthcomputer.multiconnect.protocols.v1_18_2;

import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class SignedChatScreen extends Screen {
    private static final Text TITLE = Text.translatable("multiconnect.signedChatScreen.title");
    private static final Text LINE1 = Text.translatable("multiconnect.signedChatScreen.line1");
    private static final Text LINE2 = Text.translatable("multiconnect.signedChatScreen.line2");

    private final Screen parent;

    public SignedChatScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(new ButtonWidget(width / 2 - 100, height - 50, 80, 20, ScreenTexts.YES, button -> {
            MulticonnectConfig.INSTANCE.allowOldUnsignedChat = true;
            MulticonnectConfig.INSTANCE.save();
            close();
        }));
        addDrawableChild(new ButtonWidget(width / 2 + 20, height - 50, 80, 20, ScreenTexts.NO, button -> {
            MulticonnectConfig.INSTANCE.allowOldUnsignedChat = false;
            MulticonnectConfig.INSTANCE.save();
            close();
        }));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        int y = 20;

        drawCenteredTextWithShadow(matrices, textRenderer, TITLE.asOrderedText(), width / 2, y, 0xffffff);
        y += textRenderer.fontHeight + 10;

        for (OrderedText line : textRenderer.wrapLines(LINE1, width - 40)) {
            drawCenteredTextWithShadow(matrices, textRenderer, line, width / 2, y, 0xe0e0e0);
            y += textRenderer.fontHeight;
        }
        y += 10;

        for (OrderedText line : textRenderer.wrapLines(LINE2, width - 40)) {
            drawCenteredTextWithShadow(matrices, textRenderer, line, width / 2, y, 0xe0e0e0);
            y += textRenderer.fontHeight;
        }
    }

    @Override
    protected void addScreenNarrations(NarrationMessageBuilder builder) {
        super.addScreenNarrations(builder);
        builder.nextMessage().put(NarrationPart.TITLE, LINE1);
        builder.nextMessage().put(NarrationPart.TITLE, LINE2);
    }
}
