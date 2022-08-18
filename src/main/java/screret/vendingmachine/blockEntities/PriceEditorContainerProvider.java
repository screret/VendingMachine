package screret.vendingmachine.blockEntities;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import screret.vendingmachine.containers.VenderPriceEditorMenu;

public class PriceEditorContainerProvider implements MenuProvider {
    final VendingMachineBlockEntity tile;

    VenderPriceEditorMenu container;

    public PriceEditorContainerProvider(VendingMachineBlockEntity tile){
        this.tile = tile;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.vendingmachine.vendingmachine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        container = new VenderPriceEditorMenu(windowID, playerInventory, this.tile.inventory, tile);
        return container;
    }
}
