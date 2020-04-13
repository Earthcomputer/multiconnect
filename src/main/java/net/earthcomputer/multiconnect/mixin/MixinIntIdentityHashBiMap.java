package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IIntIdentityHashBiMap;
import net.minecraft.util.IntIdentityHashBiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IntIdentityHashBiMap.class)
public abstract class MixinIntIdentityHashBiMap<K> implements IIntIdentityHashBiMap<K> {

    @Shadow private K[] values;
    @Shadow private int[] intKeys;
    @Shadow private K[] byId;
    @Shadow private int mapSize;
    @Shadow private int nextFreeIndex;

    @Shadow protected abstract int hashObject(K k);
    @Shadow protected abstract int getIndex(K k, int idealIndex);

    @Override
    public void multiconnect_remove(K k) {
        if (k == null) throw new NullPointerException();

        int idealIndex = hashObject(k);
        int index = getIndex(k, idealIndex);
        if (index == -1)
            return;

        values[index] = null;
        int id = intKeys[index];
        intKeys[index] = 0;
        byId[id] = null;

        int lastIndex = index;

        do {
            index = (index + 1) % values.length;
            if (values[index] == null)
                break;
            int thisIdealIndex = hashObject(values[index]);
            if (index > lastIndex ? (thisIdealIndex <= lastIndex || thisIdealIndex > index) : (thisIdealIndex <= lastIndex && thisIdealIndex > index)) {
                values[lastIndex] = values[index];
                values[index] = null;
                intKeys[lastIndex] = intKeys[index];
                intKeys[index] = 0;
                lastIndex = index;
            }
        } while (index != idealIndex);

        mapSize--;
        if (id == nextFreeIndex - 1)
            nextFreeIndex--;
    }
}
