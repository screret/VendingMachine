package screret.vendingmachine.blockEntities;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import screret.vendingmachine.containers.VenderPriceEditorContainer;

public class PriceEditorContainerProvider implements MenuProvider {
    final VendingMachineBlockEntity tile;

    VenderPriceEditorContainer container;

    public PriceEditorContainerProvider(VendingMachineBlockEntity tile){
        this.tile = tile;
    }

    @Override
    public BaseComponent getDisplayName() {
        return new TranslatableComponent("gui.vendingmachine.vendingmachine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        container = new VenderPriceEditorContainer(windowID, playerInventory, this.tile.inventory, tile);
        return container;
    }
}
