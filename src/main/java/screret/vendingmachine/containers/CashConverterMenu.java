package screret.vendingmachine.containers;

import screret.vendingmachine.integration.polymorph.VendingMachinesCompatibilityLoader;
import top.theillusivec4.polymorph.common.crafting.RecipeSelection;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.containers.stackhandlers.CraftOutputItemHandler;
import screret.vendingmachine.containers.stackhandlers.ConversionResultStackHandler;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.recipes.MoneyConversionRecipe;

import java.util.Optional;

public class CashConverterMenu extends AbstractContainerMenu {

    private final CraftingContainer inputSlot = new CraftingContainer(this, 1,1){
        @Override
        public void setChanged(){
            super.setChanged();
            CashConverterMenu.this.slotsChanged(this);
        }
    };
    private final ConversionResultStackHandler outputSlot = new ConversionResultStackHandler(1);

    public final int slotInputIndex, slotOutputIndex;

    private static final int SLOT_INPUT_X = 44, SLOT_INPUT_Y = 36, SLOT_OUTPUT_X = 116, SLOT_OUTPUT_Y = 36, SLOT_INV_START_X = 8, SLOT_INV_START_Y = 86;
    private static final int LAST_MENU_SLOT = 1, INV_SLOTS_AMOUNT = 36;

    private final ContainerLevelAccess access;
    private final Player player;
    private final InvWrapper playerInventory;

    public CashConverterMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(Registration.CASH_CONVERTER_MENU.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        this.playerInventory = new InvWrapper(inventory);

        slotInputIndex = this.addSlot(new Slot(inputSlot, 0, SLOT_INPUT_X, SLOT_INPUT_Y)).index;
        slotOutputIndex = this.addSlot(new CraftOutputItemHandler(this.player, this.inputSlot, this.outputSlot, 0, SLOT_OUTPUT_X, SLOT_OUTPUT_Y)).index;

        layoutPlayerInventorySlots(SLOT_INV_START_X, SLOT_INV_START_Y);
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level level, Player player, CraftingContainer inputSlot, ConversionResultStackHandler outputSlot, int slotOutputIndex) {
        if (!level.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            ItemStack output = ItemStack.EMPTY;

            if (outputSlot.getRecipeUsed() instanceof MoneyConversionRecipe recipe && recipe.matches(inputSlot, level)) {
                output = recipe.assemble(inputSlot);
            } else {
                Optional<MoneyConversionRecipe> maybeRecipe = ModList.get().isLoaded(VendingMachinesCompatibilityLoader.POLYMORPH_MODID) ? RecipeSelection.getPlayerRecipe(Registration.MONEY_CONVERSION_RECIPE_TYPE.get(), inputSlot, level, player) :  level.getRecipeManager().getRecipeFor(Registration.MONEY_CONVERSION_RECIPE_TYPE.get(), inputSlot, level);
                if (maybeRecipe.isPresent()) {
                    MoneyConversionRecipe recipe = maybeRecipe.get();
                    if (outputSlot.setRecipeUsed(level, serverplayer, recipe)) {
                        output = recipe.assemble(inputSlot);
                    }
                }
            }

            outputSlot.setStackInSlot(0, output);
            menu.setRemoteSlot(slotOutputIndex, output);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), slotOutputIndex, output));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        this.access.execute((level, pos) -> {
            slotChangedCraftingGrid(this, level, this.player, this.inputSlot, this.outputSlot, this.slotOutputIndex);
        });
        //this.broadcastChanges();
    }

    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.inputSlot);
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            final int lastSlot = LAST_MENU_SLOT + INV_SLOTS_AMOUNT;
            final int firstInvSlot = slotOutputIndex + 1;

            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index == slotOutputIndex) {
                this.access.execute((level, pos) -> slotStack.getItem().onCraftedBy(slotStack, level, player));
                if (!this.moveItemStackTo(slotStack, firstInvSlot, lastSlot, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotStack, stack);
            } else if (index != slotInputIndex) {
                if (index < INV_SLOTS_AMOUNT + 2 && !moveItemStackTo(slotStack, slotInputIndex, slotOutputIndex, false))
                    return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(slotStack, firstInvSlot, lastSlot, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
            //this.broadcastChanges();
        }

        return stack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.index != this.slotOutputIndex && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.CASH_CONVERTER.get());
    }

    public CraftingContainer getInputSlot(){
        return inputSlot;
    }

    public ConversionResultStackHandler getOutputSlot(){
        return outputSlot;
    }

    public Player getPlayer(){
        return player;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }
}
