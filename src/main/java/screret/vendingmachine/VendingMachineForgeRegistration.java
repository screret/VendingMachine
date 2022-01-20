package screret.vendingmachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.capabilities.IController;
import screret.vendingmachine.items.ControlCardItem;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VendingMachineForgeRegistration {

    public static ICapabilityProvider CONTROL_CARD_CAP_PROVIDER;

    @SubscribeEvent
    public static void setupCapabilities(@NotNull AttachCapabilitiesEvent<ItemStack> event) {
        if(event.getObject().getItem().asItem() instanceof ControlCardItem){
            Controller backend = new Controller(((ControlCardItem) event.getObject().getItem()).getOwner());
            LazyOptional<IController> optionalStorage = LazyOptional.of(() -> backend);
            Capability<IController> capability = ControlCardCapability.VENDING_CONTROL_CAPABILITY;

            CONTROL_CARD_CAP_PROVIDER = new ICapabilitySerializable<Tag>() {
                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
                    if (cap == capability) {
                        return optionalStorage.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public Tag serializeNBT() {
                    return optionalStorage.resolve().get().writeNBT(capability, backend, null);
                }

                @Override
                public void deserializeNBT(Tag nbt) {
                    optionalStorage.resolve().get().readNBT(capability, backend, null, nbt);
                }
            };

            event.addCapability(new ResourceLocation(VendingMachine.MODID, "control_card_cap"), CONTROL_CARD_CAP_PROVIDER);
            event.addListener(optionalStorage::invalidate);
        }
    }
}
