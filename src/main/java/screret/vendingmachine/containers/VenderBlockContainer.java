package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.events.packets.PacketAllowItemTake;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.UUID;

public class VenderBlockContainer extends Container {

    private final IItemHandler playerInventory;
    public final OwnedStackHandler inputInventory;
    private final IItemHandler moneyInventory;
    private final IItemHandler outputInventory;

    public boolean isAllowedToTakeItems = false;
    public static boolean buyTestMode_REMOVE_LATER = false;

    public SlotItemHandler selectedSlot;

    public UUID currentPlayer;

    private final VendingMachineTile tile;

    public static final int INPUT_SLOTS_X_AMOUNT = 4;
    public static final int INPUT_SLOTS_X_AMOUNT_PLUS_1 = INPUT_SLOTS_X_AMOUNT + 1;
    public static final int INPUT_SLOTS_Y_AMOUNT = 5;
    public static final int INPUT_SLOTS_Y_AMOUNT_PLUS_1 = INPUT_SLOTS_Y_AMOUNT + 1;

    public static final Logger LOGGER = LogManager.getLogger();


    public VenderBlockContainer(int windowID, PlayerInventory playerInventory, OwnedStackHandler inputInv, IItemHandler outputInv, IItemHandler moneyInv, VendingMachineTile tileEntity) {
        super(Registration.VENDER_CONT.get(), windowID);
        this.playerInventory = new PlayerInvWrapper(playerInventory);
        this.tile = tileEntity;
        this.inputInventory = inputInv;
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
            for(int x = 0; x < INPUT_SLOTS_X_AMOUNT_PLUS_1; x++){
                for(int y = 0; y < INPUT_SLOTS_Y_AMOUNT_PLUS_1; y++) {
                    int slotNumber = y * INPUT_SLOTS_Y_AMOUNT + x;
                    this.addSlot(new SlotItemHandler(this.inputInventory, slotNumber, INPUT_SLOTS_XPOS + SLOT_X_SPACING * x, INPUT_SLOTS_YPOS + SLOT_Y_SPACING * y));
                }
            }
            currentPlayer = playerInventory.player.getUUID();
            checkPlayerAllowedToChangeInv(playerInventory.player.getUUID());

            this.addSlot(new SlotItemHandler(this.moneyInventory, 0, MONEY_SLOT_XPOS, MONEY_SLOT_YPOS));

            final int OUTPUT_SLOTS_XPOS = 134;
            final int OUTPUT_SLOTS_YPOS = 90;
            this.addSlot(new SlotItemHandler(this.outputInventory, 0, OUTPUT_SLOTS_XPOS, OUTPUT_SLOTS_YPOS));
        } else {
            throw new IllegalStateException("TileEntity is null");
        }
    }

    public ItemStack quickMoveStack(PlayerEntity playerEntity, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
        if(!playerEntity.getUUID().equals(tile.owner) && !slot.getItemHandler().isItemValid(0, new ItemStack(Items.GOLD_INGOT))) { return ItemStack.EMPTY; }
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = slot.getItem();
            if (slotId < INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1) {
                if (!this.moveItemStackTo(itemstack1, INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
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
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId > INPUT_SLOTS_X_AMOUNT_PLUS_1 * INPUT_SLOTS_Y_AMOUNT_PLUS_1) {
            SlotItemHandler slot = (SlotItemHandler) this.slots.get(slotId);
            if (slot.getItemHandler() == inputInventory) {
                if(!isAllowedToTakeItems && clickTypeIn == ClickType.PICKUP) {
                    //TODO: create buy system
                    selectedSlot = slot;
                    player.inventory.setCarried(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
            }
        }
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public boolean checkPlayerAllowedToChangeInv(UUID currentPlayer){
        isAllowedToTakeItems = currentPlayer.equals(tile.owner) && !buyTestMode_REMOVE_LATER;
        if(tile.getLevel().getPlayerByUUID(tile.owner) != null){
            String _ownerName = ITextComponent.Serializer.fromJsonLenient(tile.getLevel().getPlayerByUUID(tile.owner).getDisplayName().getString()).getString();
            String _openerName = ITextComponent.Serializer.fromJsonLenient(tile.getLevel().getPlayerByUUID(currentPlayer).getDisplayName().getString()).getString();
            LOGGER.info("Current player who has the GUI open is the owner: " + isAllowedToTakeItems + "\n owner: " + _ownerName + "(" + tile.owner + ")" + "\n opener: " + _openerName + "(" + currentPlayer + ")");
        }
        return isAllowedToTakeItems;
    }

    public void buy(SlotItemHandler slot){
        if(slot != null && slot.getItem().getCount() > 0) {
            outputInventory.insertItem(0, slot.getItem().copy(), false);
            slot.remove(outputInventory.getSlotLimit(0));
            moneyInventory.extractItem(0, 1, false);
        }
    }

    @Override
    public boolean stillValid(PlayerEntity playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }

    public VendingMachineTile getTile(){
        return tile;
    }
}
