package screret.vendingmachine.containers;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.init.Registration;

import java.util.UUID;

public class ContainerControlCard extends AbstractContainerMenu {
    private final UUID ownerUUID;
    private final Controller controller;
    private final Inventory inv;

    public ContainerControlCard(int windowID, Inventory inv, UUID uuid, Controller controller) {
        super(Registration.CONTAINER_CONTROL_CARD.get(), windowID);
        this.inv = inv;
        ownerUUID = uuid;
        this.controller = controller;
    }

    public Player getCurrentPlayer(){
        return this.inv.player;
    }

    public Controller getController(){
        return this.controller;
    }

    public boolean hasController(){
        return this.controller != null;
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
