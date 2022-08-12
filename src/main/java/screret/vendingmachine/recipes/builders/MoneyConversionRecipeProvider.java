package screret.vendingmachine.recipes.builders;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;

import java.util.function.Consumer;

public class MoneyConversionRecipeProvider extends RecipeProvider {

    public MoneyConversionRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        conversion(consumer, 1, 2,2, 1, "2s_to_1s");
        conversion(consumer, 1, 5,5, 1, "5s_to_1s");

        conversion(consumer, 5, 2,10, 1, "10s_to_5s");

        conversion(consumer, 10, 2,20, 1, "20s_to_10s");
        conversion(consumer, 10, 5,50, 1, "50s_to_10s");

        conversion(consumer, 50, 2,100, 1, "100s_to_50s");

        conversion(consumer, 100, 10,1000, 1, "1000s_to_100s");


        conversion(consumer, 2, 1,1, 2, "1s_to_2s");
        conversion(consumer, 5, 1,1, 5, "1s_to_5s");

        conversion(consumer, 10, 1,1, 10, "1s_to_10s");
        conversion(consumer, 10, 1,5, 2, "5s_to_10s");
        conversion(consumer, 10, 1,2, 5, "2s_to_10s");

        conversion(consumer, 20, 1,1, 20, "1s_to_20s");
        conversion(consumer, 20, 1,2, 10, "2s_to_20s");
        conversion(consumer, 20, 1,5, 4, "5s_to_20s");
        conversion(consumer, 20, 1,10, 2, "10s_to_20s");

        conversion(consumer, 50, 1,1, 50, "1s_to_50s");
        conversion(consumer, 50, 1,2, 25, "2s_to_50s");
        conversion(consumer, 50, 1,5, 10, "5s_to_50s");
        conversion(consumer, 50, 1,10, 5, "10s_to_50s");

        conversion(consumer, 100, 1,1, 100, "1s_to_100s");
        conversion(consumer, 100, 1,2, 50, "2s_to_100s");
        conversion(consumer, 100, 1,5, 20, "5s_to_100s");
        conversion(consumer, 100, 1,10, 10, "10s_to_100s");
        conversion(consumer, 100, 1,20, 5, "20s_to_100s");
        conversion(consumer, 100, 1,50, 2, "50s_to_100s");

        conversion(consumer, 1000, 1,1, 1000, "1s_to_1000s");
        conversion(consumer, 1000, 1,2, 500, "2s_to_1000s");
        conversion(consumer, 1000, 1,5, 250, "5s_to_1000s");
        conversion(consumer, 1000, 1,10, 100, "10s_to_1000s");
        conversion(consumer, 1000, 1,20, 50, "20s_to_1000s");
        conversion(consumer, 1000, 1,50, 20, "50s_to_1000s");
        conversion(consumer, 1000, 1,100, 10, "100s_to_1000s");
    }

    protected static void conversion(Consumer<FinishedRecipe> recipe, float resultMoneyValue, int resultCount, float ingredientMoneyValue, int ingredientCount, String id) {
        MoneyConversionRecipeBuilder.conversion(MoneyItem.setMoneyValue(new ItemStack(Registration.MONEY.get(), resultCount), resultMoneyValue)).requires(MoneyItem.setMoneyValue(new ItemStack(Registration.MONEY.get(), ingredientCount), ingredientMoneyValue)).group("money_conversion").unlockedBy("has_money", has(Registration.MONEY.get())).save(recipe, new ResourceLocation(VendingMachine.MODID, id));
    }
}
