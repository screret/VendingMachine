package screret.vendingmachine.blockEntities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.LargeStackHandler;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VendingMachineBlockEntity extends BlockEntity implements MenuProvider {
    private static final int OUTPUT_SLOT_INDEX = 1, MONEY_SLOT_INDEX = 0;
    public LargeStackHandler inventory = new LargeStackHandler(30, this);
    public ItemStackHandler otherSlots = createOthersInv();
    private float collectedMoney;

    public UUID owner;
    public Player currentPlayer;
    public float currentPlayerInsertedMoney = 0;

    private Map<Item, Float> priceMap = new HashMap<>();

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
        parentNBTTagCompound.putFloat("CollectedMoney", collectedMoney);
        parentNBTTagCompound.putFloat("InsertedMoney", currentPlayerInsertedMoney);
    }

    protected CompoundTag savePrices() {
        CompoundTag nbt = new CompoundTag();
        if(VendingMachineConfig.GENERAL.allowPriceEditing.get()){
            for (Map.Entry<Item, Float> entry : priceMap.entrySet()){
                nbt.put(ForgeRegistries.ITEMS.getKey(entry.getKey()).toString(), FloatTag.valueOf(entry.getValue()));
            }
        } else {
            for (Map.Entry<Item, Float> entry : VendingMachineConfig.getDecryptedPrices().entrySet()){
                nbt.put(ForgeRegistries.ITEMS.getKey(entry.getKey()).toString(), FloatTag.valueOf(entry.getValue()));
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

        collectedMoney = parentNBTTagCompound.getFloat("CollectedMoney");
        currentPlayerInsertedMoney = parentNBTTagCompound.getFloat("InsertedMoney");
        //LOGGER.debug(world.getRecipeManager().getRecipesForType(BlenderRecipeSerializer.BLENDING));
    }

    protected Map<Item, Float> loadPrices(CompoundTag tag){
        Map<Item, Float> map = new HashMap<>();

        for (String entry : tag.getAllKeys()){
            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry)), tag.getFloat(entry));
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
        if(VendingMachineConfig.getPaymentItem() == Registration.MONEY.get()){
            cashOut();
            return;
        }

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
    public Component getDisplayName() {
        return Component.translatable("container.vendingmachine.vendingmachine");
    }

    public VenderBlockContainer container;

    @Override
    public VenderBlockContainer createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        currentPlayer = playerEntity;
        this.container = new VenderBlockContainer(windowID, playerInventory, this.inventory, this.otherSlots, this);
        return this.container;
    }

    public PriceEditorContainerProvider priceEditorContainerProvider = new PriceEditorContainerProvider(this);

    public void buy(int slotIndex, int amount) {
        if(!container.isAllowedToTakeItems) {
            ItemStack stack = inventory.getStackInSlot(slotIndex);
            float price = VendingMachineConfig.GENERAL.allowPriceEditing.get() ? this.priceMap.getOrDefault(stack.getItem(), 0f) : VendingMachineConfig.getDecryptedPrices().getOrDefault(stack.getItem(), 0f);
            ItemStack stack1 = new ItemStack(stack.getItem(), Math.min(stack.getCount(), amount));
            float _price = price * amount;

            if (!otherSlots.getStackInSlot(OUTPUT_SLOT_INDEX).isEmpty()) {
                Inventory playerInv = currentPlayer.getInventory();
                ItemStack outputStack = otherSlots.getStackInSlot(OUTPUT_SLOT_INDEX).copy();
                otherSlots.setStackInSlot(OUTPUT_SLOT_INDEX, ItemStack.EMPTY);
                if (!playerInv.add(outputStack)) {
                    BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
                    Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), outputStack);
                }
            }

            if(VendingMachineConfig.getPaymentItem() == Registration.MONEY.get()){
                if (_price > currentPlayerInsertedMoney) {
                    var _amount = Math.min((int)(collectedMoney / price), amount);
                    stack1 = new ItemStack(stack.getItem(), _amount);

                    currentPlayer.sendSystemMessage(Component.translatable("msg.vendingmachine.notenoughmoney"));
                }else{
                    giveChange(_price, currentPlayerInsertedMoney);
                }
                currentPlayerInsertedMoney -= _price;
            } else {
                ItemStack money = otherSlots.getStackInSlot(MONEY_SLOT_INDEX);

                if (_price > money.getCount()) {
                    var _amount = Math.min((int)(money.getCount() / price), amount);
                    stack1 = new ItemStack(stack.getItem(), _amount);

                    currentPlayer.sendSystemMessage(Component.translatable("msg.vendingmachine.notenoughmoney"));
                }
                otherSlots.extractItem(MONEY_SLOT_INDEX, (int) (_price), false);
            }
            otherSlots.insertItem(OUTPUT_SLOT_INDEX, new ItemStack(stack1.getItem(), stack1.getCount() + otherSlots.getStackInSlot(OUTPUT_SLOT_INDEX).getCount()), false);
            inventory.extractItem(slotIndex, amount, false);
            currentPlayer.sendSystemMessage(Component.translatable("msg.vendingmachine.buy", stack1.toString(), MoneyItem.DECIMAL_FORMAT.format(amount * price), ((MutableComponent) VendingMachineConfig.getPaymentItem().getDescription()).withStyle(ChatFormatting.DARK_GREEN)));
        }
    }

    private void giveChange(float price, float insertedMoney){
        float change = insertedMoney - price;

        if(!level.isClientSide() && change > 0){
            float amount = change;
            float[] moneyOut = new float[8];

            moneyOut[7] = amount / 1000f;
            amount -= (moneyOut[7] * 1000f);

            moneyOut[6] = amount / 100f;
            amount -= (moneyOut[6] * 100f);

            moneyOut[5] = amount / 50f;
            amount -= (moneyOut[5] * 50f);

            moneyOut[4] = amount / 20f;
            amount -= (moneyOut[4] * 20f);

            moneyOut[3] = amount / 10f;
            amount -= (moneyOut[3] * 10f);

            moneyOut[2] = amount / 5f;
            amount -= (moneyOut[2] * 5f);

            moneyOut[1] = amount / 2f;
            amount -= (moneyOut[1] * 2f);

            moneyOut[0] = Math.round(amount);

            for (int i = 0; i < moneyOut.length; ++i) {
                if(collectedMoney < moneyOut[i])
                    return;

                boolean check = moneyOut[i] != 0;

                if(check){
                    ArrayList<ItemStack> stacks = new ArrayList<>();

                    var itemStack = new ItemStack(Registration.MONEY.get());
                    stacks.add(itemStack);

                    MoneyItem.setMoneyValue(itemStack, MoneyItem.MONEY_VALUES[i]);
                    itemStack.setCount((int)moneyOut[i]);

                    while(itemStack.getCount() > itemStack.getMaxStackSize()){
                        stacks.add(itemStack.split(itemStack.getMaxStackSize()));
                    }

                    boolean playerInGui = false;
                    if(this.currentPlayer != null) playerInGui = true;

                    if(playerInGui){
                        Inventory playerInv = this.currentPlayer.getInventory();
                        boolean placed = false;

                        searchLoop:
                        for(int j = 0; j < playerInv.items.size(); ++j){
                            ItemStack playerStack = playerInv.items.get(j);
                            for (var stack : stacks){
                                if(ItemStack.isSameItemSameTags(stack, playerStack)){
                                    if(playerStack.getCount() + stack.getCount() <= playerStack.getMaxStackSize()){
                                        playerStack.setCount(playerStack.getCount() + stack.getCount());
                                        stack = ItemStack.EMPTY;
                                        placed = true;
                                        break searchLoop;
                                    }
                                }
                            }
                        }

                        if(!placed){
                            for(var stack : stacks){
                                if(playerInv.getFreeSlot() != -1){
                                    playerInv.add(stack);
                                    stack = ItemStack.EMPTY;
                                }else{
                                    playerInGui = false;
                                }
                            }
                        }
                    }
                    if (!playerInGui) {       //If no room, spawn
                        BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
                        for(var stack : stacks){
                            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack);
                        }
                    }

                    collectedMoney -= change;
                }
            }
        }
    }

    public void cashOut(){
        if(!level.isClientSide() && this.collectedMoney > 0) {
            float amount = collectedMoney;
            float[] moneyOut = new float[8];

            moneyOut[7] = (int)(amount / 1000);
            amount -= moneyOut[7] * 1000;

            moneyOut[6] = (int)(amount / 100);
            amount -= moneyOut[6] * 100;

            moneyOut[5] = (int)(amount / 50);
            amount -= moneyOut[5] * 50;

            moneyOut[4] = (int)(amount / 20);
            amount -= moneyOut[4] * 20;

            moneyOut[3] = (int)(amount / 10);
            amount -= moneyOut[3] * 10;

            moneyOut[2] = (int)(amount / 5);
            amount -= moneyOut[2] * 5;

            moneyOut[1] = (int)(amount / 2);
            amount -= moneyOut[1] * 2;

            moneyOut[0] = amount;

            collectedMoney = 0;

            for(int i = 0; i < moneyOut.length; ++i){
                boolean check = moneyOut[i] != 0;

                if(check){
                    ArrayList<ItemStack> stacks = new ArrayList<>();

                    var itemStack = new ItemStack(Registration.MONEY.get());
                    stacks.add(itemStack);

                    MoneyItem.setMoneyValue(itemStack, MoneyItem.MONEY_VALUES[i]);
                    itemStack.setCount((int)moneyOut[i]);

                    while(itemStack.getCount() > itemStack.getMaxStackSize()){
                        stacks.add(itemStack.split(itemStack.getMaxStackSize()));
                    }

                    boolean playerInGui = false;
                    if(this.currentPlayer != null) playerInGui = true;

                    if(playerInGui){
                        Inventory playerInv = this.currentPlayer.getInventory();
                        boolean placed = false;

                        searchLoop:
                        for(int j = 0; j < playerInv.items.size(); ++j){
                            ItemStack playerStack = playerInv.items.get(j);
                            for (var stack : stacks){
                                if(ItemStack.isSameItemSameTags(stack, playerStack)){
                                    if(playerStack.getCount() + stack.getCount() <= playerStack.getMaxStackSize()){
                                        playerStack.setCount(playerStack.getCount() + stack.getCount());
                                        stack = ItemStack.EMPTY;
                                        placed = true;
                                        break searchLoop;
                                    }
                                }
                            }
                        }

                        if(!placed){
                            for(var stack : stacks){
                                if(playerInv.getFreeSlot() != -1){
                                    playerInv.add(stack);
                                    stack = ItemStack.EMPTY;
                                }else{
                                    playerInGui = false;
                                }
                            }
                        }
                    }
                    if (!playerInGui) {       //If no room, spawn
                        BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
                        for(var stack : stacks){
                            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack);
                        }
                    }
                }
            }
        }
    }

    public void addPrice(ItemStack item, float price){
        priceMap.put(item.getItem(), price);
        this.setChanged();
    }

    public void removePrice(ItemStack item){
        priceMap.remove(item.getItem());
        this.setChanged();
    }

    public Map<Item, Float> getPrices(){
        return priceMap;
    }

    private ItemStackHandler createOthersInv(){
        return new ItemStackHandler(2){
            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                //VendingMachineBlockEntity.this.setChanged();

                return super.insertItem(slot, stack, simulate);
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack)
            {
                validateSlotIndex(slot);

                if(slot == MONEY_SLOT_INDEX){
                    if(stack.is(Registration.MONEY.get()) && VendingMachineConfig.getPaymentItem() == Registration.MONEY.get()){
                        float moneyTagValue = MoneyItem.getMoneyValue(stack);
                        float value = stack.getCount() * moneyTagValue;
                        VendingMachineBlockEntity.this.collectedMoney += value;
                        VendingMachineBlockEntity.this.currentPlayerInsertedMoney += value;

                        onContentsChanged(slot);
                        return;
                    }
                }

                this.stacks.set(slot, stack);
                onContentsChanged(slot);
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                //VendingMachineBlockEntity.this.setChanged();
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                VendingMachineBlockEntity.this.setChanged();
                super.onContentsChanged(slot);
            }
        };
    }

    public float getCollectedMoney() {
        return this.collectedMoney;
    }

    public void setCollectedMoney(float moneyCount){
        this.collectedMoney = moneyCount;
    }
}
