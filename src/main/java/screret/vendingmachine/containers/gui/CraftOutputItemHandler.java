package screret.vendingmachine.containers.gui;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.init.Registration;

public class CraftOutputItemHandler extends SlotItemHandler {
    private final CraftingContainer craftSlots;
    private int removeCount;
    private final Player player;

    public CraftOutputItemHandler(Player player, CraftingContainer craftSlots, IItemHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.player = player;
        this.craftSlots = craftSlots;
    }
    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return hasItem();
    }

    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }

        return super.remove(amount);
    }

    @Override
    public void onSwapCraft(int count) {
        this.removeCount += count;
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int count) {
        this.removeCount += count;
        this.checkTakeAchievements(stack);
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
        int i = newStackIn.getCount() - oldStackIn.getCount();
        if (i > 0) {
            this.onQuickCraft(newStackIn, i);
        }

    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        if (this.removeCount > 0) {
            stack.onCraftedBy(this.player.level, this.player, this.removeCount);
            net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(this.player, stack, this.craftSlots);
        }

        if (this.container instanceof RecipeHolder) {
            ((RecipeHolder)this.container).awardUsedRecipes(this.player);
        }

        this.removeCount = 0;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.checkTakeAchievements(stack);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> stacks = player.level.getRecipeManager().getRemainingItemsFor(Registration.MONEY_CONVERSION_RECIPE_TYPE.get(), this.craftSlots, player.level);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
        for(int i = 0; i < stacks.size(); ++i) {
            ItemStack craftStack = this.craftSlots.getItem(i);
            ItemStack slotStack = stacks.get(i);
            if (!craftStack.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                craftStack = this.craftSlots.getItem(i);
            }

            if (!slotStack.isEmpty()) {
                if (craftStack.isEmpty()) {
                    this.craftSlots.setItem(i, slotStack);
                } else if (ItemStack.isSame(craftStack, slotStack) && ItemStack.tagMatches(craftStack, slotStack)) {
                    slotStack.grow(craftStack.getCount());
                    this.craftSlots.setItem(i, slotStack);
                } else if (!this.player.getInventory().add(slotStack)) {
                    this.player.drop(slotStack, false);
                }
            }
        }

    }
}
