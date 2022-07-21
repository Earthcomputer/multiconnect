package net.earthcomputer.multiconnect.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.http.impl.bootstrap.HttpServer;

public class PacketReplayHttpServerScreen extends Screen {
    private static final Component TITLE = Component.translatable("multiconnect.debug.runningHttpServer.title");

    private final HttpServer server;

    public PacketReplayHttpServerScreen(HttpServer server) {
        super(TITLE);
        this.server = server;
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(width / 2 - 100, height / 2 + 20, 200, 20, Component.translatable("multiconnect.debug.runningHttpServer.stop"), button -> {
            onClose();
        }));
    }

    @Override
    public void onClose() {
        server.stop();
        this.minecraft.setScreen(new PacketReplayMenuScreen());
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredString(matrices, font, TITLE, width / 2, height / 2 - 50, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
