package screret.vendingmachine.events.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.function.Supplier;

public class OpenVenderGUIPacket {
    private final BlockPos pos;
    private final boolean isMainWindow;

    public OpenVenderGUIPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        isMainWindow = buf.readBoolean();
    }

    public OpenVenderGUIPacket(BlockPos pos, boolean isMainWindow) {
        this.pos = pos;
        this.isMainWindow = isMainWindow;
    }

    public static void encode(OpenVenderGUIPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.isMainWindow);
    }

    public static void handle(final OpenVenderGUIPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer playerEntity = context.get().getSender();

        BlockEntity tile = playerEntity.getLevel().getBlockEntity(packet.pos);

        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if(!packet.isMainWindow && tile instanceof VendingMachineBlockEntity _tile){
                NetworkHooks.openGui(playerEntity, _tile.priceEditorContainerProvider, packet.pos);
            }
            else if(tile instanceof VendingMachineBlockEntity _tile){
                NetworkHooks.openGui(playerEntity, _tile, packet.pos);
            }
        });

        ctx.setPacketHandled(true);
    }
}
