package screret.vendingmachine.items;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import screret.vendingmachine.init.Registration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class MoneyItem extends Item {

    public static String MONEY_VALUE_TAG = "money_value";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

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

    private static final Component componentLangBill = Component.translatable("item.vendingmachine.money.bill");
    private static final Component componentLangCoin = Component.translatable("item.vendingmachine.money.coin");

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack), DECIMAL_FORMAT.format(MoneyItem.getMoneyValue(stack)), getMoneyValue(stack) < 5 ? componentLangCoin : componentLangBill);
    }

    public static float getMoneyValue(ItemStack stack){
        return stack.is(Registration.MONEY.get()) && stack.hasTag() ? stack.getTag().getFloat(MONEY_VALUE_TAG) : -1;
    }

    public static ItemStack setMoneyValue(ItemStack stack, float value){
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat(MONEY_VALUE_TAG, value);
        return stack;
    }

    public static float getTotalOfMoney(IItemHandler itemHandler, float moneyType) {
        float totalOfMoney = 0;

        for(int index = 0; index < itemHandler.getSlots(); ++index){
            ItemStack stack = itemHandler.getStackInSlot(index);
            if (!stack.isEmpty()) {
                if(stack.is(Registration.MONEY.get())) {
                    if (MoneyItem.getMoneyValue(stack) == moneyType) {
                        totalOfMoney = totalOfMoney + stack.getCount();
                    }
                }
            }
        }

        return totalOfMoney;
    }

    public static float getTotalOfMoney(IItemHandler itemHandler) {
        float totalOfMoney = 0;

        for(int index = 0; index < itemHandler.getSlots(); ++index){
            ItemStack stack = itemHandler.getStackInSlot(index);
            if (!stack.isEmpty()) {
                if(stack.is(Registration.MONEY.get())) {
                    totalOfMoney += stack.getCount() * getMoneyValue(stack);
                }
            }
        }

        return totalOfMoney;
    }

    private float getTotalOfMoney(ItemStack stack) {
        float totalOfMoney = 0;

        if (stack != null && !stack.isEmpty()) {
            if(stack.is(Registration.MONEY.get())) {
                totalOfMoney += stack.getCount() * getMoneyValue(stack);
            }
        }
        return totalOfMoney;
    }
}
