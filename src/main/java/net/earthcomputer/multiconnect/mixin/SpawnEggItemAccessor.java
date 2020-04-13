package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.SpawnEggItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SpawnEggItem.class)
public interface SpawnEggItemAccessor {

    @Accessor("EGGS")
    static Map<EntityType<?>, SpawnEggItem> getSpawnEggs() {
        return MixinHelper.fakeInstance();
    }

}
