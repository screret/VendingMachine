package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketLoadChunkC2S {
    private final BlockPos pos;
    private long stopTime;

    public PacketLoadChunkC2S(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public PacketLoadChunkC2S(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(PacketLoadChunkC2S packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final PacketLoadChunkC2S packet, Supplier<NetworkEvent.Context> context) {
        ServerLevel level = context.get().getSender().getLevel();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> level.setChunkForced(level.getChunk(packet.pos).getPos().x, level.getChunk(packet.pos).getPos().z, true));
        });

        ctx.setPacketHandled(true);
    }
}
