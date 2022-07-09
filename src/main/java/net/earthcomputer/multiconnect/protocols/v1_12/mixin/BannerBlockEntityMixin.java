package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

@Mixin(BannerBlockEntity.class)
public class BannerBlockEntityMixin extends BlockEntity {

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

    public BannerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    private void loadBase(CompoundTag tag, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            multiconnect_setBaseColor(DyeColor.byId(tag.getInt("Base")));
        }
    }

    @Unique
    private void multiconnect_setBaseColor(DyeColor color) {
        BlockState state = getBlockState();
        if (!getType().isValid(state))
            return;

        BlockState newState;
        if (state.getBlock() instanceof WallBannerBlock) {
            newState = WALL_BANNERS_BY_COLOR.get(color).defaultBlockState().setValue(WallBannerBlock.FACING, state.getValue(WallBannerBlock.FACING));
        } else {
            newState = BannerBlock.byColor(color).defaultBlockState().setValue(BannerBlock.ROTATION, state.getValue(BannerBlock.ROTATION));
        }
        assert level != null;
        level.setBlock(getBlockPos(), newState, 18);
    }

}
