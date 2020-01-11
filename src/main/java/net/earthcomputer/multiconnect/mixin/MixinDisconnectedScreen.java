package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.api.EnumProtocol;
import net.earthcomputer.multiconnect.impl.IServerInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.ServerList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {

    @Shadow private int reasonHeight;
    @Shadow @Final private Screen parent;

    @Unique private int y;
    @Unique private boolean isProtocolReason;
    @Unique private ServerList serverList;
    @Unique private ServerInfo selectedServer;
    @Unique private ServerInfo editingServer;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Screen parentScreen, String title, Text reason, CallbackInfo ci) {
        isProtocolReason = false;
        if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
            String reasonText = reason.asString();
            for (EnumProtocol protocol : EnumProtocol.values()) {
                if (protocol != EnumProtocol.AUTO && reasonText.contains(protocol.getName())) {
                    isProtocolReason = true;
                    break;
                }
            }
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addButtons(CallbackInfo ci) {
        if (isProtocolReason) {
            y = Math.min(height / 2 + reasonHeight / 2 + 9, height - 30) + 28;
            addButton(new ButtonWidget(width / 2 - 100, y + 12, 200, 20, I18n.translate("multiconnect.changeForcedProtocol"),
                    button -> {
                editingServer = new ServerInfo(selectedServer.name, selectedServer.address, false);
                editingServer.copyFrom(selectedServer);
                MinecraftClient.getInstance().openScreen(new AddServerScreen(parent, this::editEntry, editingServer));
            }));
            ServerInfo currentServer = MinecraftClient.getInstance().getCurrentServerEntry();
            assert currentServer != null;
            serverList = new ServerList(MinecraftClient.getInstance());
            serverList.loadFile();
            for (int i = 0; i < serverList.size(); i++) {
                ServerInfo server = serverList.get(i);
                if (server.address.equals(currentServer.address)) {
                    selectedServer = server;
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V"))
    private void onRender(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isProtocolReason) {
            drawCenteredString(font, I18n.translate("multiconnect.wrongProtocolHint"), width / 2, y, 0xffffff);
        }
    }

    @Unique
    private void editEntry(boolean accepted) {
        if (accepted) {
            selectedServer.name = editingServer.name;
            selectedServer.address = editingServer.address;
            selectedServer.copyFrom(editingServer);
            for (ServerInfo server : IServerInfo.INSTANCES)
                if (server.address.equals(editingServer.address))
                    server.copyFrom(editingServer);
            serverList.saveFile();
        }
        MinecraftClient.getInstance().openScreen(parent);
    }

}
