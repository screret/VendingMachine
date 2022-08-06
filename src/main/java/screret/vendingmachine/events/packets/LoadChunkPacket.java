package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LoadChunkPacket {
    private final BlockPos pos;
    private long stopTime;

    public LoadChunkPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public LoadChunkPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(LoadChunkPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final LoadChunkPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerLevel level = context.get().getSender().getLevel();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> level.setChunkForced(level.getChunk(packet.pos).getPos().x, level.getChunk(packet.pos).getPos().z, true));
        });

        ctx.setPacketHandled(true);
    }
}
