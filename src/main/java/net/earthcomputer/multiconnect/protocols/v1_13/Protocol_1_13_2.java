package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_13.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class Protocol_1_13_2 extends Protocol_1_14 {
    public static final EntityDataAccessor<Integer> OLD_FIREWORK_SHOOTER = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> OLD_VILLAGER_PROFESSION = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Byte> OLD_ILLAGER_FLAGS = SynchedDataManager.createOldEntityData(EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> OLD_SKELETON_ATTACKING = SynchedDataManager.createOldEntityData(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OLD_ZOMBIE_ATTACKING = SynchedDataManager.createOldEntityData(EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> OLD_ZOMBIE_VILLAGER_PROFESSION = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> OLD_HORSE_ARMOR = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);

    public static final Key<byte[][]> BLOCK_LIGHT_KEY = Key.create("blockLight");
    public static final Key<byte[][]> SKY_LIGHT_KEY = Key.create("skyLight");

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Zombie.class && data == ZombieAccessor.getDataDrownedConversionId()) {
            SynchedDataManager.registerOldEntityData(Zombie.class, OLD_ZOMBIE_ATTACKING, false, Mob::setAggressive);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getDataPose())
            return false;
        if (clazz == EyeOfEnder.class && data == EyeOfEnderAccessor.getDataItemStack())
            return false;
        if (clazz == FireworkRocketEntity.class) {
            EntityDataAccessor<OptionalInt> fireworkShooter = FireworkRocketEntityAccessor.getDataAttachedToTarget();
            if (data == fireworkShooter) {
                SynchedDataManager.registerOldEntityData(FireworkRocketEntity.class, OLD_FIREWORK_SHOOTER, 0,
                        (entity, val) -> entity.getEntityData().set(fireworkShooter, val <= 0 ? OptionalInt.empty() : OptionalInt.of(val)));
                return false;
            }
            if (data == FireworkRocketEntityAccessor.getDataShotAtAngle())
                return false;
        }
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getSleepingPosId())
            return false;
        if (clazz == Villager.class) {
            EntityDataAccessor<VillagerData> villagerData = VillagerAccessor.getDataVillagerData();
            if (data == villagerData) {
                SynchedDataManager.registerOldEntityData(Villager.class, OLD_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getEntityData().set(villagerData, entity.getVillagerData().setProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == ZombieVillager.class) {
            EntityDataAccessor<VillagerData> villagerData = ZombieVillagerAccessor.getDataVillagerData();
            if (data == villagerData) {
                SynchedDataManager.registerOldEntityData(ZombieVillager.class, OLD_ZOMBIE_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getEntityData().set(villagerData, entity.getVillagerData().setProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == MushroomCow.class && data == MushroomCowAccessor.getDataType())
            return false;
        if (clazz == Cat.class) {
            if (data == CatAccessor.getIsLying()
                || data == CatAccessor.getRelaxStateOne()
                || data == CatAccessor.getDataCollarColor())
                return false;
        }
        if (clazz == AbstractArrow.class && data == AbstractArrowAccessor.getPierceLevel())
            return false;
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void postEntityDataRegister(Class<? extends Entity> clazz) {
        if (clazz == AbstractIllager.class)
            SynchedDataManager.registerOldEntityData(AbstractIllager.class, OLD_ILLAGER_FLAGS, (byte)0,
                    (entity, val) -> entity.setAggressive((val & 1) != 0));
        if (clazz == AbstractSkeleton.class)
            SynchedDataManager.registerOldEntityData(AbstractSkeleton.class, OLD_SKELETON_ATTACKING, false, Mob::setAggressive);
        if (clazz == Horse.class)
            SynchedDataManager.registerOldEntityData(Horse.class, OLD_HORSE_ARMOR, 0, (entity, val) -> {
                switch (val) {
                    case 1 -> entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_HORSE_ARMOR));
                    case 2 -> entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_HORSE_ARMOR));
                    case 3 -> entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
                    default -> entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                }
            });
        super.postEntityDataRegister(clazz);
    }

    private static VillagerProfession getVillagerProfession(int id) {
        return switch (id) {
            case 0 -> VillagerProfession.FARMER;
            case 1 -> VillagerProfession.LIBRARIAN;
            case 2 -> VillagerProfession.CLERIC;
            case 3 -> VillagerProfession.ARMORER;
            case 4 -> VillagerProfession.BUTCHER;
            default -> VillagerProfession.NITWIT;
        };
    }
}
