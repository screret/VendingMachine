package screret.vendingmachine.integration.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import screret.vendingmachine.containers.CashConverterMenu;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.recipes.MoneyConversionRecipe;

public class VendingMachinesCompatibilityModule extends AbstractCompatibilityModule {

    @Override
    public void setup() {
        PolymorphApi.common().registerItemStack2RecipeData(pStack -> {
            if (pStack.is(Registration.MONEY.get())) {
                return new ConversionStackRecipeData(pStack);
            }
            return null;
        });
    }

    @Override
    public void clientSetup() {
        PolymorphApi.client().registerWidget(pContainerScreen -> {
            if (pContainerScreen.getMenu() instanceof CashConverterMenu container) {
                return new CashConverterWidget(pContainerScreen, container.getSlot(container.slotOutputIndex));
            }
            return null;
        });
    }

    @Override
    public boolean selectRecipe(AbstractContainerMenu container, Recipe<?> recipe) {
        if (recipe instanceof MoneyConversionRecipe realRecipe) {
            if (container instanceof CashConverterMenu realContainer) {
                CraftingContainer inputSlot = realContainer.getInputSlot();
                var outputSlot = realContainer.getOutputSlot();
                var player = realContainer.getPlayer();
                if (outputSlot.setRecipeUsed(player.getLevel(), (ServerPlayer) player, realRecipe)) {
                    realContainer.slotsChanged(inputSlot);
                    return true;
                }
                //realContainer.getOutputSlot().setStackInSlot(0, realRecipe.assemble(inputSlot));
            }
        }
        return false;
    }

    @Override
    public boolean openContainer(AbstractContainerMenu container, ServerPlayer serverPlayerEntity) {

        if (container instanceof CashConverterMenu converter) {
            PolymorphApi.common().getRecipeData(serverPlayerEntity)
                    .ifPresent(recipeData -> {
                        CraftingContainer inv = converter.getInputSlot();

                        if (inv != null) {
                            container.slotsChanged(inv);
                        }
                    });
            return true;
        }
        return false;
    }
}
