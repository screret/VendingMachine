package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;
import screret.vendingmachine.init.Registration;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketCashOutC2S {
    private final BlockPos pos;

    public PacketCashOutC2S(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public PacketCashOutC2S(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(PacketCashOutC2S packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final PacketCashOutC2S packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Optional<VendingMachineBlockEntity> tile = playerEntity.getLevel().getBlockEntity(packet.pos, Registration.VENDER_TILE.get());

            if(tile.isPresent() && Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER){
                tile.get().dropMoney();
            }

        });

        ctx.setPacketHandled(true);
    }
}
