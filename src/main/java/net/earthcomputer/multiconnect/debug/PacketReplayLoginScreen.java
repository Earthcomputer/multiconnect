package net.earthcomputer.multiconnect.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

final class PacketReplayLoginScreen extends Screen {
    private static final Component STARTING_REPLAY = Component.translatable("multiconnect.debug.startingReplay");

    private final Connection connection;
    private Component status = Component.translatable("connect.connecting");

    PacketReplayLoginScreen(Connection connection) {
        super(STARTING_REPLAY);
        this.connection = connection;
    }

    public void setStatus(Component status) {
        this.status = status;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(width / 2 - 100, height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, button -> {
            connection.disconnect(Component.translatable("connect.aborted"));
            PacketReplay.stop();
            assert minecraft != null;
            minecraft.setScreen(new TitleScreen());
        }));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredString(matrices, font, STARTING_REPLAY, width / 2, height / 2 - 100, 0xffffff);
        drawCenteredString(matrices, font, status, width / 2, height / 2 - 50, 0xe0e0e0);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
