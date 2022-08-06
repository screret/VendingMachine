package screret.vendingmachine.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.items.MoneyItem;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

@Mod.EventBusSubscriber(modid = VendingMachine.MODID)
public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void protectedBreak(PlayerInteractEvent.LeftClickBlock event) {
        Block brokeBlock = event.getLevel().getBlockState(event.getPos()).getBlock();

        if (brokeBlock instanceof VendingMachineBlock) {
            BlockEntity tile = event.getLevel().getBlockEntity(event.getPos());
            if(tile instanceof VendingMachineBlockEntity) {
                if (!((event.getEntity().getUUID().equals(((VendingMachineBlockEntity)tile).owner)) || event.getEntity().isCreative())) {     //If not Owner (and not in creative) Can't Break
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag tag = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

        if(tag.getBoolean("has_logged_in")){
            return;
        }

        if(VendingMachineConfig.GENERAL.startMoney.get() > 0){
            CompoundTag moneyItemTag = new CompoundTag();
            moneyItemTag.putFloat(MoneyItem.MONEY_VALUE_TAG, MoneyItem.MONEY_VALUES[6]);
            int moneyCount = Math.round(VendingMachineConfig.GENERAL.startMoney.get() / MoneyItem.MONEY_VALUES[6]);
            ItemStack stack = new ItemStack(VendingMachineConfig.getPaymentItem(), moneyCount);
            stack.setTag(moneyItemTag);

            event.getEntity().getInventory().add(stack);
        }


        tag.putBoolean("has_logged_in", true);
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
    }

    @SubscribeEvent
    public static void configLoaded(final ModConfigEvent.Loading event){
        if(event.getConfig().getModId().equals(VendingMachine.MODID)){
            VendingMachine.LOGGER.debug("Loading Vending Machine Configs");
            VendingMachineConfig.getPaymentItem();
            VendingMachineConfig.getDecryptedPrices();
        }
    }
}