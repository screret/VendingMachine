package screret.vendingmachine.configs;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class VendingMachineConfig extends ForgeConfigSpec.Builder {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ArrayList<String> itemDefaultPrices = new ArrayList<>();
        public final ForgeConfigSpec.ConfigValue<List<String>> itemPrices;

        public final ForgeConfigSpec.BooleanValue allowPriceEditing;

        public General(ForgeConfigSpec.Builder builder)
        {
            itemDefaultPrices.add("minecraft:dirt 1");

            builder.comment("Item Prices").push("item_prices");
            this.itemPrices = builder.comment("Item prices. Format is \"namespace:item price\"")
                    .worldRestart()
                    .define("item_prices", itemDefaultPrices);
            this.allowPriceEditing = builder.comment("Set to true if players can edit item prices per-machine.")
                    .worldRestart()
                    .define("allow_editing", false);

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

}
