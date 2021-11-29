package screret.vendingmachine.events.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.SlotItemHandler;
import screret.vendingmachine.containers.OwnedStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSendBuy {
    private static SlotItemHandler slot;

    public PacketSendBuy(PacketBuffer buf) {
        ((OwnedStackHandler)slot.getItemHandler()).deserializeNBT(buf.readAnySizeNbt());
    }

    public PacketSendBuy(SlotItemHandler slot) {
        this.slot = slot;
    }

    public static void encode(PacketSendBuy packet, PacketBuffer buf) {
        buf.writeNbt(((OwnedStackHandler)packet.slot.getItemHandler()).serializeNBT());
    }

    public static void handle(final PacketSendBuy packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            //((VenderBlockContainer)ctx.getSender().containerMenu).buy(slot);
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> openScreen(slot, context.get().getSender()));
        });

        ctx.setPacketHandled(true);
    }

    public static DistExecutor.SafeRunnable openScreen(SlotItemHandler slot, ServerPlayerEntity player) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ((VenderBlockContainer)player.containerMenu).buy(slot);
            }
        };
    }
}
