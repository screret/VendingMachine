package screret.vendingmachine.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.checkerframework.checker.units.qual.C;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;

public class MoneyConversionRecipe implements Recipe<CraftingContainer> {

    public static final ResourceLocation TYPE_ID = new ResourceLocation(VendingMachine.MODID, "cash_conversion");
    public static final int MAX_SIZE = 1;

    private final ResourceLocation id;
    final String group;
    final ItemStack result;
    final Ingredient ingredient;
    final CompoundTag ingredientTag;
    final int ingredientCount;


    public MoneyConversionRecipe(ResourceLocation id, String group, ItemStack result, Ingredient ingredient, CompoundTag ingredientTag, int ingredientCount) {
        this.id = id;
        this.group = group;
        this.result = result;
        this.ingredient = ingredient;
        this.ingredientTag = ingredientTag;
        this.ingredientCount = ingredientCount;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean flag = true;
        for(var item : ingredient.getItems()){
            ItemStack stack = new ItemStack(item.getItem(), this.ingredientCount);
            stack.setTag(ingredientTag);
            if(!ItemStack.isSameItemSameTags(container.getItem(0), stack))
                flag = false;
        }

        return flag;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y <= MAX_SIZE;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return TYPE_ID;
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

    public static class Serializer implements RecipeSerializer<MoneyConversionRecipe> {

        @Override
        public MoneyConversionRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            JsonObject ingredientElement = json.getAsJsonObject("ingredient");
            Ingredient ingredient = Ingredient.fromJson(ingredientElement);
            int count = ingredientElement.get("count").getAsInt();
            CompoundTag tag;
            try {
                tag = TagParser.parseTag(ingredientElement.get("nbtTag").getAsString());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }

            if (ingredient.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else {
                ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
                return new MoneyConversionRecipe(resourceLocation, group, result, ingredient, tag, count);
            }
        }

        public MoneyConversionRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            CompoundTag tag = buf.readAnySizeNbt();
            int count = buf.readInt();
            ItemStack itemstack = buf.readItem();
            return new MoneyConversionRecipe(resourceLocation, group, itemstack, ingredient, tag, count);
        }

        public void toNetwork(FriendlyByteBuf buf, MoneyConversionRecipe recipe) {
            buf.writeUtf(recipe.group);
            recipe.ingredient.toNetwork(buf);
            buf.writeNbt(recipe.ingredientTag);
            buf.writeInt(recipe.ingredientCount);
            buf.writeItem(recipe.result);
        }
    }
}
