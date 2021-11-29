package screret.vendingmachine.events;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void protectedBreak(PlayerInteractEvent.LeftClickBlock e) {
        Block brokeBlock = e.getWorld().getBlockState(e.getPos()).getBlock();

        if (brokeBlock instanceof VendingMachineBlock) {
            TileEntity tile = e.getWorld().getBlockEntity(e.getPos());
            if(tile instanceof VendingMachineTile) {
                if (!((e.getPlayer().getUUID().equals(((VendingMachineTile)tile).owner)) || e.getPlayer().isCreative())) {     //If not Owner (and not in creative) Can't Break
                    e.setCanceled(true);
                }
            }
        }
    }
}