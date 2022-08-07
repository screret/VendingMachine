package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.core.jmx.Server;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketAllowItemTake {
    private final UUID user;
    private final boolean value;
    private final BlockPos pos;

    public PacketAllowItemTake(PacketBuffer buf) {
        user = buf.readUUID();
        value = buf.readBoolean();
        pos = buf.readBlockPos();
    }

    public PacketAllowItemTake(BlockPos pos, UUID player, boolean value) {
        this.user = player;
        this.value = value;
        this.pos = pos;
    }

    public static void encode(PacketAllowItemTake packet, PacketBuffer buf) {
        buf.writeUUID(packet.user);
        buf.writeBoolean(packet.value);
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final PacketAllowItemTake packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            TileEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineTile){
                ((VendingMachineTile) tile).container.buyTestMode_REMOVE_LATER = packet.value;
                ((VendingMachineTile) tile).container.checkPlayerAllowedToChangeInv(packet.user);
            }
        });

        ctx.setPacketHandled(true);
    }
}
