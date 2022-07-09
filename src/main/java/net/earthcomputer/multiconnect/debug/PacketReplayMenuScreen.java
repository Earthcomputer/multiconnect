package net.earthcomputer.multiconnect.debug;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class PacketReplayMenuScreen extends Screen {
    private static final Text TITLE = Text.translatable("multiconnect.debug.menu.title");

    public PacketReplayMenuScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 20, 200, 20, Text.translatable("multiconnect.debug.menu.HttpServer.start"), button -> {
            PacketReplay.startHttpServer();
        }));

        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 20 + 30, 200, 20, Text.translatable("multiconnect.debug.menu.start"), button -> {
            PacketReplay.start();
        }));

        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 20 + 60, 200, 20, ScreenTexts.CANCEL, button -> {
            close();
        }));
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, TITLE, width / 2, height / 2 - 50, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
