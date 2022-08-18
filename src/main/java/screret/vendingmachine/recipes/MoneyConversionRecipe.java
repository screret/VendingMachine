package screret.vendingmachine.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.init.Registration;

public class MoneyConversionRecipe implements Recipe<CraftingContainer> {

    public static final ResourceLocation TYPE_ID = new ResourceLocation(VendingMachine.MODID, "cash_conversion");
    public static final int MAX_SIZE = 1;

    private final ResourceLocation id;
    final String group;
    final ItemStack result;
    final NBTIngredient ingredient;

    public MoneyConversionRecipe(ResourceLocation id, String group, ItemStack result, NBTIngredient ingredient) {
        this.id = id;
        this.group = group;
        this.result = result;
        this.ingredient = ingredient;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        var stack = container.getItem(0);
        return this.ingredient.test(stack) && stack.getCount() >= ingredient.getItems()[0].getCount();
    }

    public ItemStack getIngredientStack(){
        return this.ingredient.getItems()[0];
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.MONEY_CONVERSION_RECIPE_SERIALIZER.get();
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return Registration.MONEY_CONVERSION_RECIPE_TYPE.get();
    }

    public static class RecipeType implements net.minecraft.world.item.crafting.RecipeType<MoneyConversionRecipe> {
        @Override
        public String toString(){
            return MoneyConversionRecipe.TYPE_ID.toString();
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<MoneyConversionRecipe> {

        @Override
        public MoneyConversionRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NBTIngredient ingredient = (NBTIngredient) Ingredient.fromJson(GsonHelper.getAsJsonObject(json,"ingredient"));

            if (ingredient.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else {
                ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
                return new MoneyConversionRecipe(resourceLocation, group, result, ingredient);
            }
        }

        public MoneyConversionRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            NBTIngredient ingredient = (NBTIngredient) Ingredient.fromNetwork(buf);
            ItemStack itemstack = buf.readItem();
            return new MoneyConversionRecipe(resourceLocation, group, itemstack, ingredient);
        }

        public void toNetwork(FriendlyByteBuf buf, MoneyConversionRecipe recipe) {
            buf.writeUtf(recipe.group);
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
        }
    }
}
