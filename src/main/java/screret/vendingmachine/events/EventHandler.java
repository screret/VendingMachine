package screret.vendingmachine.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
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
import screret.vendingmachine.containers.gui.VenderBlockScreen;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.ArrayList;

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

        if(tag.contains("has_logged_in") && tag.getBoolean("has_logged_in")){
            return;
        }

        spawnMoney(event.getEntity(), (float)(double)VendingMachineConfig.GENERAL.startMoney.get());

        tag.putBoolean("has_logged_in", true);
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
    }

    private static void spawnMoney(Player player, float money){
        if(!player.getLevel().isClientSide() && money > 0) {
            float amount = money;
            float[] moneyOut = new float[8];

            moneyOut[7] = (int)(amount / 1000);
            amount -= moneyOut[7] * 1000;

            moneyOut[6] = (int)(amount / 100);
            amount -= moneyOut[6] * 100;

            moneyOut[5] = (int)(amount / 50);
            amount -= moneyOut[5] * 50;

            moneyOut[4] = (int)(amount / 20);
            amount -= moneyOut[4] * 20;

            moneyOut[3] = (int)(amount / 10);
            amount -= moneyOut[3] * 10;

            moneyOut[2] = (int)(amount / 5);
            amount -= moneyOut[2] * 5;

            moneyOut[1] = (int)(amount / 2);
            amount -= moneyOut[1] * 2;

            moneyOut[0] = amount;

            for(int i = 0; i < moneyOut.length; ++i){
                boolean check = moneyOut[i] != 0;

                if(check){
                    ArrayList<ItemStack> stacks = new ArrayList<>();

                    var itemStack = new ItemStack(Registration.MONEY.get());
                    stacks.add(itemStack);

                    MoneyItem.setMoneyValue(itemStack, MoneyItem.MONEY_VALUES[i]);
                    itemStack.setCount((int)moneyOut[i]);

                    while(itemStack.getCount() > itemStack.getMaxStackSize()){
                        stacks.add(itemStack.split(itemStack.getMaxStackSize()));
                    }

                    boolean playerInGui = false;
                    if(player != null) playerInGui = true;

                    if(playerInGui){
                        Inventory playerInv = player.getInventory();
                        boolean placed = false;

                        searchLoop:
                        for(int j = 0; j < playerInv.items.size(); ++j){
                            ItemStack playerStack = playerInv.items.get(j);
                            for (var stack : stacks){
                                if(ItemStack.isSameItemSameTags(stack, playerStack)){
                                    if(playerStack.getCount() + stack.getCount() <= playerStack.getMaxStackSize()){
                                        playerStack.setCount(playerStack.getCount() + stack.getCount());
                                        stack = ItemStack.EMPTY;
                                        placed = true;
                                        break searchLoop;
                                    }
                                }
                            }
                        }

                        if(!placed){
                            for(var stack : stacks){
                                if(playerInv.getFreeSlot() != -1){
                                    playerInv.add(stack);
                                    stack = ItemStack.EMPTY;
                                }else{
                                    playerInGui = false;
                                }
                            }
                        }
                    }
                    if (!playerInGui) {       //If no room, spawn
                        BlockPos pos = player.getOnPos();
                        for(var stack : stacks){
                            Containers.dropItemStack(player.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack);
                        }
                    }
                }
            }
        }
    }
}