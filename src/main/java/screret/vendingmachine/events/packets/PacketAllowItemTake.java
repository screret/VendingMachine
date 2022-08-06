package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketAllowItemTake {
    private final UUID user;
    private final boolean value;
    private final BlockPos pos;

    public PacketAllowItemTake(FriendlyByteBuf buf) {
        user = buf.readUUID();
        value = buf.readBoolean();
        pos = buf.readBlockPos();
    }

    public PacketAllowItemTake(BlockPos pos, UUID player, boolean value) {
        this.user = player;
        this.value = value;
        this.pos = pos;
    }

    public static void encode(PacketAllowItemTake packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.user);
        buf.writeBoolean(packet.value);
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final PacketAllowItemTake packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineBlockEntity){
                ((VendingMachineBlockEntity) tile).container.checkPlayerAllowedToChangeInv(packet.user);
            }
        });

        ctx.setPacketHandled(true);
    }
}
