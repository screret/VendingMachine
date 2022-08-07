package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class DropMoneyOnClosePacket {
    private final BlockPos pos;


    public DropMoneyOnClosePacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public DropMoneyOnClosePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(DropMoneyOnClosePacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final DropMoneyOnClosePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            TileEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineTile){
                VendingMachineTile finalTile = (VendingMachineTile) tile;
                DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> finalTile::dropMoney);
            }
        });

        ctx.setPacketHandled(true);
    }
}
