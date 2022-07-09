package net.earthcomputer.multiconnect.protocols.v1_12.command.arguments;

import com.mojang.brigadier.context.ParsedArgument;

import java.util.List;

public final class Custom_1_12_Argument {

    private final List<ParsedArgument<?, ?>> subArgs;

    public Custom_1_12_Argument(List<ParsedArgument<?, ?>> subArgs) {
        this.subArgs = subArgs;
    }

    @SuppressWarnings("unchecked")
    public <S> List<ParsedArgument<S, ?>> getSubArgs() {
        return (List<ParsedArgument<S, ?>>) (List<?>) subArgs;
    }
}
