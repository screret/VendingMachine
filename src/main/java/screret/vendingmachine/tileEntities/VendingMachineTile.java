package screret.vendingmachine.tileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.capabilities.configs.VendingMachineConfig;
import screret.vendingmachine.containers.ItemStackHandlerMoney;
import screret.vendingmachine.containers.ItemStackHandlerOutput;
import screret.vendingmachine.containers.OwnedStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VendingMachineTile extends TileEntity implements INamedContainerProvider {

    public ItemStackHandlerOutput outputSlot = new ItemStackHandlerOutput(1);
    public OwnedStackHandler inputSlot = new OwnedStackHandler(30);
    public ItemStackHandlerMoney moneySlot =  new ItemStackHandlerMoney(18);

    public static Logger LOGGER = LogManager.getLogger();

    public UUID owner;

    private Map<Item, Integer> priceMap = new HashMap<>();

    public VendingMachineTile() {
        super(Registration.VENDER_TILE.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT parentNBTTagCompound) {
        super.save(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location
        if(owner != null){
            parentNBTTagCompound.putUUID("Owner", owner);
        }
        parentNBTTagCompound.put("InputSlot", inputSlot.serializeNBT());
        parentNBTTagCompound.put("MoneySlot", moneySlot.serializeNBT());
        parentNBTTagCompound.put("OutputSlot", outputSlot.serializeNBT());
        parentNBTTagCompound.put("Prices", savePrices());
        return parentNBTTagCompound;
    }

    protected CompoundNBT savePrices() {
        CompoundNBT nbt = new CompoundNBT();
        if(VendingMachineConfig.GENERAL.allowPriceEditing.get()){
            for (Map.Entry<Item, Integer> entry : priceMap.entrySet()){
                nbt.putInt(ForgeRegistries.ITEMS.getKey(entry.getKey()).toString(), entry.getValue());
                //LOGGER.info(entry + " " + nbt.getAllKeys());
            }

        } else {
            for (Map.Entry<Item, Integer> entry : VendingMachineConfig.DECRYPTED_PRICES.entrySet()){
                nbt.putInt(ForgeRegistries.ITEMS.getKey(entry.getKey()).toString(), entry.getValue());
                //LOGGER.info(entry + " " + nbt.getAllKeys());
            }

        }
        return nbt;

    }

    // This is where you load the data that you saved in write
    @Override
    public void load(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.load(blockState, parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = parentNBTTagCompound.getUUID("Owner");
        inputSlot.deserializeNBT(parentNBTTagCompound.getCompound("InputSlot"));
        moneySlot.deserializeNBT(parentNBTTagCompound.getCompound("MoneySlot"));
        outputSlot.deserializeNBT(parentNBTTagCompound.getCompound("OutputSlot"));
        priceMap = loadPrices(parentNBTTagCompound.getCompound("Prices"));
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    protected Map<Item, Integer> loadPrices(CompoundNBT array){
        Map<Item, Integer> map = new HashMap<>();

        for (String entry : array.getAllKeys()){
            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry)), array.getInt(entry));
        }
        return map;
    }

    public void dropContents(){
        CombinedInvWrapper wrapper = new CombinedInvWrapper(inputSlot, moneySlot, outputSlot);
        for(int i = 0; i < wrapper.getSlots(); i++){
            ItemEntity entity = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            entity.spawnAtLocation(wrapper.getStackInSlot(i));
        }
    }

    public void dropMoney(){
        BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
        ItemEntity entity = new ItemEntity(getLevel(), pos.getX(), pos.getY(), pos.getZ());
        entity.spawnAtLocation(moneySlot.getStackInSlot(0));
        moneySlot.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public CompoundNBT getUpdateTag(){
        return this.save(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        load(this.getBlockState(), packet.getTag());
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

    public void buy(int slotIndex, int amount) {
        if(!container.isAllowedToTakeItems) {
            ItemStack money = moneySlot.getStackInSlot(0);
            ItemStack stack = inputSlot.getStackInSlot(slotIndex);
            int price = VendingMachineConfig.GENERAL.allowPriceEditing.get() ? this.priceMap.getOrDefault(stack.getItem(), 4) : VendingMachineConfig.DECRYPTED_PRICES.getOrDefault(stack.getItem(), 4);
            int realPrice = price * amount;
            int realMoney = money.getCount() * price;

            float moneyItemValue = -1;

            if(money.getItem().equals(Registration.MONEY.get())){
                moneyItemValue = money.getTag().getFloat(MoneyItem.MONEY_VALUE_TAG) * money.getCount();
            }

            ItemStack stack1 = new ItemStack(stack.getItem(), Math.min(stack.getCount(), amount));
            if (!stack.isEmpty() && (realPrice < money.getCount() || (moneyItemValue != -1 && realPrice < moneyItemValue))) {
                if(outputSlot.getStackInSlot(0).isEmpty()){
                    outputSlot.setStackInSlot(0, new ItemStack(stack1.getItem(), stack1.getCount() + outputSlot.getStackInSlot(0).getCount()));
                }else{
                    return;
                }
                inputSlot.extractItem(slotIndex, Math.min(realMoney, amount), false);

                boolean b = addMoneyToStorage(realPrice);

                if(!b){
                    return;
                }
                if(moneyItemValue != -1){

                }else{
                    moneySlot.extractItem(0, realPrice, false);
                }

                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslationTextComponent("msg.vendingmachine.buy", stack1.toString(), amount * price, new StringTextComponent(VendingMachineConfig.getPaymentItem().toString()).withStyle(TextFormatting.DARK_GREEN)), this.container.currentPlayer);
            } else if (realPrice > money.getCount()) {
                amount = money.getCount() / price;
                realPrice = price * amount;
                stack1 = new ItemStack(stack.getItem(), amount);
                outputSlot.setStackInSlot(0, new ItemStack(stack1.getItem(), stack1.getCount() + outputSlot.getStackInSlot(0).getCount()));
                inputSlot.extractItem(slotIndex, Math.min(realMoney, amount), false);

                boolean b = addMoneyToStorage(realPrice);

                if(!b){
                    return;
                }
                moneySlot.extractItem(0, realPrice, false);

                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslationTextComponent("msg.vendingmachine.notenoughmoney"), this.container.currentPlayer);
            }
        }
    }

    private boolean addMoneyToStorage(int realPrice){
        for(int i = 1; i < moneySlot.getSlots(); ++i){
            ItemStack insertedMoney = moneySlot.insertItem(i, new ItemStack(VendingMachineConfig.getPaymentItem(), realPrice), false);
            if(insertedMoney.isEmpty()){
                break;
            }else{
                if(i == moneySlot.getSlots() - 1){
                    insertedMoney = moneySlot.insertItem(1, insertedMoney, false);
                    if(!insertedMoney.isEmpty()){
                        return false;
                    }
                }else{
                    moneySlot.insertItem(i + 1, insertedMoney, false);
                }
            }
        }
        return true;
    }

    public void addPrice(@Nonnull ItemStack item, int price){
        priceMap.put(item.getItem(), price);
        this.setChanged();
    }

    public void removePrice(ItemStack item){
        priceMap.remove(item.getItem());
        this.setChanged();
    }

    public Map<Item, Integer> getPrices(){
        return priceMap;
    }
}
