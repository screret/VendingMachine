package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class ChangePricePacket {
    private final BlockPos pos;
    private final int price;
    private final ItemStack item;
    private final boolean addOrRemove;

    public ChangePricePacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        item = buf.readItem();
        price = buf.readInt();
        addOrRemove = buf.readBoolean();
    }

    public ChangePricePacket(BlockPos pos, ItemStack item, int price, boolean addOrRemove) {
        this.pos = pos;
        this.item = item;
        this.price = price;
        this.addOrRemove = addOrRemove;
    }

    public static void encode(ChangePricePacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeItem(packet.item);
        buf.writeInt(packet.price);
        buf.writeBoolean(packet.addOrRemove);
    }

    public static void handle(final ChangePricePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            TileEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineTile){
                VendingMachineTile finalTile = (VendingMachineTile) tile;
                if(packet.addOrRemove){
                    DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> add(finalTile, packet.item, packet.price));
                }else {
                    DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> remove(finalTile, packet.item));
                }
            }
            //tile.addPrice(packet.item, packet.price);
        });

        ctx.setPacketHandled(true);
    }

    static DistExecutor.SafeRunnable add(VendingMachineTile tile, ItemStack item, int price){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                tile.addPrice(item, price);
            }
        };
    }
    static DistExecutor.SafeRunnable remove(VendingMachineTile tile, ItemStack item){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                tile.removePrice(item);
            }
        };
    }
}
