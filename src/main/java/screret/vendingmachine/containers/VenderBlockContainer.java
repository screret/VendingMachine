package screret.vendingmachine.containers;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class VenderBlockContainer extends AbstractContainerMenu {

    private final PlayerInvWrapper playerInventory;
    public final LargeStackHandler inventory;
    public final ItemStackHandler otherSlots;

    public boolean isAllowedToTakeItems = false;

    public SlotItemHandler selectedSlot;
    public UUID currentPlayer;
    private final VendingMachineBlockEntity tile;

    public static final int INPUT_SLOTS_X_AMOUNT = 5;
    public static final int INPUT_SLOTS_Y_AMOUNT = 6;
    public static int MONEY_SLOT_INDEX, OUTPUT_SLOT_INDEX, LAST_CONTAINER_SLOT_INDEX;

    public VenderBlockContainer(int windowID, Inventory playerInventory, LargeStackHandler inventory, ItemStackHandler otherSlots, VendingMachineBlockEntity tileEntity) {
        super(Registration.VENDER_CONT.get(), windowID);
        this.playerInventory = new PlayerInvWrapper(playerInventory);
        this.tile = tileEntity;
        this.inventory = inventory;
        this.otherSlots = otherSlots;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        if (tileEntity != null) {
            final int INPUT_SLOTS_XPOS = 8;
            final int INPUT_SLOTS_YPOS = 18;
            final int MONEY_SLOT_XPOS = 134;
            final int MONEY_SLOT_YPOS = 36;
            final int OUTPUT_SLOTS_XPOS = 134;
            final int OUTPUT_SLOTS_YPOS = 81;

            currentPlayer = playerInventory.player.getUUID();
            checkPlayerAllowedToChangeInv(currentPlayer);

            for(int x = 0; x < INPUT_SLOTS_X_AMOUNT; x++){
                for(int y = 0; y < INPUT_SLOTS_Y_AMOUNT; y++) {
                    int slotNumber = y * (INPUT_SLOTS_Y_AMOUNT - 1) + x;
                    int index = this.addSlot(MyHandler(this.inventory, slotNumber, INPUT_SLOTS_XPOS + SLOT_X_SPACING * x, INPUT_SLOTS_YPOS + SLOT_Y_SPACING * y, isAllowedToTakeItems)).index;
                    LAST_CONTAINER_SLOT_INDEX = Math.max(LAST_CONTAINER_SLOT_INDEX, index);
                }
            }
            ++LAST_CONTAINER_SLOT_INDEX;
            MONEY_SLOT_INDEX = this.addSlot(MoneyHandler(this.otherSlots, 0, MONEY_SLOT_XPOS, MONEY_SLOT_YPOS)).index;

            OUTPUT_SLOT_INDEX = this.addSlot(OutputHandler(this.otherSlots, 1, OUTPUT_SLOTS_XPOS, OUTPUT_SLOTS_YPOS)).index;

            layoutPlayerInventorySlots(8, 140);
        } else {
            throw new IllegalStateException("TileEntity is null");
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
        if((slot.getItemHandler() != playerInventory && slot.getItemHandler() != otherSlots) && !isAllowedToTakeItems  && !slot.mayPlace(new ItemStack(VendingMachineConfig.getPaymentItem()))) { return ItemStack.EMPTY; }
        if (slot != null && slot.hasItem()) {
            int lastInvIndex = LAST_CONTAINER_SLOT_INDEX + 36;
            int lastInvIndexNoHotbar = LAST_CONTAINER_SLOT_INDEX + 27;

            ItemStack itemstack1 = slot.getItem();
            itemstack = slot.getItem();
            if(slot.getItemHandler() == otherSlots && slotId == OUTPUT_SLOT_INDEX){
                if(!this.moveItemStackTo(itemstack1, OUTPUT_SLOT_INDEX, playerInventory.getSlots(), false)){
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (slotId != OUTPUT_SLOT_INDEX && slotId != MONEY_SLOT_INDEX) {
                if(itemstack1.is(VendingMachineConfig.getPaymentItem())){
                    if(!this.moveItemStackTo(itemstack1, OUTPUT_SLOT_INDEX, MONEY_SLOT_INDEX, false)){
                        return ItemStack.EMPTY;
                    }
                }else if(isAllowedToTakeItems){
                    if(!this.moveItemStackTo(itemstack1, MONEY_SLOT_INDEX, LAST_CONTAINER_SLOT_INDEX, false)){
                        return ItemStack.EMPTY;
                    }
                } else if (slotId >= LAST_CONTAINER_SLOT_INDEX && slotId < lastInvIndexNoHotbar) {
                    if (!this.moveItemStackTo(itemstack1, lastInvIndexNoHotbar, lastInvIndex, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotId >= LAST_CONTAINER_SLOT_INDEX && slotId < lastInvIndex && !this.moveItemStackTo(itemstack1, LAST_CONTAINER_SLOT_INDEX, lastInvIndex, false)) {
                    return ItemStack.EMPTY;
                }
            }else if (!this.moveItemStackTo(itemstack1, LAST_CONTAINER_SLOT_INDEX, lastInvIndex, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
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
        if(slotId < LAST_CONTAINER_SLOT_INDEX){
            this.doClick(slotId, dragType, clickTypeIn, player);
            return;
        }

        SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
        if (slot.getItemHandler() == inventory) {
            if(!checkPlayerAllowedToChangeInv(player.getUUID())) {
                selectedSlot = slot;
                return;
            }
        }
        this.doClick(slotId, dragType, clickTypeIn, player);
    }

    public boolean checkPlayerAllowedToChangeInv(UUID currentPlayer) {
        isAllowedToTakeItems = currentPlayer.equals(tile.owner);
        if(!isAllowedToTakeItems){
            selectedSlot = null;
        }
        return isAllowedToTakeItems;
    }

    @Override
    public boolean stillValid(Player playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }

    public VendingMachineBlockEntity getTile(){
        return tile;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int slotIn, int slotOut, boolean simulate) {
        boolean flag = false;
        int i = slotIn;
        if (simulate) {
            i = slotOut - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (simulate) {
                    if (i < slotIn) {
                        break;
                    }
                } else if (i >= slotOut) {
                    break;
                }

                SlotItemHandler slot = (SlotItemHandler) this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if(slot.getItemHandler() == this.inventory && i < LAST_CONTAINER_SLOT_INDEX && !isAllowedToTakeItems){
                    return false;
                }

                if (itemstack.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.set(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(stack.split(stack.getCount()));
                    }

                    slot.setChanged();
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

        if (!stack.isEmpty()) {
            if (simulate) {
                i = slotOut - 1;
            } else {
                i = slotIn;
            }

            while(true) {
                if (simulate) {
                    if (i < slotIn) {
                        break;
                    }
                } else if (i >= slotOut) {
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

    public SlotItemHandler MyHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean isAllowedToTakeItems){
        return new SlotItemHandler(itemHandler, index, xPosition, yPosition){
            @Override
            public int getMaxStackSize(@Nonnull ItemStack stack)
            {
                ItemStack maxAdd = stack.copy();
                IItemHandler handler = this.getItemHandler();
                int maxInput = handler.getSlotLimit(0);
                maxAdd.setCount(maxInput);

                ItemStack currentStack = handler.getStackInSlot(index);
                if (handler instanceof IItemHandlerModifiable handlerModifiable) {

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

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return isAllowedToTakeItems;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return isAllowedToTakeItems;
            }
        };
    }

    public SlotItemHandler OutputHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition){
        return new SlotItemHandler(itemHandler, index, xPosition, yPosition){
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        };
    }

    public SlotItemHandler MoneyHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition){
        return new SlotItemHandler(itemHandler, index, xPosition, yPosition){
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                //return ItemStack.isSame(stack, VendingMachineConfig.getPaymentItem().getDefaultInstance());
                return stack.is(VendingMachineConfig.getPaymentItem());
            }
        };
    }

    @Override
    protected void doClick(int slotId, int dragType, ClickType clickType, Player player) {
        Inventory inventory = player.getInventory();
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
                            int k = slot1.getMaxStackSize(itemstack2);
                            if (itemstack2.getCount() > k) {
                                itemstack2.setCount(k);
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

                SlotItemHandler slot7 = (SlotItemHandler) this.slots.get(slotId);
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
        } else if (clickType == ClickType.SWAP) {
            SlotItemHandler slot2 = (SlotItemHandler) this.slots.get(slotId);
            ItemStack itemstack4 = inventory.getItem(dragType);
            ItemStack itemstack7 = slot2.getItem();
            if (!itemstack4.isEmpty() || !itemstack7.isEmpty()) {
                if (itemstack4.isEmpty()) {
                    if (slot2.mayPickup(player)) {
                        inventory.setItem(dragType, itemstack7);
                        slot2.onSwapCraft(itemstack7.getCount());
                        slot2.set(ItemStack.EMPTY);
                        slot2.onTake(player, itemstack7);
                    }
                } else if (itemstack7.isEmpty()) {
                    if (slot2.mayPlace(itemstack4)) {
                        int l1 = slot2.getMaxStackSize(itemstack4);
                        if (itemstack4.getCount() > l1) {
                            slot2.set(itemstack4.split(l1));
                        } else {
                            inventory.setItem(dragType, ItemStack.EMPTY);
                            slot2.set(itemstack4);
                        }
                    }
                } else if (slot2.mayPickup(player) && slot2.mayPlace(itemstack4)) {
                    int i2 = slot2.getMaxStackSize(itemstack4);
                    if (itemstack4.getCount() > i2) {
                        slot2.set(itemstack4.split(i2));
                        slot2.onTake(player, itemstack7);
                        if (!inventory.add(itemstack7)) {
                            player.drop(itemstack7, true);
                        }
                    } else {
                        inventory.setItem(dragType, itemstack7);
                        slot2.set(itemstack4);
                        slot2.onTake(player, itemstack7);
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slotId >= 0) {
            SlotItemHandler slot5 = (SlotItemHandler) this.slots.get(slotId);
            if (slot5.hasItem()) {
                ItemStack itemstack6 = slot5.getItem().copy();
                itemstack6.setCount(itemstack6.getMaxStackSize());
                this.setCarried(itemstack6);
            }
        } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot4 = this.slots.get(slotId);
            int i1 = dragType == 0 ? 1 : slot4.getItem().getCount();
            ItemStack itemstack8 = slot4.safeTake(i1, Integer.MAX_VALUE, player);
            player.drop(itemstack8, true);
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            SlotItemHandler slot3 = (SlotItemHandler) this.slots.get(slotId);
            ItemStack itemstack5 = this.getCarried();
            if (!itemstack5.isEmpty() && (!slot3.hasItem() || !slot3.mayPickup(player))) {
                int k1 = dragType == 0 ? 0 : this.slots.size() - 1;
                int j2 = dragType == 0 ? 1 : -1;

                for(int k2 = 0; k2 < 2; ++k2) {
                    for(int k3 = k1; k3 >= 0 && k3 < this.slots.size() && itemstack5.getCount() < itemstack5.getMaxStackSize(); k3 += j2) {
                        SlotItemHandler slot8 = (SlotItemHandler) this.slots.get(k3);
                        if (slot8.hasItem() && canItemQuickReplace(slot8, itemstack5, true) && slot8.mayPickup(player) && this.canTakeItemForPickAll(itemstack5, slot8)) {
                            ItemStack itemstack12 = slot8.getItem();
                            if (k2 != 0 || itemstack12.getCount() != itemstack12.getMaxStackSize()) {
                                ItemStack itemstack13 = slot8.safeTake(itemstack12.getCount(), itemstack5.getMaxStackSize() - itemstack5.getCount(), player);
                                itemstack5.grow(itemstack13.getCount());
                            }
                        }
                    }
                }
            }
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
