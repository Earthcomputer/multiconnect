package net.earthcomputer.multiconnect.protocols.v1_12_2;

import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.transformer.CustomPayload;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.*;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.*;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Protocol_1_12_2 extends Protocol_1_13 {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, CommandSuggestionsS2CPacket.class);
        insertAfter(packets, DifficultyS2CPacket.class, PacketInfo.of(CommandSuggestionsS2CPacket.class, CommandSuggestionsS2CPacket::new));
        remove(packets, CommandTreeS2CPacket.class);
        remove(packets, TagQueryResponseS2CPacket.class);
        remove(packets, LookAtS2CPacket.class);
        remove(packets, StopSoundS2CPacket.class);
        remove(packets, SynchronizeRecipesS2CPacket.class);
        remove(packets, SynchronizeTagsS2CPacket.class);
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, RequestCommandCompletionsC2SPacket.class);
        insertAfter(packets, TeleportConfirmC2SPacket.class, PacketInfo.of(RequestCommandCompletionsC2SPacket.class, RequestCommandCompletionsC2SPacket::new));
        remove(packets, QueryBlockNbtC2SPacket.class);
        remove(packets, BookUpdateC2SPacket.class);
        remove(packets, QueryEntityNbtC2SPacket.class);
        remove(packets, PickFromInventoryC2SPacket.class);
        remove(packets, RenameItemC2SPacket.class);
        remove(packets, SelectVillagerTradeC2SPacket.class);
        remove(packets, UpdateBeaconC2SPacket.class);
        remove(packets, UpdateCommandBlockC2SPacket.class);
        remove(packets, UpdateCommandBlockMinecartC2SPacket.class);
        remove(packets, UpdateStructureBlockC2SPacket.class);
        remove(packets, CustomPayloadC2SPacket.class);
        insertAfter(packets, GuiCloseC2SPacket.class, PacketInfo.of(CustomPayloadC2SPacket_1_12_2.class, CustomPayloadC2SPacket_1_12_2::new));
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (!super.onSendPacket(packet))
            return false;
        if (packet.getClass() == QueryBlockNbtC2SPacket.class || packet.getClass() == QueryEntityNbtC2SPacket.class) {
            return false;
        }
        ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();
        assert connection != null;
        if (packet.getClass() == CustomPayloadC2SPacket.class) {
            ICustomPaylaodC2SPacket customPayload = (ICustomPaylaodC2SPacket) packet;
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2(customPayload.getChannel().toString(), customPayload.getData()));
            return false;
        }
        if (packet.getClass() == BookUpdateC2SPacket.class) {
            BookUpdateC2SPacket bookUpdate = (BookUpdateC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeItemStack(bookUpdate.getBook());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2(bookUpdate.wasSigned() ? "MC|BSign" : "MC|BEdit", buf));
            return false;
        }
        if (packet.getClass() == RenameItemC2SPacket.class) {
            RenameItemC2SPacket renameItem = (RenameItemC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeString(renameItem.getItemName(), 32767);
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|ItemName", buf));
            return false;
        }
        if (packet.getClass() == SelectVillagerTradeC2SPacket.class) {
            SelectVillagerTradeC2SPacket selectTrade = (SelectVillagerTradeC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(selectTrade.method_12431());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|TrSel", buf));
            return false;
        }
        if (packet.getClass() == UpdateBeaconC2SPacket.class) {
            UpdateBeaconC2SPacket updateBeacon = (UpdateBeaconC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(updateBeacon.getPrimaryEffectId());
            buf.writeInt(updateBeacon.getSecondaryEffectId());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|Beacon", buf));
            return false;
        }
        if (packet.getClass() == UpdateCommandBlockC2SPacket.class) {
            UpdateCommandBlockC2SPacket updateCmdBlock = (UpdateCommandBlockC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(updateCmdBlock.getBlockPos().getX());
            buf.writeInt(updateCmdBlock.getBlockPos().getY());
            buf.writeInt(updateCmdBlock.getBlockPos().getZ());
            buf.writeString(updateCmdBlock.getCommand());
            buf.writeBoolean(updateCmdBlock.shouldTrackOutput());
            switch (updateCmdBlock.getType()) {
                case AUTO:
                    buf.writeString("AUTO");
                    break;
                case REDSTONE:
                    buf.writeString("REDSTONE");
                    break;
                case SEQUENCE:
                    buf.writeString("SEQUENCE");
                    break;
                default:
                    LOGGER.error("Unknown command block type: " + updateCmdBlock.getType());
                    return false;
            }
            buf.writeBoolean(updateCmdBlock.isConditional());
            buf.writeBoolean(updateCmdBlock.isAlwaysActive());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|AutoCmd", buf));
            return false;
        }
        if (packet.getClass() == UpdateCommandBlockMinecartC2SPacket.class) {
            UpdateCommandBlockMinecartC2SPacket updateCmdMinecart = (UpdateCommandBlockMinecartC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeByte(1); // command block type (minecart)
            buf.writeInt(((ICommandBlockMinecartC2SPacket) updateCmdMinecart).getEntityId());
            buf.writeString(updateCmdMinecart.getCommand());
            buf.writeBoolean(updateCmdMinecart.shouldTrackOutput());
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|AdvCmd", buf));
            return false;
        }
        if (packet.getClass() == UpdateStructureBlockC2SPacket.class) {
            UpdateStructureBlockC2SPacket updateStructBlock = (UpdateStructureBlockC2SPacket) packet;
            TransformerByteBuf buf = new TransformerByteBuf(Unpooled.buffer(), null);
            buf.writeTopLevelType(CustomPayload.class);
            buf.writeInt(updateStructBlock.getPos().getX());
            buf.writeInt(updateStructBlock.getPos().getY());
            buf.writeInt(updateStructBlock.getPos().getZ());
            switch (updateStructBlock.getAction()) {
                case UPDATE_DATA:
                    buf.writeByte(1);
                    break;
                case SAVE_AREA:
                    buf.writeByte(2);
                    break;
                case LOAD_AREA:
                    buf.writeByte(3);
                    break;
                case SCAN_AREA:
                    buf.writeByte(4);
                    break;
                default:
                    LOGGER.error("Unknown structure block action: " + updateStructBlock.getAction());
                    return false;
            }
            switch (updateStructBlock.getMode()) {
                case SAVE:
                    buf.writeString("SAVE");
                    break;
                case LOAD:
                     buf.writeString("LOAD");
                     break;
                case CORNER:
                    buf.writeString("CORNER");
                    break;
                case DATA:
                    buf.writeString("DATA");
                    break;
                default:
                    LOGGER.error("Unknown structure block mode: " + updateStructBlock.getMode());
                    return false;
            }
            buf.writeString(updateStructBlock.getStructureName());
            buf.writeInt(updateStructBlock.getOffset().getX());
            buf.writeInt(updateStructBlock.getOffset().getY());
            buf.writeInt(updateStructBlock.getOffset().getZ());
            buf.writeInt(updateStructBlock.getSize().getX());
            buf.writeInt(updateStructBlock.getSize().getY());
            buf.writeInt(updateStructBlock.getSize().getZ());
            switch (updateStructBlock.getMirror()) {
                case NONE:
                    buf.writeString("NONE");
                    break;
                case LEFT_RIGHT:
                    buf.writeString("LEFT_RIGHT");
                    break;
                case FRONT_BACK:
                    buf.writeString("FRONT_BACK");
                    break;
                default:
                    LOGGER.error("Unknown mirror: " + updateStructBlock.getMirror());
                    return false;
            }
            switch (updateStructBlock.getRotation()) {
                case NONE:
                    buf.writeString("NONE");
                    break;
                case CLOCKWISE_90:
                    buf.writeString("CLOCKWISE_90");
                    break;
                case CLOCKWISE_180:
                    buf.writeString("CLOCKWISE_180");
                    break;
                case COUNTERCLOCKWISE_90:
                    buf.writeString("COUNTERCLOCKWISE_90");
                    break;
                default:
                    LOGGER.error("Unknown rotation: " + updateStructBlock.getRotation());
                    return false;
            }
            buf.writeString(updateStructBlock.getMetadata());
            buf.writeBoolean(updateStructBlock.getIgnoreEntities());
            buf.writeBoolean(updateStructBlock.shouldShowAir());
            buf.writeBoolean(updateStructBlock.shouldShowBoundingBox());
            buf.writeFloat(updateStructBlock.getIntegrity());
            buf.writeVarLong(updateStructBlock.getSeed());
            // have fun with all that, server!
            connection.sendPacket(new CustomPayloadC2SPacket_1_12_2("MC|Struct", buf));
            return false;
        }

        return true;
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        super.modifyRegistry(registry);

        // just fucking nuke them all, it's the flattening after all
        if (registry == Registry.BLOCK) {
            Blocks_1_12_2.registerBlocks((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            Items_1_12_2.registerItems((ISimpleRegistry<Item>) registry);
        }
    }
}
