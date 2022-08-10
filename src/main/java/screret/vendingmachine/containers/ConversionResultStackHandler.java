package screret.vendingmachine.containers;

import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ConversionResultStackHandler extends ItemStackHandler implements RecipeHolder {

    @javax.annotation.Nullable
    private Recipe<?> recipeUsed;

    public ConversionResultStackHandler(int size) {
        super(size);
    }

    @Override
    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        this.recipeUsed = recipe;
    }

    @Nullable
    @Override
    public Recipe<?> getRecipeUsed() {
        return recipeUsed;
    }
}
