package net.earthcomputer.multiconnect.protocols.v1_12;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Protocol_1_12_2 extends ProtocolBehavior {
    @Override
    public Float getDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0.75f;
        }
        return null;
    }

    @Override
    public void onCommandRegistration(CommandRegistrationArgs args) {
        Commands_1_12_2.register(args.context(), args.dispatcher(), args.serverCommands());
    }
}
