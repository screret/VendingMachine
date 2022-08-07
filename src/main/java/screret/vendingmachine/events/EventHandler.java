package screret.vendingmachine.events;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.capabilities.configs.VendingMachineConfig;
import screret.vendingmachine.items.MoneyItem;
import screret.vendingmachine.tileEntities.VendingMachineTile;

@Mod.EventBusSubscriber(modid = VendingMachine.MODID)
public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void protectedBreak(PlayerInteractEvent.LeftClickBlock event) {
        Block brokeBlock = event.getWorld().getBlockState(event.getPos()).getBlock();

        if (brokeBlock instanceof VendingMachineBlock) {
            TileEntity tile = event.getWorld().getBlockEntity(event.getPos());
            if(tile instanceof VendingMachineTile) {
                if (!((event.getPlayer().getUUID().equals(((VendingMachineTile)tile).owner)) || event.getPlayer().isCreative())) {     //If not Owner (and not in creative) Can't Break
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        CompoundNBT tag = event.getPlayer().getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);

        if(tag.getBoolean("has_logged_in")){
            return;
        }

        if(VendingMachineConfig.GENERAL.moneyAmount.get() > 0){
            CompoundNBT moneyItemTag = new CompoundNBT();
            moneyItemTag.putFloat(MoneyItem.MONEY_VALUE_TAG, MoneyItem.MONEY_VALUES[5]);
            int moneyCount = Math.round(VendingMachineConfig.GENERAL.moneyAmount.get() / MoneyItem.MONEY_VALUES[5]);
            ItemStack stack = new ItemStack(VendingMachineConfig.getPaymentItem(), moneyCount);
            stack.setTag(moneyItemTag);

            event.getPlayer().inventory.add(stack);
        }


        tag.putBoolean("has_logged_in", true);
        event.getPlayer().getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG, tag);
    }
}