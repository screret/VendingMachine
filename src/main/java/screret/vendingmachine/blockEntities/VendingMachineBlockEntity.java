package screret.vendingmachine.blockEntities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.LargeStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VendingMachineBlockEntity extends BlockEntity implements MenuProvider {
    private static final int OUTPUT_SLOT_INDEX = 1, MONEY_SLOT_INDEX = 0;
    public LargeStackHandler inventory = new LargeStackHandler(30, this);
    public ItemStackHandler otherSlots = createOthersInv();
    public static Logger LOGGER = LogManager.getLogger();

    public UUID owner;

    private Map<Item, Integer> priceMap = new HashMap<>();

    public VendingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.VENDER_TILE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag parentNBTTagCompound) {
        super.saveAdditional(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location
        parentNBTTagCompound.putUUID("Owner", owner);
        parentNBTTagCompound.put("Inventory", inventory.serializeNBT());
        parentNBTTagCompound.put("OtherSlots", otherSlots.serializeNBT());
        //parentNBTTagCompound.put("OutputSlot", outputSlot.serializeNBT());
        parentNBTTagCompound.put("Prices", savePrices());
    }

    protected CompoundTag savePrices() {
        CompoundTag nbt = new CompoundTag();
        if(VendingMachineConfig.GENERAL.allowPriceEditing.get()){
            for (Map.Entry<Item, Integer> entry : priceMap.entrySet()){
                nbt.put(entry.getKey().getRegistryName().toString(), IntTag.valueOf(entry.getValue()));
            }
        } else {
            for (Map.Entry<Item, Integer> entry : VendingMachineConfig.getDecryptedPrices().entrySet()){
                nbt.put(entry.getKey().getRegistryName().toString(), IntTag.valueOf(entry.getValue()));
            }
        }
        return nbt;
    }

    // This is where you load the data that you saved in write
    @Override
    public void load(@NotNull CompoundTag parentNBTTagCompound) {
        super.load(parentNBTTagCompound); // The super call is required to save and load the tiles location
        owner = parentNBTTagCompound.getUUID("Owner");
        inventory.deserializeNBT(parentNBTTagCompound.getCompound("Inventory"));
        otherSlots.deserializeNBT(parentNBTTagCompound.getCompound("OtherSlots"));
        //outputSlot.deserializeNBT(parentNBTTagCompound.getCompound("OutputSlot"));
        priceMap = loadPrices(parentNBTTagCompound.getCompound("Prices"));
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    protected Map<Item, Integer> loadPrices(CompoundTag tag){
        Map<Item, Integer> map = new HashMap<>();

        for (String entry : tag.getAllKeys()){
            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry)), tag.getInt(entry));
        }
        return map;
    }

    public void dropContents(){
        for(int slot = 0; slot < inventory.getSlots(); slot++){
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), inventory.getStackInSlot(slot));
        }
        dropMoney();
    }

    public void dropMoney(){
        BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
        Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), otherSlots.getStackInSlot(MONEY_SLOT_INDEX));
        otherSlots.setStackInSlot(MONEY_SLOT_INDEX, ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag(){
        return this.saveWithFullMetadata();
    }

    @Override
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
        this.container = new VenderBlockContainer(windowID, playerInventory, this.inventory, this.otherSlots, this);
        return this.container;
    }

    public PriceEditorContainerProvider priceEditorContainerProvider = new PriceEditorContainerProvider(this);

    public void buy(int slotIndex, int amount) {
        if(!container.isAllowedToTakeItems) {
            ItemStack money = otherSlots.getStackInSlot(MONEY_SLOT_INDEX);
            ItemStack stack = inventory.getStackInSlot(slotIndex);
            int price = VendingMachineConfig.GENERAL.allowPriceEditing.get() ? this.priceMap.getOrDefault(stack.getItem(), 0) : VendingMachineConfig.getDecryptedPrices().getOrDefault(stack.getItem(), 0);
            ItemStack stack1 = new ItemStack(stack.getItem(), Math.min(stack.getCount(), amount));
            if (!stack.isEmpty() && price * amount < money.getCount()) {
                otherSlots.setStackInSlot(OUTPUT_SLOT_INDEX, new ItemStack(stack1.getItem(), stack1.getCount() + otherSlots.getStackInSlot(OUTPUT_SLOT_INDEX).getCount()));
                inventory.extractItem(slotIndex, Math.min(money.getCount() * price, amount), false);
                otherSlots.extractItem(MONEY_SLOT_INDEX, amount * price, false);
                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslatableComponent("msg.vendingmachine.buy", stack1.toString(), amount * price, ((TranslatableComponent)VendingMachineConfig.getPaymentItem().getDescription()).withStyle(ChatFormatting.DARK_GREEN)), this.container.currentPlayer);
            } else if (price * amount > money.getCount()) {
                amount = money.getCount() / price;
                stack1 = new ItemStack(stack.getItem(), amount);
                otherSlots.setStackInSlot(OUTPUT_SLOT_INDEX, new ItemStack(stack1.getItem(), stack1.getCount() + otherSlots.getStackInSlot(OUTPUT_SLOT_INDEX).getCount()));
                inventory.extractItem(slotIndex, Math.min(money.getCount() * price, amount), false);
                otherSlots.extractItem(MONEY_SLOT_INDEX, amount * price, false);

                this.getLevel().getPlayerByUUID(this.container.currentPlayer).sendMessage(new TranslatableComponent("msg.vendingmachine.notenoughmoney"), this.container.currentPlayer);
            }
        }
    }

    public void addPrice(ItemStack item, int price){
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

    private ItemStackHandler createOthersInv(){
        return new ItemStackHandler(2){
            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                VendingMachineBlockEntity.this.setChanged();
                return super.insertItem(slot, stack, simulate);
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                VendingMachineBlockEntity.this.setChanged();
                return super.extractItem(slot, amount, simulate);
            }
        };
    }
}
