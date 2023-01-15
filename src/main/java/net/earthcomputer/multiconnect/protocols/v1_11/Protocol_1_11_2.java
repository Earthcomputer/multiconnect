package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;

public class Protocol_1_11_2 extends ProtocolBehavior {
    @Override
    public void onCommandRegistration(CommandRegistrationArgs args) {
        BrigadierRemover.of(args.dispatcher()).get("advancement").remove();
        BrigadierRemover.of(args.dispatcher()).get("function").remove();
        BrigadierRemover.of(args.dispatcher()).get("recipe").remove();
        BrigadierRemover.of(args.dispatcher()).get("reload").remove();

        Commands_1_12_2.registerVanilla(args.dispatcher(), args.serverCommands(), "achievement", AchievementCommand::register);
    }
}
