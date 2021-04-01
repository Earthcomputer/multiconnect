package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.*;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

@Mixin(BannerBlockEntity.class)
public class MixinBannerBlockEntity extends BlockEntity {

    @Unique private static final Map<DyeColor, Block> WALL_BANNERS_BY_COLOR = new EnumMap<>(DyeColor.class);
    static {
        WALL_BANNERS_BY_COLOR.put(DyeColor.WHITE, Blocks.WHITE_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.ORANGE, Blocks.ORANGE_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.MAGENTA, Blocks.MAGENTA_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.YELLOW, Blocks.YELLOW_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.LIME, Blocks.LIME_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.PINK, Blocks.PINK_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.GRAY, Blocks.GRAY_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.CYAN, Blocks.CYAN_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.PURPLE, Blocks.PURPLE_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.BLUE, Blocks.BLUE_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.BROWN, Blocks.BROWN_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.GREEN, Blocks.GREEN_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.RED, Blocks.RED_WALL_BANNER);
        WALL_BANNERS_BY_COLOR.put(DyeColor.BLACK, Blocks.BLACK_WALL_BANNER);
    }

    public MixinBannerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
    private void readBase(NbtCompound tag, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            setBaseColor(DyeColor.byId(tag.getInt("Base")));
        }
    }

    @Unique
    private void setBaseColor(DyeColor color) {
        BlockState state = getCachedState();
        if (!getType().supports(state))
            return;

        BlockState newState;
        if (state.getBlock() instanceof WallBannerBlock) {
            newState = WALL_BANNERS_BY_COLOR.get(color).getDefaultState().with(WallBannerBlock.FACING, state.get(WallBannerBlock.FACING));
        } else {
            newState = BannerBlock.getForColor(color).getDefaultState().with(BannerBlock.ROTATION, state.get(BannerBlock.ROTATION));
        }
        assert world != null;
        world.setBlockState(getPos(), newState, 18);
    }

}
