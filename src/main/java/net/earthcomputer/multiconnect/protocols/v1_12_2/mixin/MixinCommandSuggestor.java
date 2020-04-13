package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.context.ParsedArgument;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.Custom_1_12_Argument;
import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.command.ISuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Iterator;

@Mixin(CommandSuggestionHelper.class)
public class MixinCommandSuggestor {

    @ModifyVariable(method = "func_228116_a_", at = @At(value = "STORE", ordinal = 0))
    private static Iterator<ParsedArgument<ISuggestionProvider, ?>> decorateHighlightIterator(Iterator<ParsedArgument<ISuggestionProvider, ?>> itr) {
        return Iterators.concat(
                Iterators.transform(itr,
                        arg -> arg != null && arg.getResult() instanceof Custom_1_12_Argument ?
                                decorateHighlightIterator(((Custom_1_12_Argument) arg.getResult()).<ISuggestionProvider>getSubArgs().iterator())
                                : Iterators.singletonIterator(arg))
        );
    }

}
