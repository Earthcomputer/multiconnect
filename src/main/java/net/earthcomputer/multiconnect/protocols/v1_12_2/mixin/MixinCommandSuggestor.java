package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.context.ParsedArgument;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.Custom_1_12_Argument;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.server.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Iterator;

@Mixin(CommandSuggestor.class)
public class MixinCommandSuggestor {

    @ModifyVariable(method = "highlight", at = @At(value = "STORE", ordinal = 0))
    private static Iterator<ParsedArgument<CommandSource, ?>> decorateHighlightIterator(Iterator<ParsedArgument<CommandSource, ?>> itr) {
        return Iterators.concat(
                Iterators.transform(itr,
                        arg -> arg != null && arg.getResult() instanceof Custom_1_12_Argument ?
                                ((Custom_1_12_Argument) arg.getResult()).<CommandSource>getSubArgs().iterator()
                                : Iterators.singletonIterator(arg))
        );
    }

}
