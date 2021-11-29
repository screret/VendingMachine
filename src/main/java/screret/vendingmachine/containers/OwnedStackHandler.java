package screret.vendingmachine.containers;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class OwnedStackHandler extends ItemStackHandler {

    //public boolean isAllowedToTakeItems = true;

    public OwnedStackHandler(int size){
        super(size);
        //this.isAllowedToTakeItems = isAllowedToTakeItems;
    }

    /*@Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        //LOGGER.info("Current player who has the GUI open is the owner: " + isAllowedToTakeItems);
        return isAllowedToTakeItems;
    }*/

    @Override
    public int getSlotLimit(int slot) {
        return 1024;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return 1024;
    }

    /*@Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(!isAllowedToTakeItems) {
            return ItemStack.EMPTY;
        } else {
            return super.extractItem(slot, amount, simulate);
        }
    }*/

    @Override
    public CompoundNBT serializeNBT()
    {
        ListNBT nbtTagList= new ListNBT();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putInt("Slot", i);
                itemTag.putInt("SizeSpecial", stacks.get(i).getCount());
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        setSize(nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundNBT itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, ItemStack.of(itemTags));
                stacks.get(slot).setCount(itemTags.getInt("SizeSpecial"));
            }
        }
        onLoad();
    }
}
