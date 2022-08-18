package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;
import screret.vendingmachine.init.Registration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class OpenGuiPacketC2S {
    private final BlockPos pos;
    private final boolean isMainWindow;

    public OpenGuiPacketC2S(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        isMainWindow = buf.readBoolean();
    }

    public OpenGuiPacketC2S(BlockPos pos, boolean isMainWindow) {
        this.pos = pos;
        this.isMainWindow = isMainWindow;
    }

    public static void encode(OpenGuiPacketC2S packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.isMainWindow);
    }

    public static void handle(final OpenGuiPacketC2S packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();
        NetworkEvent.Context ctx = context.get();

        AtomicReference<Optional<VendingMachineBlockEntity>> tile = new AtomicReference<>();

        ctx.enqueueWork(() -> tile.set(playerEntity.getLevel().getBlockEntity(packet.pos, Registration.VENDER_TILE.get())));

        ctx.enqueueWork(() -> {
            if(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && tile.get().isPresent()){
                if (packet.isMainWindow) {
                    NetworkHooks.openGui(playerEntity, tile.get().get(), packet.pos);
                } else {
                    NetworkHooks.openGui(playerEntity, tile.get().get().priceEditorContainerProvider, packet.pos);
                }
            }
        });

        ctx.setPacketHandled(true);
    }
}
