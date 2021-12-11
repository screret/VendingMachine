package screret.vendingmachine.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.VendingMachineForgeRegistration;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.capabilities.IController;
import screret.vendingmachine.containers.ContainerControlCard;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ControlCardItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private UUID owner;

    public ControlCardItem(Properties properties, UUID owner) {
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
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        World level = ctx.getLevel();

        if(owner == null){
            owner = ctx.getPlayer().getUUID();
        }

        BlockPos pos = ctx.getClickedPos();
        ItemStack itemStack = ctx.getItemInHand();
        if (!(itemStack.getItem() instanceof ControlCardItem)) throw new AssertionError("Unexpected ControlCardItem type");
        ControlCardItem itemControlCard = (ControlCardItem)itemStack.getItem();

        TileEntity tileEntity = level.getBlockEntity(pos);
        if(tileEntity == null){
            tileEntity = level.getBlockEntity(pos.below());
            pos = pos.below();
        }
        if (tileEntity == null) return ActionResultType.PASS;

        if(tileEntity instanceof VendingMachineTile && ((VendingMachineTile)tileEntity).owner.equals(this.owner)){
            sendTile((VendingMachineTile)tileEntity, null);
        }

        if(level.isClientSide()){
            ctx.getPlayer().sendMessage(new TranslationTextComponent("msg.vendingmachine.addedcontrol"), this.owner);
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return ActionResult.pass(stack);
        if(owner == null){
            owner = player.getUUID();
        }
        NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProviderControlCard(this, stack));
        return ActionResult.success(stack);
    }

    private final Map<Direction, LazyOptional<IController>> cache = new HashMap<>();

    private void sendTile(VendingMachineTile tile, Direction direction) {
        LazyOptional<IController> targetCapability = cache.get(direction);

        if (targetCapability == null) {
            ICapabilityProvider provider = VendingMachineForgeRegistration.CONTROL_CARD_CAP_PROVIDER;
            if(provider != null){
                targetCapability = provider.getCapability(ControlCardCapability.VENDING_CONTROL_CAPABILITY, direction.getOpposite());
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

    private static class ContainerProviderControlCard implements INamedContainerProvider {
        public ContainerProviderControlCard(ControlCardItem controlCardItem, ItemStack itemStack) {
            this.thisStack = itemStack;
            this.controlCardItem = controlCardItem;
        }

        @Override
        public ITextComponent getDisplayName() {
            return thisStack.getDisplayName();
        }

        @Override
        public ContainerControlCard createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerControlCard(windowID, playerInventory, controlCardItem.getOwner(), ControlCardItem.getController(controlCardItem, thisStack));
        }

        private ControlCardItem controlCardItem;
        private ItemStack thisStack;
    }
}
