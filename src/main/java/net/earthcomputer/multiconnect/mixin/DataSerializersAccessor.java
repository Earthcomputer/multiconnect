package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.IntIdentityHashBiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataSerializers.class)
public interface DataSerializersAccessor {

    @Accessor("REGISTRY")
    static IntIdentityHashBiMap<IDataSerializer<?>> getHandlers() {
        return MixinHelper.fakeInstance();
    }

}
