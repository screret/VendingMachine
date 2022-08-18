package screret.vendingmachine.items;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import screret.vendingmachine.util.Util;

public class MoneyItem extends Item {

    public MoneyItem(Properties itemProperties) {
        super(itemProperties);
    }

    private static final Component componentLangBill = new TranslatableComponent("item.vendingmachine.money.bill");
    private static final Component componentLangCoin = new TranslatableComponent("item.vendingmachine.money.coin");

    @Override
    public Component getName(ItemStack stack) {
        return new TranslatableComponent(this.getDescriptionId(stack), Util.DECIMAL_FORMAT.format(Util.getMoneyValue(stack)), Util.getMoneyValue(stack) < 5 ? componentLangCoin : componentLangBill);
    }
}
