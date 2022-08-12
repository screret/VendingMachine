package screret.vendingmachine.integration.polymorph;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.capability.StackRecipeData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.SortedSet;

public class ConversionStackRecipeData extends StackRecipeData {

    public ConversionStackRecipeData(ItemStack pOwner) {
        super(pOwner);
    }

    @Override
    public Pair<SortedSet<IRecipePair>, ResourceLocation> getPacketData() {
        SortedSet<IRecipePair> recipesList = this.getRecipesList();
        ResourceLocation selected = null;

        if (!recipesList.isEmpty()) {
            selected = this.getSelectedRecipe().map(Recipe::getId)
                    .orElse(recipesList.first().getResourceLocation());
        }
        return new Pair<>(recipesList, selected);
    }
}