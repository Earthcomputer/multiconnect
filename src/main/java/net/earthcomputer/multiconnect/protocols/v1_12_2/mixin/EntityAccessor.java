package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("CUSTOM_NAME")
    static DataParameter<Optional<ITextComponent>> getCustomName() {
        return MixinHelper.fakeInstance();
    }
}
