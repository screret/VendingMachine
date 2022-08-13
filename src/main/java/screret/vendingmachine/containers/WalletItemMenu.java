package screret.vendingmachine.containers;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.containers.stackhandlers.WalletStackHandler;
import screret.vendingmachine.init.Registration;

import java.util.Optional;

public class WalletItemMenu extends AbstractContainerMenu {

    private final PlayerInvWrapper playerInventory;
    private final Player player;
    private final WalletStackHandler inventory;

    private static final int SLOTS_X_AMOUNT = 9, SLOTS_Y_AMOUNT = 2, SLOTS_X_POS = 8, SLOTS_Y_POS = 15, INV_SLOTS_Y_POS = 62, SLOT_X_SPACING = 18, SLOT_Y_SPACING = 18;
    private static final int LAST_CONTAINER_SLOT_INDEX = SLOTS_X_AMOUNT * SLOTS_Y_AMOUNT;

    public WalletItemMenu(int containerId, Inventory playerInv, ItemStack heldItem) {
        super(Registration.WALLET_MENU.get(), containerId);
        this.playerInventory = new PlayerInvWrapper(playerInv);
        this.player = playerInv.player;

        if(heldItem.is(Registration.WALLET.get())){
            this.inventory = (WalletStackHandler) heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new IllegalStateException("Wallet doesn't have inventory"));

            for (int y = 0; y < SLOTS_Y_AMOUNT; ++y) {
                for (int x = 0; x < SLOTS_X_AMOUNT; ++x) {
                    int slotNumber = y + x * SLOTS_X_AMOUNT;
                    this.addSlot(new SlotItemHandler(this.inventory, slotNumber, SLOTS_X_POS + SLOT_X_SPACING * y, SLOTS_Y_POS + SLOT_Y_SPACING * x));
                }
            }

            layoutPlayerInventorySlots(SLOTS_X_POS, INV_SLOTS_Y_POS);
        } else {
            throw new IllegalStateException("HeldItem is null or HeldItem is not a Wallet");
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index < LAST_CONTAINER_SLOT_INDEX) {
                if (!this.moveItemStackTo(slotStack, LAST_CONTAINER_SLOT_INDEX, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, LAST_CONTAINER_SLOT_INDEX, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
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

    @Override
    public boolean stillValid(Player player) {
        return player.containerMenu instanceof WalletItemMenu;
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

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess() {
            public ItemStack get() {
                return WalletItemMenu.this.getCarried();
            }

            public boolean set(ItemStack stack) {
                WalletItemMenu.this.setCarried(stack);
                return true;
            }
        };
    }
}
