package screret.vendingmachine.containers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemStackHandlerOutput extends ItemStackHandler {

    public ItemStackHandlerOutput(int size){
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 192;
    }
}
