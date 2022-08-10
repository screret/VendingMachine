package screret.vendingmachine.items;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    private float getTotalOfMoney(ItemStackHandler stackHandler, float moneyType) {
        float totalOfMoney = 0;

        for(int i = 0; i < stackHandler.getSlots(); ++i){
            ItemStack stack = stackHandler.getStackInSlot(0);
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

    private float getTotalOfMoney(ItemStack stack, float moneyType) {
        float totalOfMoney = 0;

        if (stack != null && !stack.isEmpty()) {
            if(stack.is(Registration.MONEY.get())) {
                if (MoneyItem.getMoneyValue(stack) == moneyType) {
                    totalOfMoney = totalOfMoney + stack.getCount();
                }
            }
        }
        return totalOfMoney;
    }

    private float getAllMoneyValue(ItemStackHandler wallet, float amountRemovable) {
        float amount = amountRemovable;

        float one = getTotalOfMoney(wallet, 1);
        float two = getTotalOfMoney(wallet, 2);
        float five = getTotalOfMoney(wallet, 5);
        float ten = getTotalOfMoney(wallet, 10);
        float twenty = getTotalOfMoney(wallet, 20);
        float fifty = getTotalOfMoney(wallet, 50);
        float hundred = getTotalOfMoney(wallet, 100);
        float thousand = getTotalOfMoney(wallet, 1000);

        float[] out = new float[8];

        out[7] = amount / 1000f;
        while (out[7] > thousand) out[7]--;
        amount = amount - (out[7] * 1000);

        out[6] = amount / 100f;
        while (out[6] > hundred) out[6]--;
        amount = amount - (out[6] * 100);

        out[5] = amount / 50f;
        while (out[5] > fifty) out[5]--;
        amount = amount - (out[5] * 50);

        out[4] = amount / 20f;
        while (out[4] > twenty) out[4]--;
        amount = amount - (out[4] * 20);

        out[3] = amount / 10f;
        while (out[3] > ten) out[3]--;
        amount = amount - (out[3] * 10);

        out[2] = amount / 5f;
        while (out[2] > five) out[2]--;
        amount = amount - (out[2] * 5);

        out[1] = amount / 2f;
        while (out[1] > two) out[1]--;
        amount = amount - (out[1] * 2);

        out[0] = amount / 1f;
        while (out[0] > one) out[0]--;
        amount = amount - (out[0] * 1);

        return amount;


    }
}
