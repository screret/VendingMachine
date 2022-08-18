package screret.vendingmachine.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import screret.vendingmachine.capabilities.WalletCapabilityProvider;
import screret.vendingmachine.containers.WalletItemMenu;
import screret.vendingmachine.util.Util;

import java.util.List;

public class WalletItem extends Item {

    public WalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (!level.isClientSide && heldStack.is(this)) {
            NetworkHooks.openGui((ServerPlayer) player, new WalletMenuProvider(heldStack));

            return InteractionResultHolder.success(heldStack);
        }

        return InteractionResultHolder.pass(heldStack);
    }

    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (stack.is(this))
            return new WalletCapabilityProvider();
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipLines, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipLines, flag);
        tooltipLines.add(new TranslatableComponent(this.getDescriptionId() + ".desc", Util.DECIMAL_FORMAT.format(Util.getTotalOfMoney(stack))));

    }

    protected class WalletMenuProvider implements MenuProvider {

        private ItemStack thisStack;

        public WalletMenuProvider(ItemStack stack){
            this.thisStack = stack;
        }

        @Override
            public Component getDisplayName() {
                return thisStack.getHoverName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new WalletItemMenu(containerId, inventory, player.getMainHandItem());
        }
    }
}
