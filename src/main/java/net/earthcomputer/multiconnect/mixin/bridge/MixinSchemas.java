package net.earthcomputer.multiconnect.mixin.bridge;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceTypesFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Schemas.class)
public class MixinSchemas {
    @SuppressWarnings("unchecked")
    @Inject(method = "build",
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
        builder.addFixer(new ChoiceTypesFix(schema1466, "multiconnect: fix block entity type", TypeReferences.BLOCK_ENTITY));
    }
}
