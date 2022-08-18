package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.function.Supplier;

public class PacketBuyC2S {
    private final int slot;
    private final BlockPos pos;
    private final int amount;

    public PacketBuyC2S(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        slot = buf.readInt();
        amount = buf.readInt();
    }

    public PacketBuyC2S(BlockPos pos, int slot, int amount) {
        this.pos = pos;
        this.slot = slot;
        this.amount = amount;
    }

    public static void encode(PacketBuyC2S packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.slot);
        buf.writeInt(packet.amount);
    }

    public static void handle(final PacketBuyC2S packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            VendingMachineBlockEntity tile = (VendingMachineBlockEntity)playerEntity.getLevel().getBlockEntity(packet.pos);

            if(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
                tile.buy(packet.slot, packet.amount);

            //tile.buy(packet.slot, packet.amount);
            //DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> openScreen(slot, context.get().getSender()));
        });

        ctx.setPacketHandled(true);
    }
}
