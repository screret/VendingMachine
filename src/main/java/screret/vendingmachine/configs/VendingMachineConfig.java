package screret.vendingmachine.configs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.init.Registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VendingMachineConfig extends ForgeConfigSpec.Builder {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ArrayList<String> itemDefaultPrices = new ArrayList<>();
        public final ForgeConfigSpec.ConfigValue<List<String>> itemPrices;

        public final ForgeConfigSpec.ConfigValue<String> paymentItem;
        public final String defaultPaymentItem = "vendingmachine:money";
        public final ForgeConfigSpec.DoubleValue startMoney;

        public final ForgeConfigSpec.BooleanValue allowPriceEditing;
        public final ForgeConfigSpec.BooleanValue isStackPrices;
        public final ForgeConfigSpec.IntValue maxVenderStack;

        public General(ForgeConfigSpec.Builder builder)
        {
            itemDefaultPrices.add("minecraft:dirt 1");

            builder.comment("Items").push("items");
            this.paymentItem = builder.comment("The default payment item. Format is \"namespace:item\"")
                    .worldRestart()
                    .define("payment_item", defaultPaymentItem);
            this.startMoney = builder.comment("Set to 0 if you don't want to give new players money, else set to the amount of money to give new players.")
                    .worldRestart()
                    .defineInRange("start_money", 1000d, 0d, Double.MAX_VALUE);
            this.itemPrices = builder.comment("Item prices. Format is \"namespace:item price\"")
                    .worldRestart()
                    .define("item_prices", itemDefaultPrices);
            this.allowPriceEditing = builder.comment("Set to true if players can edit item prices per-machine.")
                    .worldRestart()
                    .define("allow_editing", true);
            this.isStackPrices = builder.comment("Set to true if prices are per-stack and not per-item")
                    .worldRestart()
                    .define("is_stack_price", false);
            this.maxVenderStack = builder.comment("Maximum value of a stack inside a Vending Machine")
                    .worldRestart()
                    .defineInRange("max_stack", 1024, 1, 1024);

            builder.pop();
        }
    }

    private static HashMap<Item, Float> DECRYPTED_PRICES;

    public static HashMap<Item, Float> getDecryptedPrices(){
        if(DECRYPTED_PRICES != null)
            return DECRYPTED_PRICES;

        HashMap<Item, Float> map = new HashMap<>();

        for (String string : GENERAL.itemPrices.get()){
            String key = string.split(" ")[0];
            String value = string.split(" ")[1];

            if(ResourceLocation.isValidResourceLocation(key)) {
                map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(key)), Float.valueOf(value));
            }
        }

        DECRYPTED_PRICES = map;
        return DECRYPTED_PRICES;
    }

    private static Item PAYMENT_ITEM;

    public static Item getPaymentItem(){
        if(PAYMENT_ITEM != null)
            return PAYMENT_ITEM;

        if(ResourceLocation.isValidResourceLocation(GENERAL.paymentItem.get())) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GENERAL.paymentItem.get()));
            PAYMENT_ITEM = item;
            return item;
        }
        PAYMENT_ITEM = Registration.MONEY.get();
        return PAYMENT_ITEM;
    }

}
