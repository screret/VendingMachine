package screret.vendingmachine.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.xml.soap.Text;

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
    public ITextComponent getName(ItemStack stack) {
        return new TranslationTextComponent(this.getDescriptionId(stack), MoneyItem.getMoneyValue(stack));
    }

    public static float getMoneyValue(ItemStack stack){
        return stack.getTag().getFloat(MONEY_VALUE_TAG);
    }

    public static void setMoneyValue(ItemStack stack, float value){
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putFloat(MONEY_VALUE_TAG, value);
    }
}
