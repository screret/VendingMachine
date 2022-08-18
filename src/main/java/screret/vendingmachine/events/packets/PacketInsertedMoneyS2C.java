package screret.vendingmachine.events.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;
import screret.vendingmachine.init.Registration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class PacketInsertedMoneyS2C {

    private final float amount;
    private final BlockPos pos;

    public PacketInsertedMoneyS2C(FriendlyByteBuf buf){
        this.amount = buf.readFloat();
        this.pos = buf.readBlockPos();
    }

    public PacketInsertedMoneyS2C(float amount, BlockPos pos){
        this.amount = amount;
        this.pos = pos;
    }

    public static void encode(PacketInsertedMoneyS2C packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.amount);
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(final PacketInsertedMoneyS2C packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
                Optional<VendingMachineBlockEntity> maybeBe = Minecraft.getInstance().level.getBlockEntity(packet.pos, Registration.VENDER_TILE.get());
                maybeBe.ifPresent(be -> be.currentPlayerInsertedMoney = packet.amount);
                return null;
            });
        });

        ctx.setPacketHandled(true);
    }
}
