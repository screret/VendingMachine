package screret.vendingmachine.containers;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderPriceEditorContainer extends AbstractContainerMenu {
    private final PlayerInvWrapper playerInventory;
    final VendingMachineTile tile;

    private Runnable slotUpdateListener = () -> { };

    public int selectedItemIndex = -1;

    public VenderPriceEditorContainer(int windowID, Inventory inv, VendingMachineTile tile) {
        super(Registration.VENDER_CONT_PRICES.get(), windowID);
        this.tile = tile;
        this.playerInventory = new PlayerInvWrapper(inv);

        //layoutPlayerInventorySlots(8, 140);
    }

    public boolean hasPricesSet(){
        return tile.getPrices().size() > 0;
    }

    public VendingMachineTile getTile(){
        return tile;
    }

    @Override
    public boolean clickMenuButton(Player playerEntity, int index) {
        if (this.isValidPriceIndex(index)) {
            this.selectedItemIndex = index;
        }
        return true;
    }

    private boolean isValidPriceIndex(int index) {
        return index >= 0 && index < this.tile.getPrices().size();
    }

    @Override
    public boolean stillValid(Player playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }

    public void registerUpdateListener(Runnable runnable) {
        this.slotUpdateListener = runnable;
    }

    public void updateGUI(){
        this.slotUpdateListener.run();
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
}
