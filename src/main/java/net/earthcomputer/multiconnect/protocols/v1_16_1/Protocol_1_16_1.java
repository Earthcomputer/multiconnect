package net.earthcomputer.multiconnect.protocols.v1_16_1;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.AbstractPiglinEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.PiglinEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_2.Protocol_1_16_2;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;

public class Protocol_1_16_1 extends Protocol_1_16_2 {
    private static final TrackedData<Boolean> OLD_IMMUNE_TO_ZOMBIFICATION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.MUSHROOM_GROW_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM);
        tags.add(BlockTags.BASE_STONE_OVERWORLD, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE);
        tags.add(BlockTags.BASE_STONE_NETHER, Blocks.NETHERRACK, Blocks.BASALT, Blocks.BLACKSTONE);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        tags.add(ItemTags.STONE_CRAFTING_MATERIALS, Items.COBBLESTONE, Items.BLACKSTONE);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == PiglinEntity.class && data == PiglinEntityAccessor.getCharging()) {
            DataTrackerManager.registerOldTrackedData(PiglinEntity.class, OLD_IMMUNE_TO_ZOMBIFICATION, false, AbstractPiglinEntity::setImmuneToZombification);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == AbstractPiglinEntity.class && data == AbstractPiglinEntityAccessor.getImmuneToZombification()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
