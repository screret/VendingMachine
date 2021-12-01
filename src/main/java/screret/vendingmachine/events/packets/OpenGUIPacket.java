package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.UUID;
import java.util.function.Supplier;

public class OpenGUIPacket {
    private final BlockPos pos;
    private final boolean isMainTab;

    public OpenGUIPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        isMainTab = buf.readBoolean();
    }

    public OpenGUIPacket(BlockPos pos, boolean isMainTab) {
        this.pos = pos;
        this.isMainTab = isMainTab;
    }

    public static void encode(OpenGUIPacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.isMainTab);
    }

    public static void handle(final OpenGUIPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            TileEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

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
