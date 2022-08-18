package screret.vendingmachine.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.VendingMachineForgeRegistration;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.capabilities.IController;
import screret.vendingmachine.containers.ControlCardMenu;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ControlCardItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private UUID owner;

    public ControlCardItem(Properties properties) {
        super(properties);
        this.owner = owner;
    }

    public void setOwner(UUID uuid){
        owner = uuid;
    }

    public UUID getOwner(){
        return owner;
    }

    @Nonnull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        if(!ctx.getPlayer().isCrouching()){
            return InteractionResult.PASS;
        }

        Level level = ctx.getLevel();

        if(owner == null){
            owner = ctx.getPlayer().getUUID();
        }

        BlockPos pos = ctx.getClickedPos();
        ItemStack itemStack = ctx.getItemInHand();
        if (!(itemStack.getItem() instanceof ControlCardItem)) throw new AssertionError("Unexpected ControlCardItem type");
        ControlCardItem itemControlCard = (ControlCardItem)itemStack.getItem();

        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity == null){
            tileEntity = level.getBlockEntity(pos.below());
            pos = pos.below();
        }
        if (tileEntity == null) return InteractionResult.PASS;

        if(level.isClientSide()){
            return InteractionResult.PASS;
        }

        if(tileEntity instanceof VendingMachineBlockEntity && ((VendingMachineBlockEntity)tileEntity).owner.equals(this.owner) && !ControlCardItem.getController(itemControlCard, itemStack).hasMachine(pos)) {
            sendTile((VendingMachineBlockEntity) tileEntity, null);
            ctx.getPlayer().displayClientMessage(new TranslatableComponent("msg.vendingmachine.added_control"), true);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);
        if(owner == null){
            owner = player.getUUID();
        }
        NetworkHooks.openGui((ServerPlayer) player, new ContainerProviderControlCard(this, stack));
        return InteractionResultHolder.success(stack);
    }

    private final Map<Direction, LazyOptional<IController>> cache = new HashMap<>();

    private void sendTile(VendingMachineBlockEntity tile, Direction direction) {
        LazyOptional<IController> targetCapability = cache.get(direction);

        if (targetCapability == null) {
            ICapabilityProvider provider = VendingMachineForgeRegistration.CONTROL_CARD_CAP_PROVIDER;
            if(provider != null){
                targetCapability = provider.getCapability(ControlCardCapability.VENDING_CONTROL_CAPABILITY, direction);
                cache.put(direction, targetCapability);
                targetCapability.addListener(self -> cache.put(direction, null));
            }
        }

        targetCapability.ifPresent(storage -> storage.addMachine(tile.getBlockPos()));
    }

    public static Controller getController(ControlCardItem item, ItemStack itemStack) {
        IController controller = itemStack.getCapability(ControlCardCapability.VENDING_CONTROL_CAPABILITY).orElse(null);
        if (controller == null || !(controller instanceof Controller)) {
            LOGGER.error("ControlCardItem did not have the expected VENDING_CONTROL_CAPABILITY");
            return new Controller(item.owner);
        }
        return (Controller)controller;
    }

    private static class ContainerProviderControlCard implements MenuProvider {
        public ContainerProviderControlCard(ControlCardItem controlCardItem, ItemStack itemStack) {
            this.thisStack = itemStack;
            this.controlCardItem = controlCardItem;
        }

        @Override
        public Component getDisplayName() {
            return thisStack.getDisplayName();
        }

        @Override
        public ControlCardMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
            return new ControlCardMenu(windowID, playerInventory, controlCardItem.getOwner(), ControlCardItem.getController(controlCardItem, thisStack));
        }

        private ControlCardItem controlCardItem;
        private ItemStack thisStack;
    }
}
