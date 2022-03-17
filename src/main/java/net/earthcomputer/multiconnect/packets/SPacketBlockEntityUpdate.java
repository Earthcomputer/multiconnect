package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;

@MessageVariant
public class SPacketBlockEntityUpdate {
    public CommonTypes.BlockPos pos;
    public byte blockEntityType; // not from the block entity registry
    @Datafix(value = DatafixTypes.BLOCK_ENTITY, preprocess = "preprocessBlockEntityData")
    public NbtCompound data;

    public static void preprocessBlockEntityData(NbtCompound data, @Argument("blockEntityType") byte blockEntityType) {
        BlockEntityType<?> type = switch (blockEntityType) {
            case 1 -> BlockEntityType.MOB_SPAWNER;
            case 2 -> BlockEntityType.COMMAND_BLOCK;
            case 3 -> BlockEntityType.BEACON;
            case 4 -> BlockEntityType.SKULL;
            case 5 -> BlockEntityType.CONDUIT;
            case 6 -> BlockEntityType.BANNER;
            case 7 -> BlockEntityType.STRUCTURE_BLOCK;
            case 8 -> BlockEntityType.END_GATEWAY;
            case 9 -> BlockEntityType.SIGN;
            case 11 -> BlockEntityType.BED;
            case 12 -> BlockEntityType.JIGSAW;
            case 13 -> BlockEntityType.CAMPFIRE;
            case 14 -> BlockEntityType.BEEHIVE;
            default -> null;
        };
        if (type == null || !MultiConnectAPI.instance().doesServerKnow(Registry.BLOCK_ENTITY_TYPE, type)) {
            return;
        }

        data.putString("id", ConnectionInfo.protocolVersion <= Protocols.V1_10 ? Protocol_1_10.getBlockEntityId(type) : String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(type)));
    }
}
