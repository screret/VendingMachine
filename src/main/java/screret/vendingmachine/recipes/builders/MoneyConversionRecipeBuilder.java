package screret.vendingmachine.recipes.builders;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import screret.vendingmachine.init.Registration;

import java.util.function.Consumer;

public class MoneyConversionRecipeBuilder implements RecipeBuilder {

    private final ItemStack result;
    private Ingredient ingredient;
    private CompoundTag ingredientTag;
    private int ingredientCount;
    @Nullable
    private String group;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    public MoneyConversionRecipeBuilder(ItemStack result) {
        this.result = result;
    }

    public static MoneyConversionRecipeBuilder conversion(ItemStack stack) {
        return new MoneyConversionRecipeBuilder(stack);
    }

    @Override
    public MoneyConversionRecipeBuilder unlockedBy(String key, CriterionTriggerInstance criterion) {
        this.advancement.addCriterion(key, criterion);
        return this;
    }

    public MoneyConversionRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public MoneyConversionRecipeBuilder requires(Item item) {
        return this.requires(new ItemStack(item));
    }

    public MoneyConversionRecipeBuilder requires(ItemStack stack) {
        this.requires(Ingredient.of(stack));
        this.ingredientCount = stack.getCount();
        this.ingredientTag = stack.getTag();

        return this;
    }

    public MoneyConversionRecipeBuilder requires(Ingredient ingredient) {
        this.requires(ingredient, 1);
        return this;
    }

    public MoneyConversionRecipeBuilder requires(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.ingredientCount = count;
        return this;
    }

    @Override
    public MoneyConversionRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.getItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> recipe, ResourceLocation recipeId) {
        this.ensureValid(recipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        recipe.accept(new MoneyConversionRecipeBuilder.Result(recipeId, this.group == null ? "" : this.group, this.ingredient, this.ingredientTag, this.ingredientCount, this.result, this.advancement, new ResourceLocation(recipeId.getNamespace(), "recipes/" + this.result.getItem().getItemCategory().getRecipeFolderName() + "/" + recipeId.getPath())));
    }

    private void ensureValid(ResourceLocation p_126330_) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126330_);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final Ingredient ingredient;
        private final CompoundTag ingredientTag;
        private final int ingredientCount;
        private final ItemStack result;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, String group, Ingredient ingredient, CompoundTag ingredientTag, int ingredientCount, ItemStack result, Advancement.Builder advancement, ResourceLocation advancementId) {
            this.id = id;
            this.group = group;
            this.ingredient = ingredient;
            this.ingredientTag = ingredientTag;
            this.ingredientCount = ingredientCount;
            this.result = result;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            var ingredient = (JsonObject)this.ingredient.toJson();
            ingredient.addProperty("count", this.ingredientCount);
            ingredient.addProperty("nbtTag", this.ingredientTag.toString());
            json.add("ingredient", ingredient);

            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result.getItem()).toString());
            if(this.result.getCount() > 1){
                result.addProperty("count", this.result.getCount());
            }
            if(this.result.hasTag()){
                result.addProperty("tag", this.result.getTag().toString());
            }

            json.add("result", result);
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public RecipeSerializer<?> getType() {
            return Registration.MONEY_CONVERSION_RECIPE_SERIALIZER.get();
        }

        @javax.annotation.Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @javax.annotation.Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
