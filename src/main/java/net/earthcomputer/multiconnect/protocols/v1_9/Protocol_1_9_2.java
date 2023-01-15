package net.earthcomputer.multiconnect.protocols.v1_9;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;

public class Protocol_1_9_2 extends ProtocolBehavior {
    @Override
    public void onCommandRegistration(CommandRegistrationArgs args) {
        BrigadierRemover.of(args.dispatcher()).get("stopsound").remove();
    }
}
