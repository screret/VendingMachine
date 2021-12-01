package screret.vendingmachine.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderPriceEditorContainer extends Container {
    final VendingMachineTile tile;

    public VenderPriceEditorContainer(int windowID, VendingMachineTile tile) {
        super(Registration.VENDER_CONT_PRICES.get(), windowID);
        this.tile = tile;
    }

    @Override
    public boolean stillValid(PlayerEntity playerEntity) {
        return playerEntity.position().distanceToSqr(this.tile.getBlockPos().getX(), this.tile.getBlockPos().getY(), this.tile.getBlockPos().getZ()) < 8 * 8;
    }
}
