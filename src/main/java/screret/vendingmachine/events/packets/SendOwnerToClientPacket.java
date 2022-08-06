package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

public class SendOwnerToClientPacket {
    private final UUID owner;
    private final BlockPos pos;

    public SendOwnerToClientPacket(FriendlyByteBuf buf) {
        owner = buf.readUUID();
        pos = buf.readBlockPos();
    }

    public SendOwnerToClientPacket(BlockPos pos, UUID owner) {
        this.pos = pos;
        this.owner = owner;
    }

    public static void encode(SendOwnerToClientPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUUID(packet.owner);
    }

    public static void handle(final SendOwnerToClientPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineBlockEntity finalTile && playerEntity.getLevel().isLoaded(packet.pos)){
                DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> finalTile.owner = packet.owner);
            }
        });

        ctx.setPacketHandled(true);
    }
}
