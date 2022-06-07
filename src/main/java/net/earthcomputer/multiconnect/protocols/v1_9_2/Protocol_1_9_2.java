package net.earthcomputer.multiconnect.protocols.v1_9_2;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_9_4.Protocol_1_9_4;
import net.minecraft.command.CommandSource;

import java.util.Set;

public class Protocol_1_9_2 extends Protocol_1_9_4 {
    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("stopsound").remove();
    }
}
