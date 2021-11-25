package screret.vendingmachine.tileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;

public class VendingMachineTile extends TileEntity implements INamedContainerProvider {

    public ItemStackHandler itemSlot = customHandlerOutput(1);
    public ItemStackHandler inputSlot = customHandlerItems(36);
    public ItemStackHandler moneySlot =  customHandlerMoney(1);

    public CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemSlot, inputSlot, moneySlot);

    static Logger LOGGER = LogManager.getLogger();

    public UUID owner;
    public UUID currentPlayer;

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
        parentNBTTagCompound.putUUID("Owner", owner);
        parentNBTTagCompound.put("Inputslot", inputSlot.serializeNBT());
        parentNBTTagCompound.put("MoneySlot", moneySlot.serializeNBT());
        parentNBTTagCompound.put("ItemSlot", itemSlot.serializeNBT());
        combinedInvWrapper = new CombinedInvWrapper(itemSlot, inputSlot, moneySlot);
        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in write
    @Override
    public void load(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.load(blockState, parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = parentNBTTagCompound.getUUID("Owner");
        inputSlot.deserializeNBT(parentNBTTagCompound.getCompound("Inputslot"));
        moneySlot.deserializeNBT(parentNBTTagCompound.getCompound("MoneySlot"));
        itemSlot.deserializeNBT(parentNBTTagCompound.getCompound("ItemSlot"));
        combinedInvWrapper = new CombinedInvWrapper(itemSlot, inputSlot, moneySlot);
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    public void dropContents(){
        Random random = new Random();
        for(int i = 0; i < NUMBER_OF_SLOTS; i++){
            ItemEntity item = new ItemEntity(getLevel(), random.nextDouble(), random.nextDouble(), random.nextDouble(), combinedInvWrapper.getStackInSlot(i));
            item.spawnAtLocation(combinedInvWrapper.getStackInSlot(i));
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

    public ItemStackHandler customHandlerItems(int size){
        return new ItemStackHandler(size) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                LOGGER.info(currentPlayer + " " + getLevel().getPlayerByUUID(owner));
                return currentPlayer == owner;
            }
        };
    }

    public ItemStackHandler customHandlerMoney(int size){
        return new ItemStackHandler(size) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.copy().getItem() == Items.GOLD_INGOT;
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

    @Override
    public VenderBlockContainer createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new VenderBlockContainer(windowID, playerInventory, this.inputSlot, this.itemSlot, this.moneySlot, this);
    }

}
