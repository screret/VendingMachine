package screret.vendingmachine.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.items.MoneyItem;
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

    @SubscribeEvent
    public static void playerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag tag = event.getPlayer().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

        if(tag.getBoolean("has_logged_in")){
            return;
        }

        if(VendingMachineConfig.GENERAL.moneyAmount.get() > 0){
            CompoundTag moneyItemTag = new CompoundTag();
            moneyItemTag.putFloat(MoneyItem.MONEY_VALUE_TAG, MoneyItem.MONEY_VALUES[6]);
            int moneyCount = Math.round(VendingMachineConfig.GENERAL.moneyAmount.get() / MoneyItem.MONEY_VALUES[6]);
            ItemStack stack = new ItemStack(VendingMachineConfig.getPaymentItem(), moneyCount);
            stack.setTag(moneyItemTag);

            event.getPlayer().getInventory().add(stack);
        }


        tag.putBoolean("has_logged_in", true);
        event.getPlayer().getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
    }
}