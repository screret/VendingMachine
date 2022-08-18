package screret.vendingmachine.blockEntities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderBlockMenu;
import screret.vendingmachine.containers.stackhandlers.LargeStackHandler;
import screret.vendingmachine.events.packets.PacketInsertedMoneyS2C;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class VendingMachineBlockEntity extends BlockEntity implements MenuProvider {
    private static final int OUTPUT_SLOT_INDEX = 1, MONEY_SLOT_INDEX = 0;
    public LargeStackHandler inventory = new LargeStackHandler(30, this);
    public ItemStackHandler otherSlots = createOthersInv();
    private float collectedMoney;

    public UUID owner;
    public Player currentPlayer;
    public float currentPlayerInsertedMoney = 0;

    private Map<Item, Float> priceMap = new HashMap<>();
    private boolean walletIn = false;

    public VendingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.VENDER_TILE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag parentNBTTagCompound) {
        super.saveAdditional(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location
        parentNBTTagCompound.putUUID("Owner", owner == null ? net.minecraft.Util.NIL_UUID : owner);
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
        return new TranslatableComponent("container.vendingmachine.vendingmachine");
    }

    public VenderBlockMenu container;

    @Override
    public VenderBlockMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        currentPlayer = playerEntity;
        this.container = new VenderBlockMenu(windowID, playerInventory, this.inventory, this.otherSlots, this);
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
            collectedMoney += _price;

            var money = otherSlots.getStackInSlot(MONEY_SLOT_INDEX);
            /*if(money.is(Registration.WALLET.get())){
                //walletIn = true;
                float moneyValue = Util.getTotalOfMoney(money);
                if(_price > moneyValue){
                    var _amount = Math.min((int)(currentPlayerInsertedMoney / price), amount);
                    stack1 = new ItemStack(stack.getItem(), _amount);
                    stack1.setTag(stack.getTag());
                }else{
                    giveChange(_price, moneyValue);
                    //walletIn = false;
                }
            } else */if(VendingMachineConfig.getPaymentItem() == Registration.MONEY.get()){
                if (_price > currentPlayerInsertedMoney) {
                    var _amount = Math.min((int)(currentPlayerInsertedMoney / price), amount);
                    stack1 = new ItemStack(stack.getItem(), _amount);
                    stack1.setTag(stack.getTag());

                }else{
                    var change = giveChange(_price, currentPlayerInsertedMoney);
                    currentPlayer.sendMessage(new TranslatableComponent("msg.vendingmachine.gave_change", Util.DECIMAL_FORMAT.format(change)), net.minecraft.Util.NIL_UUID);
                }
            } else {
                if (_price > money.getCount()) {
                    var _amount = Math.min((int)(money.getCount() / price), amount);
                    stack1 = new ItemStack(stack.getItem(), _amount);
                    stack1.setTag(stack.getTag());

                }
                otherSlots.extractItem(MONEY_SLOT_INDEX, (int) (_price), false);
            }
            if(currentPlayerInsertedMoney < _price){
                currentPlayer.sendMessage(new TranslatableComponent("msg.vendingmachine.not_enough_money"), net.minecraft.Util.NIL_UUID);
                if(currentPlayerInsertedMoney <= 0){
                    currentPlayer.sendMessage(new TranslatableComponent("msg.vendingmachine.not_enough_money.desc"), net.minecraft.Util.NIL_UUID);
                }
            } else {
                otherSlots.insertItem(OUTPUT_SLOT_INDEX, stack1, false);
                inventory.extractItem(slotIndex, stack1.getCount(), false);
                currentPlayer.sendMessage(new TranslatableComponent("msg.vendingmachine.buy", stack1.toString(), Util.DECIMAL_FORMAT.format(amount * price), ((MutableComponent) VendingMachineConfig.getPaymentItem().getDescription()).withStyle(ChatFormatting.DARK_GREEN)), net.minecraft.Util.NIL_UUID);
                currentPlayerInsertedMoney -= _price;
            }
            this.setChanged();
            VendingMachine.NETWORK_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) currentPlayer), new PacketInsertedMoneyS2C(currentPlayerInsertedMoney, this.worldPosition));
        }
    }

    private float giveChange(float price, float insertedMoney){
        float change = insertedMoney - price;

        if(!level.isClientSide() && change > 0){
            float amount = change;
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

            for (int i = 0; i < moneyOut.length; ++i) {
                if(collectedMoney < moneyOut[i] && !otherSlots.getStackInSlot(MONEY_SLOT_INDEX).is(Registration.WALLET.get())){
                    if(!walletIn){
                        currentPlayer.sendMessage(new TranslatableComponent("msg.vendingmachine.machine_empty_money"), net.minecraft.Util.NIL_UUID);
                    }
                    return collectedMoney;
                }

                boolean check = moneyOut[i] != 0;

                if(check){
                    currentPlayerInsertedMoney -= Util.MONEY_VALUES[i] * moneyOut[i];

                    HashMap<ItemStack, Boolean> stacks = new HashMap<>();

                    var itemStack = Util.setMoneyValue(new ItemStack(Registration.MONEY.get(), (int)moneyOut[i]), Util.MONEY_VALUES[i]);
                    stacks.put(itemStack, true);

                    while(itemStack.getCount() > itemStack.getMaxStackSize()){
                        stacks.put(itemStack.split(itemStack.getMaxStackSize()), true);
                    }

                    boolean playerInGui = this.currentPlayer != null;

                    if(playerInGui){
                        if(walletIn) {
                            IItemHandlerModifiable handler = (IItemHandlerModifiable) otherSlots.getStackInSlot(MONEY_SLOT_INDEX).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new IllegalStateException("Wallet doesn't have inventory"));

                            for(var stack : stacks.entrySet()){
                                searchLoop2:
                                for (int j = 0; j < handler.getSlots(); j++) {
                                    if (handler.getStackInSlot(j).equals(ItemStack.EMPTY) || ItemStack.isSameItemSameTags(stack.getKey(), handler.getStackInSlot(j))) {
                                        handler.setStackInSlot(j, stack.getKey().copy());
                                        stack.setValue(true);
                                        break searchLoop2;
                                    } else {
                                        stack.setValue(false);
                                    }
                                }

                            }
                        } else {
                            Inventory playerInv = this.currentPlayer.getInventory();
                            boolean placed = false;

                            for (var stack : stacks.entrySet()){
                                searchLoop:
                                for(int j = 0; j < playerInv.items.size(); ++j){
                                ItemStack playerStack = playerInv.items.get(j);
                                    if(ItemStack.isSameItemSameTags(stack.getKey(), playerStack)){
                                        if(playerStack.getCount() + stack.getKey().getCount() <= playerStack.getMaxStackSize()){
                                            playerStack.setCount(playerStack.getCount() + stack.getKey().getCount());
                                            placed = true;
                                            break searchLoop;
                                        }
                                    }
                                }
                            }

                            if(!placed){
                                for(var stack : stacks.entrySet()){
                                    if(playerInv.getFreeSlot() != -1){
                                        playerInv.add(stack.getKey());
                                        stack.setValue(true);
                                    }else{
                                        stack.setValue(false);
                                    }
                                }
                            }
                        }

                    }
                    BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
                    for(var stack : stacks.entrySet()){
                        if(!stack.getValue()){
                            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack.getKey());
                        }
                    }

                }
            }
        }
        collectedMoney -= change;
        return change;
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

            for(int i = 0; i < moneyOut.length; ++i){
                boolean check = moneyOut[i] != 0;

                if(check){
                    HashMap<ItemStack, Boolean> stacks = new HashMap<>();

                    var itemStack = Util.setMoneyValue(new ItemStack(Registration.MONEY.get(), (int)moneyOut[i]), Util.MONEY_VALUES[i]);
                    stacks.put(itemStack, true);

                    while(itemStack.getCount() > itemStack.getMaxStackSize()){
                        stacks.put(itemStack.split(itemStack.getMaxStackSize()), true);
                    }

                    boolean playerInGui = this.currentPlayer != null;

                    if(playerInGui){
                        if(walletIn) {
                            IItemHandlerModifiable handler = (IItemHandlerModifiable) otherSlots.getStackInSlot(MONEY_SLOT_INDEX).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new IllegalStateException("Wallet doesn't have inventory"));

                            for(var stack : stacks.entrySet()){
                                searchLoop2:
                                for (int j = 0; j < handler.getSlots(); j++) {
                                    if (handler.getStackInSlot(j).equals(ItemStack.EMPTY) || ItemStack.isSameItemSameTags(stack.getKey(), handler.getStackInSlot(j))) {
                                        handler.setStackInSlot(j, stack.getKey());
                                        stack.setValue(true);
                                        break searchLoop2;
                                    } else {
                                        stack.setValue(false);
                                    }
                                }

                            }
                            //}
                        } else {
                            Inventory playerInv = this.currentPlayer.getInventory();
                            boolean placed = false;

                            for (var stack : stacks.entrySet()){
                                searchLoop:
                                for(int j = 0; j < playerInv.items.size(); ++j){
                                    ItemStack playerStack = playerInv.items.get(j);
                                    if(ItemStack.isSameItemSameTags(stack.getKey(), playerStack)){
                                        if(playerStack.getCount() + stack.getKey().getCount() <= playerStack.getMaxStackSize()){
                                            playerStack.setCount(playerStack.getCount() + stack.getKey().getCount());
                                            placed = true;
                                            break searchLoop;
                                        }
                                    }
                                }
                            }

                            if(!placed){
                                for(var stack : stacks.entrySet()){
                                    if(playerInv.getFreeSlot() != -1){
                                        playerInv.add(stack.getKey());
                                        stack.setValue(true);
                                    }else{
                                        stack.setValue(false);
                                    }
                                }
                            }
                        }

                    }
                    BlockPos pos = getBlockPos().relative(this.getBlockState().getValue(VendingMachineBlock.FACING));
                    for(var stack : stacks.entrySet()){
                        if(!stack.getValue()){
                            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack.getKey());
                        }
                    }

                }
            }
            collectedMoney = 0;
            this.setChanged();
        }
        currentPlayerInsertedMoney = 0;
        if(currentPlayer != null){
            VendingMachine.NETWORK_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) currentPlayer), new PacketInsertedMoneyS2C(currentPlayerInsertedMoney, this.worldPosition));
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
                    if (stack.is(Registration.MONEY.get()) && VendingMachineConfig.getPaymentItem() == Registration.MONEY.get()) {
                        float value = Util.getTotalOfMoney(stack);
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
