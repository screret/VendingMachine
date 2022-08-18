package screret.vendingmachine.containers;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.containers.stackhandlers.LargeStackHandler;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import java.util.UUID;

import static screret.vendingmachine.containers.VenderBlockMenu.*;

public class VenderPriceEditorMenu extends AbstractContainerMenu {
    private final PlayerInvWrapper playerInventory;
    public final LargeStackHandler inputInventory;
    final VendingMachineBlockEntity tile;

    public boolean isAllowedToTakeItems = false;
    public SlotItemHandler selectedSlot;


    public VenderPriceEditorMenu(int windowID, Inventory inv, LargeStackHandler inputInv, VendingMachineBlockEntity tileEntity) {
        super(Registration.VENDER_PRICES_MENU.get(), windowID);
        this.tile = tileEntity;
        this.playerInventory = new PlayerInvWrapper(inv);
        this.inputInventory = inputInv;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        if (tileEntity != null) {
            final int INPUT_SLOTS_XPOS = 8;
            final int INPUT_SLOTS_YPOS = 18;

            for(int y = 0; y < INPUT_SLOTS_Y_AMOUNT; y++){
                for(int x = 0; x < INPUT_SLOTS_X_AMOUNT; x++) {
                    int slotNumber = x + y * INPUT_SLOTS_X_AMOUNT;
                    this.addSlot(MyHandler(this.inputInventory, slotNumber, INPUT_SLOTS_XPOS + SLOT_X_SPACING * x, INPUT_SLOTS_YPOS + SLOT_Y_SPACING * y));
                }
            }
            checkPlayerAllowedToChangeInv(inv.player.getUUID());

            layoutPlayerInventorySlots(8, 140);
        } else {
            throw new IllegalStateException("TileEntity is null");
        }

        layoutPlayerInventorySlots(8, 140);
    }

    public VendingMachineBlockEntity getTile(){
        return tile;
    }

    public ItemStack quickMoveStack(Player p_39253_, int p_39254_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39254_);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (p_39254_ < LAST_CONTAINER_SLOT_INDEX) {
                if (!this.moveItemStackTo(itemstack1, LAST_CONTAINER_SLOT_INDEX + 1, LAST_CONTAINER_SLOT_INDEX + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, LAST_CONTAINER_SLOT_INDEX + 1, false)) {
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

    @Override
    public boolean stillValid(Player playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
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

    public boolean checkPlayerAllowedToChangeInv(UUID currentPlayer) {
        isAllowedToTakeItems = currentPlayer.equals(tile.owner);
        if(!isAllowedToTakeItems){
            selectedSlot = null;
        }
        return isAllowedToTakeItems;
    }

    public SlotItemHandler MyHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition){
        return new SlotItemHandler(itemHandler, index, xPosition, yPosition){
            /*@Override
            public int getMaxStackSize(@Nonnull ItemStack stack)
            {
                return 1;
            }//*/

            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        };
    }
}
