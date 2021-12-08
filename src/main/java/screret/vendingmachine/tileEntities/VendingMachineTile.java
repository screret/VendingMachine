package screret.vendingmachine.tileEntities;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.ItemStackHandlerMoney;
import screret.vendingmachine.containers.ItemStackHandlerOutput;
import screret.vendingmachine.containers.OwnedStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.events.packets.SendOwnerToClientPacket;
import screret.vendingmachine.init.Registration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VendingMachineTile extends BlockEntity implements MenuProvider {

    public ItemStackHandler outputSlot = new ItemStackHandlerOutput(1);
    public OwnedStackHandler inputSlot = new OwnedStackHandler(30);
    public ItemStackHandlerMoney moneySlot =  new ItemStackHandlerMoney(1);

    public static Logger LOGGER = LogManager.getLogger();

    public UUID owner;

    private Map<Item, Integer> priceMap = new HashMap<>();

    public VendingMachineTile(BlockPos pos, BlockState state) {
        super(Registration.VENDER_TILE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag parentNBTTagCompound) {
        super.saveAdditional(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location
        parentNBTTagCompound.putUUID("Owner", owner);
        parentNBTTagCompound.put("InputSlot", inputSlot.serializeNBT());
        parentNBTTagCompound.put("MoneySlot", moneySlot.serializeNBT());
        parentNBTTagCompound.put("OutputSlot", outputSlot.serializeNBT());
        parentNBTTagCompound.put("Prices", savePrices());
    }

    protected CompoundTag savePrices() {
        if(VendingMachineConfig.GENERAL.allowPriceEditing.get()){
            CompoundTag nbt = new CompoundTag();
            for (Map.Entry<Item, Integer> entry : priceMap.entrySet()){
                nbt.put(ForgeRegistries.ITEMS.getKey(entry.getKey()).toString(), IntTag.valueOf(entry.getValue()));
            }
            return nbt;
        } else {
            CompoundTag nbt = new CompoundTag();
            for (Map.Entry<Item, Integer> entry : VendingMachineConfig.DECRYPTED_PRICES.entrySet()){
                nbt.put(ForgeRegistries.ITEMS.getKey(entry.getKey()).toString(), IntTag.valueOf(entry.getValue()));
            }
            return nbt;
        }
    }

    // This is where you load the data that you saved in write
    @Override
    public void load(@NotNull CompoundTag parentNBTTagCompound) {
        super.load(parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = parentNBTTagCompound.getUUID("Owner");
        inputSlot.deserializeNBT(parentNBTTagCompound.getCompound("InputSlot"));
        moneySlot.deserializeNBT(parentNBTTagCompound.getCompound("MoneySlot"));
        outputSlot.deserializeNBT(parentNBTTagCompound.getCompound("OutputSlot"));
        priceMap = loadPrices(parentNBTTagCompound.getCompound("Prices"));
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    protected Map<Item, Integer> loadPrices(CompoundTag array){
        Map<Item, Integer> map = new HashMap<>();

        for (String entry : array.getAllKeys()){
            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry)), array.getInt(entry));
        }
        return map;
    }

    public void dropContents(){
        CombinedInvWrapper wrapper = new CombinedInvWrapper(inputSlot, moneySlot, outputSlot);
        for(int i = 0; i < wrapper.getSlots(); i++){
            ItemEntity entity = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), wrapper.getStackInSlot(i));
            entity.spawnAtLocation(wrapper.getStackInSlot(i));
        }
    }

    public void dropMoney(){
        BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
        ItemEntity entity = new ItemEntity(getLevel(), pos.getX(), pos.getY(), pos.getZ(), moneySlot.getStackInSlot(0));
        entity.spawnAtLocation(moneySlot.getStackInSlot(0));
        moneySlot.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
        if(!level.isClientSide){
            VendingMachine.NETWORK_HANDLER.sendTo(new SendOwnerToClientPacket(this.worldPosition, this.owner), Minecraft.getInstance().getConnection().getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    @Override
    public CompoundTag getUpdateTag(){
        return this.save(new CompoundTag());
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        if(getLevel().isLoaded(this.worldPosition) && this.getLevel().getBlockState(this.worldPosition).getBlock() != Blocks.AIR){
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag compoundtag = pkt.getTag();
        if (compoundtag != null) {
            load(compoundtag);
        }
    }

    @Override
    public BaseComponent getDisplayName() {
        return new TranslatableComponent("gui.vendingmachine.vendingmachine");
    }

    public VenderBlockContainer container;

    @Override
    public VenderBlockContainer createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        this.container = new VenderBlockContainer(windowID, playerInventory, this.inputSlot, this.outputSlot, this.moneySlot, this);
        return this.container;
    }

    public PriceEditorContainerProvider priceEditorContainerProvider = new PriceEditorContainerProvider(this);

    public void buy(int slotIndex, int amount) {
        if(!container.isAllowedToTakeItems) {
            ItemStack money = moneySlot.getStackInSlot(0);
            ItemStack stack = inputSlot.getStackInSlot(slotIndex);
            int price = VendingMachineConfig.GENERAL.allowPriceEditing.get() ? this.priceMap.getOrDefault(stack.getItem(), 4) : VendingMachineConfig.DECRYPTED_PRICES.getOrDefault(stack.getItem(), 4);
            ItemStack stack1 = new ItemStack(stack.getItem(), Math.min(stack.getCount(), amount));
            if (!stack.isEmpty() && price * amount < money.getCount()) {
                outputSlot.setStackInSlot(0, new ItemStack(stack1.getItem(), stack1.getCount() + outputSlot.getStackInSlot(0).getCount()));
                inputSlot.extractItem(slotIndex, Math.min(money.getCount() * price, amount), false);
                moneySlot.extractItem(0, amount * price, false);
                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslatableComponent("msg.vendingmachine.buy", stack1.toString(), amount * price, new TextComponent(VendingMachineConfig.PAYMENT_ITEM.toString()).withStyle(ChatFormatting.DARK_GREEN)), this.container.currentPlayer);
            } else if (price * amount > money.getCount()) {
                amount = money.getCount() / price;
                stack1 = new ItemStack(stack.getItem(), amount);
                outputSlot.setStackInSlot(0, new ItemStack(stack1.getItem(), stack1.getCount() + outputSlot.getStackInSlot(0).getCount()));
                inputSlot.extractItem(slotIndex, Math.min(money.getCount() * price, amount), false);
                moneySlot.extractItem(0, amount * price, false);

                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslatableComponent("msg.vendingmachine.notenoughmoney"), this.container.currentPlayer);
            }
        }
    }

    public void addPrice(ItemStack item, int price){
        priceMap.put(item.getItem(), price);
        this.setChanged();
        LOGGER.info(priceMap.entrySet());
    }

    public void removePrice(ItemStack item){
        priceMap.remove(item.getItem());
        this.setChanged();
    }

    public Map<Item, Integer> getPrices(){
        return priceMap;
    }
}
