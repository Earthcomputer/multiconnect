package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.context.ParsedArgument;
import net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.Custom_1_12_Argument;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Iterator;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {

    @ModifyVariable(method = "formatText", at = @At(value = "STORE", ordinal = 0))
    private static Iterator<ParsedArgument<SharedSuggestionProvider, ?>> decorateHighlightIterator(Iterator<ParsedArgument<SharedSuggestionProvider, ?>> itr) {
        return Iterators.concat(
                Iterators.transform(itr,
                        arg -> arg != null && arg.getResult() instanceof Custom_1_12_Argument ?
                                decorateHighlightIterator(((Custom_1_12_Argument) arg.getResult()).<SharedSuggestionProvider>getSubArgs().iterator())
                                : Iterators.singletonIterator(arg))
        );
    }

}
