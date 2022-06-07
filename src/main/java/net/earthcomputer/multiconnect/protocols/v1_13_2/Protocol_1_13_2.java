package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_13_2.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.entity.*;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

import java.util.*;

public class Protocol_1_13_2 extends Protocol_1_14 {
    public static final TrackedData<Integer> OLD_FIREWORK_SHOOTER = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_VILLAGER_PROFESSION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Byte> OLD_ILLAGER_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<Boolean> OLD_SKELETON_ATTACKING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> OLD_ZOMBIE_ATTACKING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> OLD_ZOMBIE_VILLAGER_PROFESSION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_HORSE_ARMOR = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    public static final Key<byte[][]> BLOCK_LIGHT_KEY = Key.create("blockLight");
    public static final Key<byte[][]> SKY_LIGHT_KEY = Key.create("skyLight");

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == ZombieEntity.class && data == ZombieEntityAccessor.getConvertingInWater()) {
            DataTrackerManager.registerOldTrackedData(ZombieEntity.class, OLD_ZOMBIE_ATTACKING, false, MobEntity::setAttacking);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getPose())
            return false;
        if (clazz == EyeOfEnderEntity.class && data == EnderEyeEntityAccessor.getItem())
            return false;
        if (clazz == FireworkRocketEntity.class) {
            TrackedData<OptionalInt> fireworkShooter = FireworkEntityAccessor.getShooter();
            if (data == fireworkShooter) {
                DataTrackerManager.registerOldTrackedData(FireworkRocketEntity.class, OLD_FIREWORK_SHOOTER, 0,
                        (entity, val) -> entity.getDataTracker().set(fireworkShooter, val <= 0 ? OptionalInt.empty() : OptionalInt.of(val)));
                return false;
            }
            if (data == FireworkEntityAccessor.getShotAtAngle())
                return false;
        }
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getSleepingPosition())
            return false;
        if (clazz == VillagerEntity.class) {
            TrackedData<VillagerData> villagerData = VillagerEntityAccessor.getVillagerData();
            if (data == villagerData) {
                DataTrackerManager.registerOldTrackedData(VillagerEntity.class, OLD_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getDataTracker().set(villagerData, entity.getVillagerData().withProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == ZombieVillagerEntity.class) {
            TrackedData<VillagerData> villagerData = ZombieVillagerEntityAccessor.getVillagerData();
            if (data == villagerData) {
                DataTrackerManager.registerOldTrackedData(ZombieVillagerEntity.class, OLD_ZOMBIE_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getDataTracker().set(villagerData, entity.getVillagerData().withProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == MooshroomEntity.class && data == MooshroomEntityAccessor.getType())
            return false;
        if (clazz == CatEntity.class) {
            if (data == CatEntityAccessor.getInSleepingPose()
                || data == CatEntityAccessor.getHeadDown()
                || data == CatEntityAccessor.getCollarColor())
                return false;
        }
        if (clazz == PersistentProjectileEntity.class && data == ProjectileEntityAccessor.getPierceLevel())
            return false;
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void postEntityDataRegister(Class<? extends Entity> clazz) {
        if (clazz == IllagerEntity.class)
            DataTrackerManager.registerOldTrackedData(IllagerEntity.class, OLD_ILLAGER_FLAGS, (byte)0,
                    (entity, val) -> entity.setAttacking((val & 1) != 0));
        if (clazz == AbstractSkeletonEntity.class)
            DataTrackerManager.registerOldTrackedData(AbstractSkeletonEntity.class, OLD_SKELETON_ATTACKING, false, MobEntity::setAttacking);
        if (clazz == HorseEntity.class)
            DataTrackerManager.registerOldTrackedData(HorseEntity.class, OLD_HORSE_ARMOR, 0, (entity, val) -> {
                switch (val) {
                    case 1 -> entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_HORSE_ARMOR));
                    case 2 -> entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_HORSE_ARMOR));
                    case 3 -> entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
                    default -> entity.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
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
