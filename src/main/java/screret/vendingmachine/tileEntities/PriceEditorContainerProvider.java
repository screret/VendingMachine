package screret.vendingmachine.tileEntities;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import screret.vendingmachine.containers.VenderPriceEditorContainer;

public class PriceEditorContainerProvider implements MenuProvider {
    final VendingMachineTile tile;

    VenderPriceEditorContainer container;

    public PriceEditorContainerProvider(VendingMachineTile tile){
        this.tile = tile;
    }

    @Override
    public BaseComponent getDisplayName() {
        return new TranslatableComponent("gui.vendingmachine.vendingmachine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        container = new VenderPriceEditorContainer(windowID, playerInventory, this.tile.inputSlot, tile);
        return container;
    }
}
