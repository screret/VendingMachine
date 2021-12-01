package screret.vendingmachine.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemHandlerMoney extends ItemStackHandler {

    public ItemHandlerMoney(int size){
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.copy().getItem() == Items.GOLD_INGOT;
    }
}
