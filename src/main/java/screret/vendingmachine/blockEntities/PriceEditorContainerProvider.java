package screret.vendingmachine.blockEntities;

import net.minecraft.network.chat.Component;
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
    public Component getDisplayName() {
        return Component.translatable("gui.vendingmachine.vendingmachine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        container = new VenderPriceEditorContainer(windowID, playerInventory, this.tile.inventory, tile);
        return container;
    }
}
