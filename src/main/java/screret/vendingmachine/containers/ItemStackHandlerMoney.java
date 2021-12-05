package screret.vendingmachine.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import screret.vendingmachine.configs.VendingMachineConfig;

import javax.annotation.Nonnull;

public class ItemStackHandlerMoney extends ItemStackHandler {

    public ItemStackHandlerMoney(int size){
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.copy().getItem() == VendingMachineConfig.PAYMENT_ITEM;
    }
}
