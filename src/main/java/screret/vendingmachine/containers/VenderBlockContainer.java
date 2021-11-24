package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import javax.annotation.Nonnull;

public class VenderBlockContainer extends Container {

    private final IItemHandler playerInventory;
    private final IItemHandler inventory;

    private final VendingMachineTile tile;

    private static final int HOTBAR_SLOT_COUNT = 9;
	private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
	private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
	private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
	private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

	private static final int VANILLA_FIRST_SLOT_INDEX = 0;
	private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
	private static final int TE_INVENTORY_SLOT_COUNT = VendingMachineTile.NUMBER_OF_SLOTS;  // must match TileEntityInventoryBasic.NUMBER_OF_SLOTS

    public static final int TITLE_INVENTORY_YPOS = 20;  // the ContainerScreenBasic needs to know these so it can tell where to draw the Titles
    public static final int PLAYER_INVENTORY_YPOS = 84;
    public static final int PLAYER_INVENTORY_XPOS = 8;

    public static final int INV_SIZE = 6;

    private static final Logger LOGGER = LogManager.getLogger();


    public VenderBlockContainer(int windowID, PlayerInventory playerInventory, IItemHandler inven, VendingMachineTile tileEntity) {
        super(Registration.VENDER_CONT.get(), windowID);
        this.playerInventory = new InvWrapper(playerInventory);
        this.tile = tileEntity;
        this.inventory = inven;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        layoutPlayerInventorySlots(8, 84);
        if (tileEntity != null) {
            final int INPUT_SLOTS_XPOS = 24;
            final int INPUT_SLOTS_YPOS = 16;
            final int BOTTLE_SLOT_XPOS = 59;
            final int BOTTLE_SLOT_YPOS = 34;
            for(int i = 0; i < tileEntity.inputSlot.getSlots(); i++){
                this.addSlot(slotHandler(inven, i, INPUT_SLOTS_XPOS + SLOT_X_SPACING * i, INPUT_SLOTS_YPOS + SLOT_Y_SPACING * i));
            }
            this.addSlot(slotHandler(inven, 36, BOTTLE_SLOT_XPOS, BOTTLE_SLOT_YPOS));

            final int OUTPUT_SLOTS_XPOS = 113;
            final int OUTPUT_SLOTS_YPOS = 34;
            this.addSlot(slotHandler(inven, 4, OUTPUT_SLOTS_XPOS + SLOT_X_SPACING * 0, OUTPUT_SLOTS_YPOS));

            String message = "";
            for(StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            LOGGER.info(message);
        } else {
            throw new IllegalStateException("TileEntity is null");
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            }

            if (stack.isEmpty()) {
                slot.mayPlace(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
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

    public SlotItemHandler slotHandler(IItemHandler handler, int index, int xPosition, int yPosition){
        return new SlotItemHandler(handler, index, xPosition, yPosition){
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (index == 0) {
                    return false;
                } else if (index != 36) {
                    return true;
                } else {
                    return stack.copy().getItem() == Items.GOLD_INGOT;
                }
            }
        };
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return false;
    }
}
