package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IIdList;
import net.minecraft.util.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.IdentityHashMap;
import java.util.List;

@Mixin(IdList.class)
public class MixinIdList<T> implements IIdList {

    @Shadow private int nextId;
    @Shadow @Final private IdentityHashMap<T, Integer> idMap;
    @Shadow @Final private List<T> list;

    @Override
    public void clear() {
        nextId = 0;
        idMap.clear();
        list.clear();
    }
}
