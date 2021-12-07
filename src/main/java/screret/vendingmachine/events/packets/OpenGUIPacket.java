package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class OpenGUIPacket {
    private final BlockPos pos;
    private final boolean isMainTab;

    public OpenGUIPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        isMainTab = buf.readBoolean();
    }

    public OpenGUIPacket(BlockPos pos, boolean isMainTab) {
        this.pos = pos;
        this.isMainTab = isMainTab;
    }

    public static void encode(OpenGUIPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.isMainTab);
    }

    public static void handle(final OpenGUIPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineTile){
                if(packet.isMainTab){
                    NetworkHooks.openGui(playerEntity, (VendingMachineTile)tile, packet.pos);
                }else {
                    NetworkHooks.openGui(playerEntity, ((VendingMachineTile)tile).priceEditorContainerProvider, packet.pos);
                }
            }
        });

        ctx.setPacketHandled(true);
    }
}
