package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IInt2ObjectBiMap;
import net.minecraft.util.Int2ObjectBiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Int2ObjectBiMap.class)
public abstract class MixinInt2ObjectBiMap<K> implements IInt2ObjectBiMap<K> {

    @Shadow private K[] values;
    @Shadow private int[] ids;
    @Shadow private K[] idToValues;
    @Shadow private int size;
    @Shadow private int nextId;

    @Shadow protected abstract int getIdealIndex(K k);
    @Shadow protected abstract int findIndex(K k, int idealIndex);

    @Override
    public void remove(K k) {
        if (k == null) throw new NullPointerException();

        int idealIndex = getIdealIndex(k);
        int index = findIndex(k, idealIndex);
        if (index == -1)
            return;

        values[index] = null;
        int id = ids[index];
        ids[index] = 0;
        idToValues[id] = null;

        int lastIndex = index;

        do {
            index = (index + 1) % values.length;
            if (values[index] == null)
                break;
            int thisIdealIndex = getIdealIndex(values[index]);
            if (index > lastIndex ? (thisIdealIndex <= lastIndex || thisIdealIndex > index) : (thisIdealIndex <= lastIndex && thisIdealIndex > index)) {
                values[lastIndex] = values[index];
                values[index] = null;
                ids[lastIndex] = ids[index];
                ids[index] = 0;
                lastIndex = index;
            }
        } while (index != idealIndex);

        size--;
        if (id == nextId - 1)
            nextId--;
    }
}
