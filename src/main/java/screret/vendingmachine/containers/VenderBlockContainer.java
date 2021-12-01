package screret.vendingmachine.containers;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public class VenderBlockContainer extends Container {

    private final PlayerInvWrapper playerInventory;
    public final OwnedStackHandler inputInventory;
    private final ItemHandlerMoney moneyInventory;
    private final IItemHandler outputInventory;

    public boolean isAllowedToTakeItems = false;
    public static boolean buyTestMode_REMOVE_LATER = false;
    public int tab;

    public SlotItemHandler selectedSlot;
    public UUID currentPlayer;
    private final VendingMachineTile tile;

    public static final int INPUT_SLOTS_X_AMOUNT = 4;
    public static final int INPUT_SLOTS_X_AMOUNT_PLUS_1 = INPUT_SLOTS_X_AMOUNT + 1;
    public static final int INPUT_SLOTS_Y_AMOUNT = 5;
    public static final int INPUT_SLOTS_Y_AMOUNT_PLUS_1 = INPUT_SLOTS_Y_AMOUNT + 1;

    public static final Logger LOGGER = LogManager.getLogger();

    private int quickcraftStatus;
    private int quickcraftType = -1;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();

    public VenderBlockContainer(int windowID, PlayerInventory playerInventory, OwnedStackHandler inputInv, IItemHandler outputInv, ItemHandlerMoney moneyInv, VendingMachineTile tileEntity) {
        super(Registration.VENDER_CONT.get(), windowID);
        this.playerInventory = new PlayerInvWrapper(playerInventory);
        this.tile = tileEntity;
        this.inputInventory = inputInv;
        this.moneyInventory = moneyInv;
        this.outputInventory = outputInv;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        layoutPlayerInventorySlots(8, 140);
        if (tileEntity != null) {
            final int INPUT_SLOTS_XPOS = 8;
            final int INPUT_SLOTS_YPOS = 18;
            final int MONEY_SLOT_XPOS = 134;
            final int MONEY_SLOT_YPOS = 36;
            for(int x = 0; x < INPUT_SLOTS_X_AMOUNT_PLUS_1; x++){
                for(int y = 0; y < INPUT_SLOTS_Y_AMOUNT_PLUS_1; y++) {
                    int slotNumber = y * INPUT_SLOTS_Y_AMOUNT + x;
                    this.addSlot(MyHandler(this.inputInventory, slotNumber, INPUT_SLOTS_XPOS + SLOT_X_SPACING * x, INPUT_SLOTS_YPOS + SLOT_Y_SPACING * y));
                }
            }
            currentPlayer = playerInventory.player.getUUID();
            checkPlayerAllowedToChangeInv(currentPlayer);

            this.addSlot(new SlotItemHandler(this.moneyInventory, 0, MONEY_SLOT_XPOS, MONEY_SLOT_YPOS));

            final int OUTPUT_SLOTS_XPOS = 134;
            final int OUTPUT_SLOTS_YPOS = 90;
            this.addSlot(new SlotItemHandler(this.outputInventory, 0, OUTPUT_SLOTS_XPOS, OUTPUT_SLOTS_YPOS));
        } else {
            throw new IllegalStateException("TileEntity is null");
        }
    }

    public ItemStack quickMoveStack(PlayerEntity playerEntity, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
        if(!isAllowedToTakeItems && !slot.getItemHandler().isItemValid(0, new ItemStack(Items.GOLD_INGOT))) { return ItemStack.EMPTY; }
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = slot.getItem();
            if (slotId < INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1) {
                if (!this.moveItemStackTo(itemstack1, INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId > INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1) {
            SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
            if (slot.getItemHandler() == inputInventory) {
                if(!isAllowedToTakeItems && clickTypeIn == ClickType.PICKUP) {
                    selectedSlot = slot;
                    ItemStack playerCarried = player.inventory.getCarried();
                    if(!playerCarried.isEmpty()){
                        player.inventory.setCarried(ItemStack.EMPTY);
                        for (int i = 0; i < playerInventory.getSlots(); i++){
                            playerInventory.setStackInSlot(i, playerInventory.getStackInSlot(i).isEmpty() ? playerCarried : playerInventory.getStackInSlot(i));
                        }
                    }
                    return ItemStack.EMPTY;
                }
            }
        }
        return this.doClick(slotId, dragType, clickTypeIn, player);
    }

    public boolean checkPlayerAllowedToChangeInv(UUID currentPlayer){
        isAllowedToTakeItems = currentPlayer.equals(tile.owner) && !buyTestMode_REMOVE_LATER;
        if(tile.getLevel().getPlayerByUUID(tile.owner) != null){
            String _ownerName = ITextComponent.Serializer.fromJsonLenient(tile.getLevel().getPlayerByUUID(tile.owner).getDisplayName().getString()).getString();
            String _openerName = ITextComponent.Serializer.fromJsonLenient(tile.getLevel().getPlayerByUUID(currentPlayer).getDisplayName().getString()).getString();
            LOGGER.info("Current player who has the GUI open is the owner: " + isAllowedToTakeItems + "\n owner: " + _ownerName + "(" + tile.owner + ")" + "\n opener: " + _openerName + "(" + currentPlayer + ")");
        }
        if(!isAllowedToTakeItems){
            selectedSlot = null;
        }
        return isAllowedToTakeItems;
    }

    @Override
    public boolean stillValid(PlayerEntity playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }

    public VendingMachineTile getTile(){
        return tile;
    }

    protected boolean moveItemStackTo(ItemStack stack, int slot, int slot2, boolean simulate) {
        boolean flag = false;
        int i = slot;
        if (simulate) {
            i = slot2 - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (simulate) {
                    if (i < slot) {
                        break;
                    }
                } else if (i >= slot2) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack = slot1.getItem();
                if (!itemstack.isEmpty() && consideredTheSameItem(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = slot1.getMaxStackSize();
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot1.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot1.setChanged();
                        flag = true;
                    }
                }

                if (simulate) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (simulate) {
                i = slot2 - 1;
            } else {
                i = slot;
            }

            while(true) {
                if (simulate) {
                    if (i < slot) {
                        break;
                    }
                } else if (i >= slot2) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (simulate) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    public SlotItemHandler MyHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition){
        return new SlotItemHandler(itemHandler, index, xPosition, yPosition){
            @Override
            public int getMaxStackSize(@Nonnull ItemStack stack)
            {
                ItemStack maxAdd = stack.copy();
                IItemHandler handler = this.getItemHandler();
                int maxInput = handler.getSlotLimit(0);
                maxAdd.setCount(maxInput);

                ItemStack currentStack = handler.getStackInSlot(index);
                if (handler instanceof IItemHandlerModifiable) {
                    IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;

                    handlerModifiable.setStackInSlot(index, ItemStack.EMPTY);

                    ItemStack remainder = handlerModifiable.insertItem(index, maxAdd, true);

                    handlerModifiable.setStackInSlot(index, currentStack);

                    return maxInput - remainder.getCount();
                }
                else
                {
                    ItemStack remainder = handler.insertItem(index, maxAdd, true);

                    int current = currentStack.getCount();
                    int added = maxInput - remainder.getCount();
                    return current + added;
                }
            }
        };
    }

    private ItemStack doClick(int slotId, int dragType, ClickType clickType, PlayerEntity playerEntity) {
        ItemStack itemstack = ItemStack.EMPTY;
        PlayerInventory playerinventory = playerEntity.inventory;
        if (clickType == ClickType.QUICK_CRAFT) {
            int i1 = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(dragType);
            if ((i1 != 1 || this.quickcraftStatus != 2) && i1 != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (playerinventory.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(dragType);
                if (isValidQuickcraftType(this.quickcraftType, playerEntity)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot7 = this.slots.get(slotId);
                ItemStack itemstack12 = playerinventory.getCarried();
                if (slot7 != null && canItemQuickReplace(slot7, itemstack12, true) && slot7.mayPlace(itemstack12) && (this.quickcraftType == 2 || itemstack12.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot7)) {
                    this.quickcraftSlots.add(slot7);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    ItemStack itemstack10 = playerinventory.getCarried().copy();
                    int k1 = playerinventory.getCarried().getCount();

                    for(Slot slot8 : this.quickcraftSlots) {
                        ItemStack itemstack13 = playerinventory.getCarried();
                        if (slot8 != null && canItemQuickReplace(slot8, itemstack13, true) && slot8.mayPlace(itemstack13) && (this.quickcraftType == 2 || itemstack13.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot8)) {
                            ItemStack itemstack14 = itemstack10.copy();
                            int j3 = slot8.hasItem() ? slot8.getItem().getCount() : 0;
                            getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemstack14, j3);
                            int k3 = slot8.getMaxStackSize(itemstack14);
                            if (itemstack14.getCount() > k3) {
                                itemstack14.setCount(k3);
                            }

                            k1 -= itemstack14.getCount() - j3;
                            slot8.set(itemstack14);
                        }
                    }

                    itemstack10.setCount(k1);
                    playerinventory.setCarried(itemstack10);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            if (slotId == -999) {
                if (!playerinventory.getCarried().isEmpty()) {
                    if (dragType == 0) {
                        playerEntity.drop(playerinventory.getCarried(), true);
                        playerinventory.setCarried(ItemStack.EMPTY);
                    }

                    if (dragType == 1) {
                        playerEntity.drop(playerinventory.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slot5 = this.slots.get(slotId);
                if (slot5 == null || !slot5.mayPickup(playerEntity)) {
                    return ItemStack.EMPTY;
                }

                for(ItemStack itemstack8 = this.quickMoveStack(playerEntity, slotId); !itemstack8.isEmpty() && ItemStack.isSame(slot5.getItem(), itemstack8); itemstack8 = this.quickMoveStack(playerEntity, slotId)) {
                    itemstack = itemstack8.copy();
                }
            } else {
                if (slotId < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slot6 = this.slots.get(slotId);
                if (slot6 != null) {
                    ItemStack itemstack9 = slot6.getItem();
                    ItemStack itemstack11 = playerinventory.getCarried();
                    if (!itemstack9.isEmpty()) {
                        itemstack = itemstack9.copy();
                    }

                    if (itemstack9.isEmpty()) {
                        if (!itemstack11.isEmpty() && slot6.mayPlace(itemstack11)) {
                            int j2 = dragType == 0 ? itemstack11.getCount() : 1;
                            if (j2 > slot6.getMaxStackSize(itemstack11)) {
                                j2 = slot6.getMaxStackSize(itemstack11);
                            }

                            slot6.set(itemstack11.split(j2));
                        }
                    } else if (slot6.mayPickup(playerEntity)) {
                        if (itemstack11.isEmpty()) {
                            if (itemstack9.isEmpty()) {
                                slot6.set(ItemStack.EMPTY);
                                playerinventory.setCarried(ItemStack.EMPTY);
                            } else {
                                int k2 = dragType == 0 ? itemstack9.getMaxStackSize() : (itemstack9.getMaxStackSize() + 1) / 2;
                                playerinventory.setCarried(slot6.remove(k2));
                                if (itemstack9.isEmpty()) {
                                    slot6.set(ItemStack.EMPTY);
                                }

                                slot6.onTake(playerEntity, playerinventory.getCarried());
                            }
                        } else if (slot6.mayPlace(itemstack11)) {
                            if (consideredTheSameItem(itemstack9, itemstack11)) {
                                int l2 = dragType == 0 ? itemstack11.getCount() : 1;
                                if (l2 > slot6.getMaxStackSize(itemstack11) - itemstack9.getCount()) {
                                    l2 = slot6.getMaxStackSize(itemstack11) - itemstack9.getCount();
                                }

                                itemstack11.shrink(l2);
                                itemstack9.grow(l2);
                            } else if (itemstack11.getCount() <= slot6.getMaxStackSize(itemstack11)) {
                                slot6.set(itemstack11);
                                playerinventory.setCarried(itemstack9);
                            }
                        } else if (itemstack11.getMaxStackSize() > 1 && consideredTheSameItem(itemstack9, itemstack11) && !itemstack9.isEmpty()) {
                            int i3 = itemstack9.getCount();
                            if (i3 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
                                itemstack11.grow(i3);
                                itemstack9 = slot6.remove(i3);
                                if (itemstack9.isEmpty()) {
                                    slot6.set(ItemStack.EMPTY);
                                }

                                slot6.onTake(playerEntity, playerinventory.getCarried());
                            }
                        }
                    }

                    slot6.setChanged();
                }
            }
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot2 = this.slots.get(slotId);
            ItemStack itemstack5 = playerinventory.getCarried();
            if (!itemstack5.isEmpty() && (slot2 == null || !slot2.hasItem() || !slot2.mayPickup(playerEntity))) {
                int j1 = dragType == 0 ? 0 : this.slots.size() - 1;
                int i2 = dragType == 0 ? 1 : -1;

                for(int j = 0; j < 2; ++j) {
                    for(int k = j1; k >= 0 && k < this.slots.size() && itemstack5.getCount() < slot2.getMaxStackSize(); k += i2) {
                        Slot slot1 = this.slots.get(k);
                        if (slot1.hasItem() && canItemQuickReplace(slot1, itemstack5, true) && slot1.mayPickup(playerEntity) && this.canTakeItemForPickAll(itemstack5, slot1)) {
                            ItemStack itemstack3 = slot1.getItem();
                            if (j != 0 || itemstack3.getCount() != slot1.getMaxStackSize()) {
                                int l = Math.min(itemstack5.getMaxStackSize() - itemstack5.getCount(), itemstack3.getCount());
                                ItemStack itemstack4 = slot1.remove(l);
                                itemstack5.grow(l);
                                if (itemstack4.isEmpty()) {
                                    slot1.set(ItemStack.EMPTY);
                                }

                                slot1.onTake(playerEntity, itemstack4);
                            }
                        }
                    }
                }
            }
            this.broadcastChanges();

        } else {
            super.clicked(slotId, dragType, clickType, playerEntity);
        }

        return itemstack;
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack stack, boolean simulate) {
        boolean flag = slot == null || !slot.hasItem();
        if (!flag && stack.sameItem(slot.getItem()) && ItemStack.tagMatches(slot.getItem(), stack)) {
            return slot.getItem().getCount() + (simulate ? 0 : stack.getCount()) <= slot.getMaxStackSize();
        } else {
            return flag;
        }
    }
}
