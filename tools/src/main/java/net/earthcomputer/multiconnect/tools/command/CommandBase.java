package net.earthcomputer.multiconnect.tools.command;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract sealed class CommandBase permits RenameCommand, RenamePacketCommand {
    public static final Map<String, CommandBase> COMMANDS;
    static {
        Map<String, CommandBase> commands = new TreeMap<>();
        for (Class<?> permittedSubclass : CommandBase.class.getPermittedSubclasses()) {
            CommandBase command;
            try {
                command = (CommandBase) permittedSubclass.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            commands.put(command.getName(), command);
        }
        COMMANDS = Collections.unmodifiableMap(new LinkedHashMap<>(commands));
    }

    public abstract String getName();

    public abstract boolean run(List<String> args, OptionSet options) throws IOException;

    public abstract void printHelp(List<String> args, OptionSet options);

    public List<OptionSpec<?>> getRequiredFlags() {
        return Collections.emptyList();
    }

    public List<OptionSpec<?>> getOptionalFlags() {
        return Collections.emptyList();
    }
}
