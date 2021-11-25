package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderBlockContainer extends Container {

    private final IItemHandler playerInventory;
    private final PlayerInventory realPlayerInv;
    private final IItemHandler inputInventory;
    private final IItemHandler moneyInventory;
    private final IItemHandler outputInventory;

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

    public static final Logger LOGGER = LogManager.getLogger();


    public VenderBlockContainer(int windowID, PlayerInventory playerInventory, IItemHandler inputInventory, IItemHandler outputInv, IItemHandler moneyInv, VendingMachineTile tileEntity) {
        super(Registration.VENDER_CONT.get(), windowID);
        this.playerInventory = new PlayerInvWrapper(playerInventory);
        this.realPlayerInv = playerInventory;
        this.tile = tileEntity;
        this.inputInventory = inputInventory;
        this.moneyInventory = moneyInv;
        this.outputInventory = outputInv;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        layoutPlayerInventorySlots(8, 140);
        if (tileEntity != null) {
            final int INPUT_SLOTS_XPOS = 8;
            final int INPUT_SLOTS_YPOS = 18;
            final int MONEY_SLOT_XPOS = 134;
            final int MONEY_SLOT_YPOS = 36;
            for(int x = 0; x < 5; x++){
                for(int y = 0; y < 6; y++) {
                    this.addSlot(new SlotItemHandler(this.inputInventory, y + x, INPUT_SLOTS_XPOS + SLOT_X_SPACING * x, INPUT_SLOTS_YPOS + SLOT_Y_SPACING * y));
                }
            }
            this.addSlot(new SlotItemHandler(this.moneyInventory, 42, MONEY_SLOT_XPOS, MONEY_SLOT_YPOS));

            final int OUTPUT_SLOTS_XPOS = 134;
            final int OUTPUT_SLOTS_YPOS = 90;
            this.addSlot(new SlotItemHandler(this.outputInventory, 42, OUTPUT_SLOTS_XPOS, OUTPUT_SLOTS_YPOS));
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

    @Override
    public boolean stillValid(PlayerEntity playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }
}
