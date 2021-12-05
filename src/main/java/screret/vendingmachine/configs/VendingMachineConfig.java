package screret.vendingmachine.configs;

import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.item.Item;
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
        public final String defaultPaymentItem = "minecraft:emerald";

        public final ForgeConfigSpec.BooleanValue allowPriceEditing;
        public final ForgeConfigSpec.BooleanValue isStackPrices;

        public General(ForgeConfigSpec.Builder builder)
        {
            itemDefaultPrices.add("minecraft:dirt 1");

            builder.comment("Items").push("items");
            this.paymentItem = builder.comment("The default payment item. Format is \"namespace:item\"")
                    .worldRestart()
                    .define("payment_item", defaultPaymentItem);
            this.itemPrices = builder.comment("Item prices. Format is \"namespace:item price\"")
                    .worldRestart()
                    .define("item_prices", itemDefaultPrices);
            this.allowPriceEditing = builder.comment("Set to true if players can edit item prices per-machine.")
                    .worldRestart()
                    .define("allow_editing", false);
            this.isStackPrices = builder.comment("Set to true if prices are per-stack and not per-item")
                    .worldRestart()
                    .define("is_stack_price", false);

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
            map.put(Registry.ITEM.get(new ResourceLocation(keyParts[0], keyParts[1])), Integer.decode(value));
        }

        DECRYPTED_PRICES = map;
        return map;
    }

    public static Item PAYMENT_ITEM = getPaymentItem();

    public static Item getPaymentItem(){
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(GENERAL.paymentItem.get()));
    }

}
