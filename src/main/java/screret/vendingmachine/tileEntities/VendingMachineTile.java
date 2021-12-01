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
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.ItemHandlerMoney;
import screret.vendingmachine.containers.OwnedStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.containers.VenderPriceEditorContainer;
import screret.vendingmachine.init.Registration;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VendingMachineTile extends TileEntity implements INamedContainerProvider {

    public ItemStackHandler outputSlot = customHandlerOutput(1);
    public OwnedStackHandler inputSlot = new OwnedStackHandler(30);
    public ItemHandlerMoney moneySlot =  new ItemHandlerMoney(1);

    static Logger LOGGER = LogManager.getLogger();

    public UUID owner;

    public HashMap<ItemStack, Integer> priceHashMap = new HashMap<ItemStack, Integer>();

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
        parentNBTTagCompound.put("InputSlot", inputSlot.serializeNBT());
        parentNBTTagCompound.put("MoneySlot", moneySlot.serializeNBT());
        parentNBTTagCompound.put("OutputSlot", outputSlot.serializeNBT());
        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in write
    @Override
    public void load(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.load(blockState, parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = parentNBTTagCompound.getUUID("Owner");
        inputSlot.deserializeNBT(parentNBTTagCompound.getCompound("InputSlot"));
        moneySlot.deserializeNBT(parentNBTTagCompound.getCompound("MoneySlot"));
        outputSlot.deserializeNBT(parentNBTTagCompound.getCompound("OutputSlot"));
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
        return this.container;
    }

    public PriceEditorContainerProvider priceEditorContainerProvider = new PriceEditorContainerProvider(this);

    public void buy(int slotIndex) {
        if(!container.isAllowedToTakeItems) {
            ItemStack money = moneySlot.getStackInSlot(0);
            ItemStack stack = inputSlot.getStackInSlot(slotIndex);
            int price = VendingMachineConfig.DECRYPTED_PRICES.getOrDefault(stack.getItem(), 4);
            ItemStack stack1 = new ItemStack(stack.getItem(), Math.min(stack.getCount(), 64));
            if (!stack.isEmpty() && price < money.getCount()) {
                LOGGER.info("bought " + stack.getCount() + " " + stack.getItem());
                outputSlot.setStackInSlot(0, stack1);
                inputSlot.extractItem(slotIndex, 64, false);
                moneySlot.extractItem(0, 1, false);
                LOGGER.info("set item in slot 0 to item " + stack1.getItem() + " (" + stack1.getCount() + ")");
            } else if (price > money.getCount()) {
                LOGGER.warn("you don't have enough money.");
                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslationTextComponent("vendingmachine.notenoughmoney"), this.container.currentPlayer);

            }
        }
    }

}
