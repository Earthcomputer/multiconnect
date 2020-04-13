package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ArgumentTypes.class)
public class MixinArgumentTypes {

    private static final ResourceLocation NBT = new ResourceLocation("nbt");
    private static final ResourceLocation NBT_COMPOUND_TAG = new ResourceLocation("nbt_compound_tag");

    @ModifyVariable(method = "get(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/command/arguments/ArgumentTypes$Entry;", ordinal = 0, at = @At("HEAD"))
    private static ResourceLocation modifyById(ResourceLocation id) {
        return ConnectionInfo.protocolVersion <= Protocols.V1_13_2 && NBT.equals(id) ? NBT_COMPOUND_TAG : id;
    }

}
