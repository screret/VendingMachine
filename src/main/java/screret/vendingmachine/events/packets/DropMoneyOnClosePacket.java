package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.function.Supplier;

public class DropMoneyOnClosePacket {
    private final BlockPos pos;

    public DropMoneyOnClosePacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public DropMoneyOnClosePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(DropMoneyOnClosePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final DropMoneyOnClosePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineBlockEntity){
                VendingMachineBlockEntity finalTile = (VendingMachineBlockEntity) tile;
                DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> finalTile::dropMoney);
            }
        });

        ctx.setPacketHandled(true);
    }
}
