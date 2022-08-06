package screret.vendingmachine.containers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import screret.vendingmachine.configs.VendingMachineConfig;

import javax.annotation.Nonnull;

public class LargeStackHandler extends ItemStackHandler {

    private BlockEntity blockEntity;

    public LargeStackHandler(int size, BlockEntity be){
        super(size);
        this.blockEntity = be;
        //this.isAllowedToTakeItems = isAllowedToTakeItems;
    }

    @Override
    public int getSlotLimit(int slot) {
        return VendingMachineConfig.GENERAL.maxVenderStack.get();
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return VendingMachineConfig.GENERAL.maxVenderStack.get();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            }
            else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        if (existing.getCount() <= amount)
        {
            if (!simulate)
            {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            else
            {
                return existing.copy();
            }
        }
        else
        {
            if (!simulate)
            {
                this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - amount));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, amount);
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        blockEntity.setChanged();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                this.saveItem(itemTag, stacks.get(i));
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, this.loadItem(itemTags));
            }
        }
        onLoad();
    }

    public CompoundTag saveItem(CompoundTag p_77955_1_, ItemStack stack) {
        ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
        p_77955_1_.putString("id", resourcelocation.toString());
        p_77955_1_.putInt("Count", stack.getCount());
        if (stack.getTag() != null) {
            p_77955_1_.put("tag", stack.getTag().copy());
        }
        return p_77955_1_;
    }

    public ItemStack loadItem(CompoundTag nbt) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("id")));
        int count = nbt.getInt("Count");

        ItemStack stack;

        if(nbt.contains("ForgeCaps")){
            stack = new ItemStack(item, count, nbt.getCompound("ForgeCaps"));
        }else{
            stack = new ItemStack(item, count);
        }
        if (nbt.contains("tag", 10)) {
            stack.setTag(nbt.getCompound("tag"));
            stack.getItem().verifyTagAfterLoad(stack.getTag());
        }
        return stack;
    }
}
