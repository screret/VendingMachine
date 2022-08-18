package screret.vendingmachine.integration.polymorph;

import top.theillusivec4.polymorph.client.recipe.widget.PlayerRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public class CashConverterWidget extends PlayerRecipesWidget {

    public CashConverterWidget(AbstractContainerScreen<?> containerScreen, Slot outputSlot) {
        super(containerScreen, outputSlot);
    }

    @Override
    public int getXPos() {
        return getOutputSlot().x + 22;
    }

    @Override
    public int getYPos() {
        return getOutputSlot().y;
    }
}
