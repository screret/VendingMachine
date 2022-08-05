package screret.vendingmachine.containers;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.UUID;

import static screret.vendingmachine.containers.VenderBlockContainer.*;

public class VenderPriceEditorContainer extends AbstractContainerMenu {
    private final PlayerInvWrapper playerInventory;
    public final OwnedStackHandler inputInventory;
    final VendingMachineTile tile;

    public boolean isAllowedToTakeItems = false;
    public SlotItemHandler selectedSlot;


    public VenderPriceEditorContainer(int windowID, Inventory inv, OwnedStackHandler inputInv, VendingMachineTile tileEntity) {
        super(Registration.VENDER_CONT_PRICES.get(), windowID);
        this.tile = tileEntity;
        this.playerInventory = new PlayerInvWrapper(inv);
        this.inputInventory = inputInv;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        if (tileEntity != null) {
            final int INPUT_SLOTS_XPOS = 8;
            final int INPUT_SLOTS_YPOS = 18;

            for(int x = 0; x < INPUT_SLOTS_X_AMOUNT_PLUS_1; x++){
                for(int y = 0; y < INPUT_SLOTS_Y_AMOUNT_PLUS_1; y++) {
                    int slotNumber = y * INPUT_SLOTS_Y_AMOUNT + x;
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

    public VendingMachineTile getTile(){
        return tile;
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
        isAllowedToTakeItems = currentPlayer.equals(tile.owner) && !buyTestMode_REMOVE_LATER;
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
