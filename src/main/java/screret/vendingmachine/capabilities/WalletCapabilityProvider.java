package screret.vendingmachine.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.containers.stackhandlers.WalletStackHandler;

public class WalletCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    // BackpackItemStackHandler is just an extension of ItemStackHandler which makes sure no Backpack can be put in the inventory
    private WalletStackHandler cachedStackHandler;

    // This instantiates the Inventory only when it is first requested, and then caches it
    @NotNull
    private WalletStackHandler getCachedInventory() {
        if (cachedStackHandler == null) cachedStackHandler = new WalletStackHandler();
        return cachedStackHandler;
    }

    private final LazyOptional<IItemHandler> lazyInventory = LazyOptional.of(this::getCachedInventory);

    // Provides the Inventory
    @NotNull @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, lazyInventory);
    }

    // Saves the Inventory data to an NBT tag, so that it can be saved to disk
    @Override
    public CompoundTag serializeNBT() {
        return getCachedInventory().serializeNBT();
    }

    // Reads the Inventory data from an NBT tag that was saved to disk
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getCachedInventory().deserializeNBT(nbt);
    }
}