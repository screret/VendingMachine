package screret.vendingmachine.tileEntities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import screret.vendingmachine.containers.VenderPriceEditorContainer;

public class PriceEditorContainerProvider implements INamedContainerProvider {
    final VendingMachineTile tile;

    public PriceEditorContainerProvider(VendingMachineTile tile){
        this.tile = tile;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.vendingmachine.vendingmachine");
    }

    @Override
    public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new VenderPriceEditorContainer(windowID, playerInventory, tile);
    }
}
