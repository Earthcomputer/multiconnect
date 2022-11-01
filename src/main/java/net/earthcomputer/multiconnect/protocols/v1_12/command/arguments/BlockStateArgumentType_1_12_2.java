package net.earthcomputer.multiconnect.protocols.v1_12.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class BlockStateArgumentType_1_12_2 implements ArgumentType<Custom_1_12_Argument> {

    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone foo=bar");

    private final boolean test;

    private BlockStateArgumentType_1_12_2(boolean test) {
        this.test = test;
    }

    public static BlockStateArgumentType_1_12_2 blockState() {
        return new BlockStateArgumentType_1_12_2(false);
    }

    public static BlockStateArgumentType_1_12_2 testBlockState() {
        return new BlockStateArgumentType_1_12_2(true);
    }

    @Override
    public Custom_1_12_Argument parse(StringReader reader) throws CommandSyntaxException {
        var result = new ArrayList<ParsedArgument<?, ?>>();

        int start = reader.getCursor();
        ResourceLocation id = ResourceLocation.read(reader);
        if (!Registry.BLOCK.containsKey(id)) {
            reader.setCursor(start);
            throw BlockStateParser.ERROR_UNKNOWN_BLOCK.createWithContext(reader, id);
        }
        Block block = Registry.BLOCK.get(id);
        if (!isValidBlock(block)) {
            reader.setCursor(start);
            throw BlockStateParser.ERROR_UNKNOWN_BLOCK.createWithContext(reader, id);
        }

        result.add(new ParsedArgument<>(start, reader.getCursor(), block));
        if (!reader.canRead())
            return new Custom_1_12_Argument(result);

        reader.expect(' ');
        start = reader.getCursor();
        try {
            int meta;
            if (!test && reader.peek() == '*') {
                reader.skip();
                meta = -1;
            } else {
                meta = reader.readInt();
            }
            if (meta >= (test ? -1 : 0) && meta < 16 && (!reader.canRead() || reader.peek() == ' ')) {
                result.add(new ParsedArgument<>(start, reader.getCursor(), meta));
                return new Custom_1_12_Argument(result);
            }
        } catch (CommandSyntaxException ignore) {
        }
        reader.setCursor(start);
        if ("default".equals(reader.readUnquotedString())) {
            result.add(new ParsedArgument<>(start, reader.getCursor(), 0));
            return new Custom_1_12_Argument(result);
        }

        reader.setCursor(start);

        // TODO: rewrite for via
//        List<String> properties = BlockStateReverseData.OLD_PROPERTIES.getOrDefault(id, Collections.emptyList());
//        Set<String> alreadySeen = new HashSet<>();
//        while (reader.canRead() && reader.peek() != ' ') {
//            int propStart = reader.getCursor();
//            String property = reader.readUnquotedString();
//            if (alreadySeen.contains(property)) {
//                reader.setCursor(propStart);
//                throw BlockStateParser.ERROR_DUPLICATE_PROPERTY.createWithContext(reader, id, property);
//            }
//            if (!properties.contains(property)) {
//                reader.setCursor(propStart);
//                throw BlockStateParser.ERROR_UNKNOWN_PROPERTY.createWithContext(reader, id, property);
//            }
//            alreadySeen.add(property);
//            reader.expect('=');
//            int valueStart = reader.getCursor();
//            String value = reader.readUnquotedString();
//            if (!BlockStateReverseData.OLD_PROPERTY_VALUES.get(Pair.of(id, property)).contains(value)) {
//                reader.setCursor(valueStart);
//                throw BlockStateParser.ERROR_INVALID_VALUE.createWithContext(reader, id, property, value);
//            }
//            if (reader.canRead() && reader.peek() != ' ')
//                reader.expect(',');
//        }

        result.add(new ParsedArgument<>(start, reader.getCursor(), null));
        return new Custom_1_12_Argument(result);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        int spaceIndex = builder.getRemaining().indexOf(' ');
        if (spaceIndex == -1) {
            SharedSuggestionProvider.suggestResource(Registry.BLOCK.keySet().stream().filter(id -> isValidBlock(Registry.BLOCK.get(id))), builder);
            return builder.buildFuture();
        }

        ResourceLocation blockId = ResourceLocation.tryParse(builder.getInput().substring(builder.getStart(), builder.getStart() + spaceIndex));

        String propertiesStr = builder.getInput().substring(builder.getStart() + spaceIndex + 1);

        int commaIndex = propertiesStr.lastIndexOf(',');
        int equalsIndex = propertiesStr.lastIndexOf('=');
        builder = builder.createOffset(builder.getStart() + spaceIndex + commaIndex + 2);

        if (commaIndex == -1 && equalsIndex == -1) {
            SharedSuggestionProvider.suggest(new String[] {"default"}, builder);
            if (test)
                SharedSuggestionProvider.suggest(new String[] {"*"}, builder);
        }

        // TODO: rewrite for via
//        if (blockId == null || !BlockStateReverseData.OLD_PROPERTIES.containsKey(blockId))
//            return builder.buildFuture();
//
//        if (equalsIndex <= commaIndex) {
//            SharedSuggestionProvider.suggest(BlockStateReverseData.OLD_PROPERTIES.get(blockId).stream().map(str -> str + "="), builder);
//        } else {
//            String property = builder.getInput().substring(builder.getStart(), builder.getStart() + equalsIndex - commaIndex - 1);
//            List<String> values = BlockStateReverseData.OLD_PROPERTY_VALUES.get(Pair.of(blockId, property));
//            if (values != null) {
//                builder = builder.createOffset(builder.getStart() + equalsIndex - commaIndex);
//                SharedSuggestionProvider.suggest(values, builder);
//            }
//        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isValidBlock(Block block) {
        return Registry.BLOCK.getId(block) < 256;
    }

}
