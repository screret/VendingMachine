package screret.vendingmachine.capabilities.configs;

import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class VendingMachineConfig extends ForgeConfigSpec.Builder {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ArrayList<String> itemDefaultPrices = new ArrayList<>();
        public final ForgeConfigSpec.ConfigValue<List<String>> itemPrices;

        public final ForgeConfigSpec.ConfigValue<String> paymentItem;
        public final String defaultPaymentItem = "vendingmachine:money";

        public final ForgeConfigSpec.BooleanValue allowPriceEditing;
        public final ForgeConfigSpec.BooleanValue isStackPrices;
        //public final ForgeConfigSpec.BooleanValue allowControlCard;

        public final ForgeConfigSpec.ConfigValue<Integer> moneyAmount;

        public General(ForgeConfigSpec.Builder builder)
        {
            itemDefaultPrices.add("minecraft:dirt 1");

            builder.comment("Items").push("items");
            this.paymentItem = builder.comment("The default payment item. Format is \"namespace:item\"")
                    .worldRestart()
                    .define("payment_item", defaultPaymentItem);
            this.moneyAmount = builder.comment("Set to 0 if you don't want to give new players money, else set to the amount of money to give new players.", "only works in counts of 100.")
                    .worldRestart()
                    .define("start_money", 1000);
            this.itemPrices = builder.comment("Item default prices. Format is \"namespace:item price\"")
                    .worldRestart()
                    .define("item_prices", itemDefaultPrices);
            this.allowPriceEditing = builder.comment("Set to true if players can edit item prices per-machine.")
                    .worldRestart()
                    .define("allow_editing", false);
            this.isStackPrices = builder.comment("Set to true if prices are per-stack and not per-item")
                    .worldRestart()
                    .define("is_stack_price", false);
            //this.allowControlCard = builder.comment("Set to false if you don't want players to be able to remote control their machines.")
            //        .worldRestart()
            //        .define("allow_rc", true);


            builder.pop();
        }
    }

    public static HashMap<Item, Integer> DECRYPTED_PRICES = decryptPrices();

    public static HashMap<Item, Integer> decryptPrices(){
        HashMap<Item, Integer> map = new HashMap<>();

        for (String string : GENERAL.itemPrices.get()){
            String key = string.split(" ")[0];
            String value = string.split(" ")[1];

            String[] keyParts = key.split(":");

            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(keyParts[0], keyParts[1])), Integer.decode(value));
        }

        return map;
    }

    public static Item PAYMENT_ITEM = getPaymentItem();

    public static Item getPaymentItem(){
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(GENERAL.paymentItem.get()));
    }

}
