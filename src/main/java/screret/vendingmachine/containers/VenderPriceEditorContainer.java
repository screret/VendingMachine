package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderPriceEditorContainer extends Container {
    private final PlayerInvWrapper playerInventory;
    final VendingMachineTile tile;

    public int selectedItemIndex;

    public VenderPriceEditorContainer(int windowID, PlayerInventory inv, VendingMachineTile tile) {
        super(Registration.VENDER_CONT_PRICES.get(), windowID);
        this.tile = tile;
        this.playerInventory = new PlayerInvWrapper(inv);

        layoutPlayerInventorySlots(8, 140);
    }

    public boolean hasPricesSet(){
        return tile.priceHashMap.size() > 0;
    }

    public VendingMachineTile getTile(){
        return tile;
    }

    @Override
    public boolean clickMenuButton(PlayerEntity playerEntity, int index) {
        if (this.isValidItemIndex(index)) {
            this.selectedItemIndex = index;
        }
        return true;
    }

    private boolean isValidItemIndex(int index) {
        return index >= 0 && index < this.tile.priceHashMap.size();
    }

    @Override
    public boolean stillValid(PlayerEntity playerEntity) {
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
}
