package screret.vendingmachine.events.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.function.Supplier;

public class AddPricePacket {
    private final BlockPos pos;
    private final int price;
    private final Item item;

    public AddPricePacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        item = buf.readItem().getItem();
        price = buf.readInt();
    }

    public AddPricePacket(BlockPos pos, Item item, int price) {
        this.pos = pos;
        this.item = item;
        this.price = price;
    }

    public static void encode(AddPricePacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeItem(new ItemStack(packet.item));
        buf.writeInt(packet.price);
    }

    public static void handle(final AddPricePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity playerEntity = context.get().getSender();

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            VendingMachineTile tile = (VendingMachineTile)playerEntity.getLevel().getBlockEntity(packet.pos);

            tile.priceHashMap.put(packet.item, packet.price);
        });

        ctx.setPacketHandled(true);
    }
}
