package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Arrays;
import java.util.Collection;

public final class XPArgumentType implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("0", "123", "-123", "30L");

    private XPArgumentType() {
    }

    public static XPArgumentType xp() {
        return new XPArgumentType();
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int amount = reader.readInt();
        if (reader.canRead() && (reader.peek() == 'l' || reader.peek() == 'L'))
            reader.skip();
        return amount;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
