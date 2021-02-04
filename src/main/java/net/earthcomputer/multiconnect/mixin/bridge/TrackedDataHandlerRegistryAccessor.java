package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.collection.Int2ObjectBiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrackedDataHandlerRegistry.class)
public interface TrackedDataHandlerRegistryAccessor {

    @Accessor("DATA_HANDLERS")
    static Int2ObjectBiMap<TrackedDataHandler<?>> getDataHandlers() {
        return MixinHelper.fakeInstance();
    }

}
