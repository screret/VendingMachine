package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.init.Registration;

import java.util.UUID;

public class ContainerControlCard extends Container {
    private final UUID ownerUUID;
    private final Controller controller;
    private final PlayerInventory inv;

    public ContainerControlCard(int windowID, PlayerInventory inv, UUID uuid, Controller controller) {
        super(Registration.CONTAINER_CONTROL_CARD.get(), windowID);
        this.inv = inv;
        ownerUUID = uuid;
        this.controller = controller;
    }

    public PlayerEntity getCurrentPlayer(){
        return this.inv.player;
    }

    public Controller getController(){
        return this.controller;
    }

    public boolean hasController(){
        return this.controller != null;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }
}
