package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ArgumentTypes.class)
public class MixinArgumentTypes {

    private static final Identifier NBT = new Identifier("nbt");
    private static final Identifier NBT_COMPOUND_TAG = new Identifier("nbt_compound_tag");

    @ModifyVariable(method = "byId", ordinal = 0, at = @At("HEAD"))
    private static Identifier modifyById(Identifier id) {
        return ConnectionInfo.protocolVersion <= Protocols.V1_13_2 && NBT.equals(id) ? NBT_COMPOUND_TAG : id;
    }

}
