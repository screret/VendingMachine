package screret.vendingmachine.items;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MoneyItem extends Item {

    public static String MONEY_VALUE_TAG = "money_value";

    public static float[] MONEY_VALUES = new float[]{
            1f,
            2f,
            5f,
            10f,
            20f,
            50f,
            100f,
            1000f,
    };

    public MoneyItem(Properties itemProperties) {
        super(itemProperties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return new TranslatableComponent(this.getDescriptionId(stack), MoneyItem.getMoneyValue(stack));
    }

    public static float getMoneyValue(ItemStack stack){
        return stack.getTag().getFloat(MONEY_VALUE_TAG);
    }

    public static void setMoneyValue(ItemStack stack, float value){
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat(MONEY_VALUE_TAG, value);
    }
}
