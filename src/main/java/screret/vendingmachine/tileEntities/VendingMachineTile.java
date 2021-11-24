package screret.vendingmachine.tileEntities;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;

import javax.annotation.Nonnull;

public class VendingMachineTile extends TileEntity implements INamedContainerProvider {

    public ItemStackHandler itemSlot = customHandler(1);
    public ItemStackHandler inputSlot = customHandler(36);
    public ItemStackHandler moneySlot =  customHandler(1);

    public CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemSlot, inputSlot, moneySlot);

    static Logger LOGGER = LogManager.getLogger();

    public PlayerEntity owner;

    public static final int NUMBER_OF_SLOTS = 38;

    protected ItemStack failedMatch = ItemStack.EMPTY;

    public VendingMachineTile() {
        super(Registration.VENDER_TILE.get());
    }

    ItemStackHandler handler = new ItemStackHandler(){

    };

    @Override
    public CompoundNBT save(CompoundNBT parentNBTTagCompound) {
        super.save(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location
        parentNBTTagCompound.putUUID("vendingmachine:owner", owner.getUUID());
        parentNBTTagCompound.put("vendingmachine:inputslot", inputSlot.serializeNBT());
        parentNBTTagCompound.put("vendingmachine:moneySlot", moneySlot.serializeNBT());
        parentNBTTagCompound.put("vendingmachine:itemSlot", itemSlot.serializeNBT());
        combinedInvWrapper = new CombinedInvWrapper(itemSlot, inputSlot, moneySlot);
        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in write
    @Override
    public void load(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.load(blockState, parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = this.level.getPlayerByUUID(parentNBTTagCompound.getUUID("vendingmachine:owner"));
        inputSlot.deserializeNBT(parentNBTTagCompound.getCompound("vendingmachine:inputslot"));
        moneySlot.deserializeNBT(parentNBTTagCompound.getCompound("vendingmachine:moneySlot"));
        itemSlot.deserializeNBT(parentNBTTagCompound.getCompound("vendingmachine:itemSlot"));
        combinedInvWrapper = new CombinedInvWrapper(itemSlot, inputSlot, moneySlot);
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    public void dropContents(){
        for(int i = 0; i < NUMBER_OF_SLOTS; i++){
            ItemEntity item = new ItemEntity(this.level, 0, 0, 0, combinedInvWrapper.getStackInSlot(i));
            item.spawnAtLocation(combinedInvWrapper.getStackInSlot(i));
        }
    }

    public ItemStackHandler customHandler(int size){
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                // To make sure the TE persists when the chunk is saved later we need to
                // mark it dirty every time the item handler changes
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0) {
                    return false;
                } else if (slot != 36) {
                    return true;
                } else {
                    return stack.copy().getItem() == Items.GOLD_INGOT;
                }
            }
        };
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.vendingmachine.vendingmachine");
    }

    @Override
    public VenderBlockContainer createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new VenderBlockContainer(windowID, playerInventory, this.combinedInvWrapper, this);
    }

}
