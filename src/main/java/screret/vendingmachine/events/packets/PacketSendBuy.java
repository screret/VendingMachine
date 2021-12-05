package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class PacketSendBuy {
    private final int slot;
    private final BlockPos pos;
    private final int amount;

    public PacketSendBuy(PacketBuffer buf) {
        pos = buf.readBlockPos();
        slot = buf.readInt();
        amount = buf.readInt();
    }

    public PacketSendBuy(BlockPos pos, int slot, int amount) {
        this.pos = pos;
        this.slot = slot;
        this.amount = amount;
    }

    public static void encode(PacketSendBuy packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.slot);
        buf.writeInt(packet.amount);
    }

    public static void handle(final PacketSendBuy packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            VendingMachineTile tile = (VendingMachineTile)playerEntity.getLevel().getBlockEntity(packet.pos);

           tile.buy(packet.slot, packet.amount);
            //DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> openScreen(slot, context.get().getSender()));
        });

        ctx.setPacketHandled(true);
    }
}
