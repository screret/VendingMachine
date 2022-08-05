package screret.vendingmachine.containers;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class VenderBlockContainer extends AbstractContainerMenu {

    private final PlayerInvWrapper playerInventory;
    public final OwnedStackHandler inputInventory;
    private final ItemStackHandlerMoney moneyInventory;
    private final IItemHandler outputInventory;

    public boolean isAllowedToTakeItems = false;
    public static boolean buyTestMode_REMOVE_LATER = false;

    public SlotItemHandler selectedSlot;
    public UUID currentPlayer;
    private final VendingMachineTile tile;

    public static final int INPUT_SLOTS_X_AMOUNT = 4;
    public static final int INPUT_SLOTS_X_AMOUNT_PLUS_1 = INPUT_SLOTS_X_AMOUNT + 1;
    public static final int INPUT_SLOTS_Y_AMOUNT = 5;
    public static final int INPUT_SLOTS_Y_AMOUNT_PLUS_1 = INPUT_SLOTS_Y_AMOUNT + 1;

    public VenderBlockContainer(int windowID, Inventory playerInventory, OwnedStackHandler inputInv, IItemHandler outputInv, ItemStackHandlerMoney moneyInv, VendingMachineTile tileEntity) {
        super(Registration.VENDER_CONT.get(), windowID);
        this.playerInventory = new PlayerInvWrapper(playerInventory);
        this.tile = tileEntity;
        this.inputInventory = inputInv;
        this.moneyInventory = moneyInv;
        this.outputInventory = outputInv;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

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

            this.addSlot(MyHandler(this.moneyInventory, 0, MONEY_SLOT_XPOS, MONEY_SLOT_YPOS));

            final int OUTPUT_SLOTS_XPOS = 134;
            final int OUTPUT_SLOTS_YPOS = 90;
            this.addSlot(new SlotItemHandler(this.outputInventory, 0, OUTPUT_SLOTS_XPOS, OUTPUT_SLOTS_YPOS));

            layoutPlayerInventorySlots(8, 140);
        } else {
            throw new IllegalStateException("TileEntity is null");
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerEntity, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
        if((slot.getItemHandler() != playerInventory && slot.getItemHandler() != moneyInventory) && !isAllowedToTakeItems && !slot.getItemHandler().isItemValid(0, new ItemStack(VendingMachineConfig.PAYMENT_ITEM))) { return ItemStack.EMPTY; }
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
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId > INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1) {
            SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
            if(!checkPlayerAllowedToChangeInv(player.getUUID())) {
                if (slot.getItemHandler() == inputInventory) {
                    selectedSlot = slot;
                    return;
                }
            }
        }
        this.doClick(slotId, dragType, clickTypeIn, player);
    }

    public boolean checkPlayerAllowedToChangeInv(UUID currentPlayer) {
        isAllowedToTakeItems = currentPlayer.equals(tile.owner) && !buyTestMode_REMOVE_LATER;
        if(!isAllowedToTakeItems){
            selectedSlot = null;
        }
        return isAllowedToTakeItems;
    }

    @Override
    public boolean stillValid(Player playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }

    public VendingMachineTile getTile(){
        return tile;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int slotIndex1, int slotIndex2, boolean simulate) {
        boolean flag = false;
        int i = slotIndex1;
        if (simulate) {
            i = slotIndex2 - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (simulate) {
                    if (i < slotIndex1) {
                        break;
                    }
                } else if (i >= slotIndex2) {
                    break;
                }

                SlotItemHandler slot = (SlotItemHandler) this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if(slot.getItemHandler() == inputInventory && !isAllowedToTakeItems){
                    return false;
                }

                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = slot.getMaxStackSize();
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
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
                i = slotIndex2 - 1;
            } else {
                i = slotIndex1;
            }

            while(true) {
                if (simulate) {
                    if (i < slotIndex1) {
                        break;
                    }
                } else if (i >= slotIndex2) {
                    break;
                }

                SlotItemHandler slot1 = (SlotItemHandler) this.slots.get(i);
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

    @Override
    protected void doClick(int slotId, int dragType, ClickType clickType, Player player) {
        InventoryMenu playerInventory = player.inventoryMenu;
        if (clickType == ClickType.QUICK_CRAFT) {
            int i = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(dragType);
            if ((i != QUICKCRAFT_HEADER_CONTINUE || this.quickcraftStatus != QUICKCRAFT_HEADER_END) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == QUICKCRAFT_HEADER_START) {
                this.quickcraftType = getQuickcraftType(dragType);
                if (isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == QUICKCRAFT_HEADER_CONTINUE) {
                Slot slot = this.slots.get(slotId);
                ItemStack itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && (this.quickcraftType == QUICKCRAFT_TYPE_CLONE || itemstack.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == QUICKCRAFT_HEADER_END) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int l = (this.quickcraftSlots.iterator().next()).index;
                        this.resetQuickCraft();
                        this.doClick(l, this.quickcraftType, ClickType.PICKUP, player);
                        return;
                    }

                    ItemStack itemstack3 = this.getCarried().copy();
                    int j1 = this.getCarried().getCount();

                    for(Slot slot1 : this.quickcraftSlots) {
                        ItemStack itemstack1 = this.getCarried();
                        if (slot1 != null && canItemQuickReplace(slot1, itemstack1, true) && slot1.mayPlace(itemstack1) && (this.quickcraftType == QUICKCRAFT_TYPE_CLONE || itemstack1.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot1)) {
                            ItemStack itemstack2 = itemstack3.copy();
                            int j = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemstack2, j);
                            if (itemstack2.getCount() > slot1.getMaxStackSize(itemstack2)) {
                                itemstack2.setCount(slot1.getMaxStackSize(itemstack2));
                            }

                            j1 -= itemstack2.getCount() - j;
                            slot1.set(itemstack2);
                        }
                    }

                    itemstack3.setCount(j1);
                    this.setCarried(itemstack3);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != QUICKCRAFT_HEADER_START) {
            this.resetQuickCraft();
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            ClickAction clickaction = dragType == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (clickaction == ClickAction.PRIMARY) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }

                SlotItemHandler slot6 = (SlotItemHandler) this.slots.get(slotId);
                if (!slot6.mayPickup(player)) {
                    return;
                }

                ItemStack itemstack9 = this.quickMoveStack(player, slotId);
                while (!itemstack9.isEmpty() && ItemStack.isSame(slot6.getItem(), itemstack9)) {
                    itemstack9 = this.quickMoveStack(player, slotId);
                }
            } else {
                if (slotId < 0) {
                    return;
                }

                Slot slot7 = this.slots.get(slotId);
                ItemStack itemstack10 = slot7.getItem();
                ItemStack itemstack11 = this.getCarried();
                player.updateTutorialInventoryAction(itemstack11, slot7.getItem(), clickaction);
                if (!itemstack11.overrideStackedOnOther(slot7, clickaction, player) && !itemstack10.overrideOtherStackedOnMe(itemstack11, slot7, clickaction, player, this.createCarriedSlotAccess())) {
                    if (itemstack10.isEmpty()) {
                        if (!itemstack11.isEmpty()) {
                            int l2 = clickaction == ClickAction.PRIMARY ? itemstack11.getCount() : 1;
                            this.setCarried(slot7.safeInsert(itemstack11, l2));
                        }
                    } else if (slot7.mayPickup(player)) {
                        if (itemstack11.isEmpty()) {
                            int i3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : (itemstack10.getCount() + 1) / 2;
                            Optional<ItemStack> optional1 = slot7.tryRemove(i3, Integer.MAX_VALUE, player);
                            optional1.ifPresent((p_150421_) -> {
                                this.setCarried(p_150421_);
                                slot7.onTake(player, p_150421_);
                            });
                        } else if (slot7.mayPlace(itemstack11)) {
                            if (ItemStack.isSameItemSameTags(itemstack10, itemstack11)) {
                                int j3 = clickaction == ClickAction.PRIMARY ? itemstack11.getCount() : 1;
                                this.setCarried(slot7.safeInsert(itemstack11, j3));
                            } else if (itemstack11.getCount() <= slot7.getMaxStackSize(itemstack11)) {
                                slot7.set(itemstack11);
                                this.setCarried(itemstack10);
                            }
                        } else if (ItemStack.isSameItemSameTags(itemstack10, itemstack11)) {
                            Optional<ItemStack> optional = slot7.tryRemove(itemstack10.getCount(), slot7.getMaxStackSize(itemstack11) - itemstack11.getCount(), player);
                            optional.ifPresent((p_150428_) -> {
                                itemstack11.grow(p_150428_.getCount());
                                slot7.onTake(player, p_150428_);
                            });
                        }
                    }
                }

                slot7.setChanged();
            }
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot2 = this.slots.get(slotId);
            ItemStack itemstack5 = playerInventory.getCarried();
            if (!itemstack5.isEmpty() && (slot2 == null || !slot2.hasItem() || !slot2.mayPickup(player))) {
                int j1 = dragType == 0 ? 0 : this.slots.size() - 1;
                int i2 = dragType == 0 ? 1 : -1;

                for(int j = 0; j < 2; ++j) {
                    for(int k = j1; k >= 0 && k < this.slots.size() && itemstack5.getCount() < itemstack5.getMaxStackSize(); k += i2) {
                        Slot slot1 = this.slots.get(k);
                        if (slot1.hasItem() && canItemQuickReplace(slot1, itemstack5, true) && slot1.mayPickup(player) && this.canTakeItemForPickAll(itemstack5, slot1)) {
                            ItemStack itemstack3 = slot1.getItem();
                            if (j != 0 || itemstack3.getCount() != itemstack3.getMaxStackSize()) {
                                int l = Math.min(itemstack5.getMaxStackSize() - itemstack5.getCount(), itemstack3.getCount());
                                ItemStack itemstack4 = slot1.remove(l);
                                itemstack5.grow(l);
                                if (itemstack4.isEmpty()) {
                                    slot1.set(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack4);
                            }
                        }
                    }
                }
            }
            this.broadcastChanges();

        } else {
            super.doClick(slotId, dragType, clickType, player);
        }

    }

    public static boolean canItemQuickReplace(@Nullable SlotItemHandler slot, ItemStack stack, boolean simulate) {
        boolean flag = slot == null || !slot.hasItem();
        if (!flag && stack.sameItem(slot.getItem()) && ItemStack.tagMatches(slot.getItem(), stack)) {
            return slot.getItem().getCount() + (simulate ? 0 : stack.getCount()) <= slot.getMaxStackSize();
        } else {
            return flag;
        }
    }


    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess() {
            public ItemStack get() {
                return VenderBlockContainer.this.getCarried();
            }

            public boolean set(ItemStack stack) {
                VenderBlockContainer.this.setCarried(stack);
                return true;
            }
        };
    }
}
