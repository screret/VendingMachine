package screret.vendingmachine.events;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void protectedBreak(PlayerInteractEvent.LeftClickBlock e) {
        Block brokeBlock = e.getWorld().getBlockState(e.getPos()).getBlock();

        if (brokeBlock instanceof VendingMachineBlock) {
            BlockEntity tile = e.getWorld().getBlockEntity(e.getPos());
            if(tile instanceof VendingMachineTile) {
                if (!((e.getPlayer().getUUID().equals(((VendingMachineTile)tile).owner)) || e.getPlayer().isCreative())) {     //If not Owner (and not in creative) Can't Break
                    e.setCanceled(true);
                }
            }
        }
    }
}