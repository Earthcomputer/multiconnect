package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandBlockMinecartEntity.class)
public interface CommandBlockMinecartEntityAccessor {
    @Accessor("COMMAND")
    static TrackedData<String> getCommand() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("LAST_OUTPUT")
    static TrackedData<Text> getLastOutput() {
        return MixinHelper.fakeInstance();
    }
}
