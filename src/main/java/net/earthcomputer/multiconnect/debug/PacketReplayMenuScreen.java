package net.earthcomputer.multiconnect.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PacketReplayMenuScreen extends Screen {
    private static final Component TITLE = Component.translatable("multiconnect.debug.menu.title");

    public PacketReplayMenuScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(width / 2 - 100, height / 2 + 20, 200, 20, Component.translatable("multiconnect.debug.menu.HttpServer.start"), button -> {
            PacketReplay.startHttpServer();
        }));

        addRenderableWidget(new Button(width / 2 - 100, height / 2 + 20 + 30, 200, 20, Component.translatable("multiconnect.debug.menu.start"), button -> {
            PacketReplay.start();
        }));

        addRenderableWidget(new Button(width / 2 - 100, height / 2 + 20 + 60, 200, 20, Component.translatable("multiconnect.debug.menu.cancel"), button -> {
            onClose();
        }));
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredString(matrices, font, TITLE, width / 2, height / 2 - 50, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
