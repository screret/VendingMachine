package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.function.Supplier;

public class ChangePricePacket {
    private final BlockPos pos;
    private final int price;
    private final ItemStack item;
    private final boolean add;

    public ChangePricePacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        add = buf.readBoolean();
        item = buf.readItem();
        price = buf.readInt();
    }

    public ChangePricePacket(BlockPos pos, ItemStack item, int price, boolean add) {
        this.pos = pos;
        this.item = item;
        this.price = price;
        this.add = add;
    }

    public static void encode(ChangePricePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.add);
        buf.writeItem(packet.item);
        buf.writeInt(packet.price);
    }

    public static void handle(final ChangePricePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineBlockEntity finalTile){
                if(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER){
                    if(packet.add){
                        finalTile.addPrice(packet.item, packet.price);
                        //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> finalTile.addPrice(packet.item, packet.price));
                    } else {
                        finalTile.removePrice(packet.item);
                        //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> finalTile.removePrice(packet.item));
                    }
                }
            }
        });

        ctx.setPacketHandled(true);
    }
}
