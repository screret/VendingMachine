package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.init.Registration;

import java.util.UUID;

public class ContainerControlCard extends AbstractContainerMenu {
    private final UUID ownerUUID;
    private final Controller controller;
    private final PlayerInventory inv;

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
    public boolean stillValid(Player player) {
        return true;
    }
}
