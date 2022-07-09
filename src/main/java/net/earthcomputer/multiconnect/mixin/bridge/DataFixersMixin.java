package net.earthcomputer.multiconnect.mixin.bridge;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.References;

@Mixin(DataFixers.class)
public class DataFixersMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "addFixers",
            slice = @Slice(from = @At(value = "CONSTANT", args = "intValue=1466")),
            at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DataFixerBuilder;addFixer(Lcom/mojang/datafixers/DataFix;)V", ordinal = 0, remap = false))
    private static void fixBlockEntityType(DataFixerBuilder builder, CallbackInfo ci) {
        Int2ObjectSortedMap<Schema> schemas;
        try {
            Field field = DataFixerBuilder.class.getDeclaredField("schemas");
            field.setAccessible(true);
            schemas = (Int2ObjectSortedMap<Schema>) field.get(builder);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }

        Schema schema1466 = schemas.get(DataFixUtils.makeKey(1466, 0));
        builder.addFixer(new AddNewChoices(schema1466, "multiconnect: fix block entity type", References.BLOCK_ENTITY));
    }
}
