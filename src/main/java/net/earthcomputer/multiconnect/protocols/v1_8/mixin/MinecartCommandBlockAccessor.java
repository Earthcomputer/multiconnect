package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartCommandBlock.class)
public interface MinecartCommandBlockAccessor {
    @Accessor("DATA_ID_COMMAND_NAME")
    static EntityDataAccessor<String> getDataIdCommandName() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_ID_LAST_OUTPUT")
    static EntityDataAccessor<Component> getDataIdLastOutput() {
        return MixinHelper.fakeInstance();
    }
}
