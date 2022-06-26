package net.earthcomputer.multiconnect.debug;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.http.impl.bootstrap.HttpServer;

public class RunningHttpServerScreen extends Screen {
    private static final Text TITLE = Text.translatable("multiconnect.debug.runningHttpServer.title");

    private final HttpServer server;

    public RunningHttpServerScreen(HttpServer server) {
        super(TITLE);
        this.server = server;
    }

    @Override
    protected void init() {
        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 20, 200, 20, Text.translatable("multiconnect.debug.runningHttpServer.stop"), button -> {
            close();
        }));
    }

    @Override
    public void close() {
        server.stop();
        super.close();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, TITLE, width / 2, height / 2 - 50, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
