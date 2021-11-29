package screret.vendingmachine.tileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.containers.OwnedStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VendingMachineTile extends TileEntity implements INamedContainerProvider {

    public ItemStackHandler outputSlot = customHandlerOutput(1);
    public OwnedStackHandler inputSlot = new OwnedStackHandler(36);
    public ItemStackHandler moneySlot =  customHandlerMoney(1);

    static Logger LOGGER = LogManager.getLogger();

    public UUID owner;
    public UUID currentPlayer;

    private final LazyOptional<IItemHandler> inputSlotholder = LazyOptional.of(() -> inputSlot);

    public static final int NUMBER_OF_SLOTS = 38;

    protected ItemStack failedMatch = ItemStack.EMPTY;

    public VendingMachineTile() {
        super(Registration.VENDER_TILE.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT parentNBTTagCompound) {
        super.save(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location
        parentNBTTagCompound.putUUID("Owner", owner);
        parentNBTTagCompound.put("Inputslot", inputSlot.serializeNBT());
        parentNBTTagCompound.put("MoneySlot", moneySlot.serializeNBT());
        parentNBTTagCompound.put("ItemSlot", outputSlot.serializeNBT());
        parentNBTTagCompound.merge(inputSlot.serializeNBT());
        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in write
    @Override
    public void load(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.load(blockState, parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = parentNBTTagCompound.getUUID("Owner");
        inputSlot.deserializeNBT(parentNBTTagCompound.getCompound("Inputslot"));
        moneySlot.deserializeNBT(parentNBTTagCompound.getCompound("MoneySlot"));
        outputSlot.deserializeNBT(parentNBTTagCompound.getCompound("ItemSlot"));
        inputSlot.deserializeNBT(parentNBTTagCompound);
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    public void dropContents(){
        CombinedInvWrapper wrapper = new CombinedInvWrapper(inputSlot, moneySlot, outputSlot);
        for(int i = 0; i < wrapper.getSlots(); i++){
            ItemEntity entity = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            entity.spawnAtLocation(wrapper.getStackInSlot(i));
        }
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT nbt = this.save(new CompoundNBT());
        return nbt;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        this.save(this.getUpdateTag());
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        load(this.getBlockState(), packet.getTag());
    }

    public ItemStackHandler customHandlerMoney(int size){
        return new ItemStackHandler(size) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.copy().getItem() == Items.GOLD_INGOT;
            }
            @Override
            public int getSlotLimit(int slot)
            {
                return 1024;
            }
            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return 1024;
            }
        };
    }

    public ItemStackHandler customHandlerOutput(int size){
        return new ItemStackHandler(size) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return false;
            }
        };
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.vendingmachine.vendingmachine");
    }

    public VenderBlockContainer container;

    @Override
    public VenderBlockContainer createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        this.container = new VenderBlockContainer(windowID, playerInventory, this.inputSlot, this.outputSlot, this.moneySlot, this);
        this.currentPlayer = playerEntity.getUUID();
        return this.container;
    }

    public void buy(SlotItemHandler stack) {
        if(stack.getItem() != ItemStack.EMPTY) {
            outputSlot.insertItem(0, stack.getItem().copy(), false);
            stack.remove(outputSlot.getSlotLimit(0));
            moneySlot.extractItem(0, 1, false);
        }
        LOGGER.info("bought " + stack.getItem());
    }

}
