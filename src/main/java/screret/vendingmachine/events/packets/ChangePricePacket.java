package screret.vendingmachine.events.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.objectweb.asm.commons.SerialVersionUIDAdder;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class ChangePricePacket {
    private final BlockPos pos;
    private final int price;
    private final ItemStack item;
    private final boolean add;

    public ChangePricePacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        item = buf.readItem();
        price = buf.readInt();
        add = buf.readBoolean();
    }

    public ChangePricePacket(BlockPos pos, ItemStack item, int price, boolean add) {
        this.pos = pos;
        this.item = item;
        this.price = price;
        this.add = add;
    }

    public static void encode(ChangePricePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeItem(packet.item);
        buf.writeInt(packet.price);
        buf.writeBoolean(packet.add);
    }

    public static void handle(final ChangePricePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

            if(tile instanceof VendingMachineTile){
                VendingMachineTile finalTile = (VendingMachineTile) tile;
                if(packet.add){
                    DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> finalTile.addPrice(packet.item, packet.price));
                } else {
                    DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> finalTile.removePrice(packet.item));
                }
            }
        });

        ctx.setPacketHandled(true);
    }
}
