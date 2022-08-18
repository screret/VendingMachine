package screret.vendingmachine.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Util {
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

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

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
                    if (Util.getMoneyValue(stack) == moneyType) {
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

    public static float getTotalOfMoney(ItemStack stack) {
        float totalOfMoney = 0;

        if(stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()){
            IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().get();
            return getTotalOfMoney(handler);
        }


        if (!stack.isEmpty()) {
            if(stack.is(Registration.MONEY.get())) {
                totalOfMoney += stack.getCount() * getMoneyValue(stack);
            }
        }
        return totalOfMoney;
    }
}
