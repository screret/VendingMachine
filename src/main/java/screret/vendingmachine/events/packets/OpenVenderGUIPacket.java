package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class OpenVenderGUIPacket {
    private final BlockPos pos;
    private final boolean isMainWindow;

    public OpenVenderGUIPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        isMainWindow = buf.readBoolean();
    }

    public OpenVenderGUIPacket(BlockPos pos, boolean isMainWindow) {
        this.pos = pos;
        this.isMainWindow = isMainWindow;
    }

    public static void encode(OpenVenderGUIPacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.isMainWindow);
    }

    public static void handle(final OpenVenderGUIPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        TileEntity tile = context.get().getSender().getLevel().getBlockEntity(packet.pos);

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if(packet.isMainWindow){
                NetworkHooks.openGui(playerEntity, ((VendingMachineTile)tile).priceEditorContainerProvider, packet.pos);
            }
            else if(tile instanceof VendingMachineTile){
                NetworkHooks.openGui(playerEntity, (VendingMachineTile)tile, packet.pos);
            }
        });

        ctx.setPacketHandled(true);
    }
}
