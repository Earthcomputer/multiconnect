package net.earthcomputer.multiconnect.debug;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

final class PacketReplayLoginScreen extends Screen {
    private static final Text STARTING_REPLAY = Text.translatable("multiconnect.debug.startingReplay");

    private final ClientConnection connection;
    private Text status = Text.translatable("connect.connecting");

    PacketReplayLoginScreen(ClientConnection connection) {
        super(STARTING_REPLAY);
        this.connection = connection;
    }

    public void setStatus(Text status) {
        this.status = status;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(width / 2 - 100, height / 4 + 120 + 12, 200, 20, ScreenTexts.CANCEL, button -> {
            connection.disconnect(Text.translatable("connect.aborted"));
            PacketReplay.stop();
            assert client != null;
            client.setScreen(new TitleScreen());
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, STARTING_REPLAY, width / 2, height / 2 - 100, 0xffffff);
        drawCenteredText(matrices, textRenderer, status, width / 2, height / 2 - 50, 0xe0e0e0);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
