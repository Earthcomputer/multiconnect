package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.generic.IIdList;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Mixin(IdList.class)
public class MixinIdList<T> implements IIdList {

    @Shadow private int nextId;
    @Shadow @Final private IdentityHashMap<T, Integer> idMap;
    @Shadow @Final private List<T> list;

    @Unique private int minHighIds = Integer.MAX_VALUE;
    @Unique private final Int2ObjectMap<T> highIdsMap = new Int2ObjectOpenHashMap<>();

    @Redirect(method = "set", at = @At(value = "INVOKE", target = "Ljava/util/IdentityHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object redirectSetPut(IdentityHashMap<T, Integer> idMap, T key, Object value) {
        return idMap.putIfAbsent(key, (Integer) value);
    }

    @Inject(method = "set", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false), cancellable = true)
    private void onSet(T value, int id, CallbackInfo ci) {
        if (id > list.size() + 4096) {
            if (id < minHighIds)
                minHighIds = id;
            highIdsMap.put(id, value);
            if (nextId <= id)
                nextId = id + 1;
            ci.cancel();
        }
        else if (id > minHighIds) {
            minHighIds = Integer.MAX_VALUE;
            while (id >= list.size())
                list.add(null);
            Iterator<Int2ObjectMap.Entry<T>> itr = highIdsMap.int2ObjectEntrySet().iterator();
            while (itr.hasNext()) {
                Int2ObjectMap.Entry<T> entry = itr.next();
                if (entry.getIntKey() <= id) {
                    list.set(entry.getIntKey(), entry.getValue());
                    itr.remove();
                } else {
                    minHighIds = entry.getIntKey();
                }
            }
        }
    }

    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private void onGet(int id, CallbackInfoReturnable<T> ci) {
        if (ci.getReturnValue() == null) {
            ci.setReturnValue(highIdsMap.get(id));
        }
    }

    @Inject(method = "iterator", at = @At("RETURN"), cancellable = true)
    private void onIterator(CallbackInfoReturnable<Iterator<T>> ci) {
        ci.setReturnValue(Iterators.concat(ci.getReturnValue(),
                highIdsMap.int2ObjectEntrySet().stream()
                        .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                        .map(Int2ObjectMap.Entry::getValue)
                        .iterator()));
    }

    @Override
    public void multiconnect_clear() {
        nextId = 0;
        idMap.clear();
        list.clear();
        highIdsMap.clear();
        minHighIds = Integer.MAX_VALUE;
    }

    @Override
    public Iterable<Integer> multiconnect_ids() {
        return Stream.concat(IntStream.range(0, list.size()).filter(i -> list.get(i) != null).boxed(),
                highIdsMap.keySet().stream().sorted())::iterator;
    }
}
