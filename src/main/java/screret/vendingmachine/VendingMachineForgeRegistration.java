package screret.vendingmachine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.capabilities.IController;
import screret.vendingmachine.items.ControlCardItem;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VendingMachineForgeRegistration {

    public static ICapabilityProvider CONTROL_CARD_CAP_PROVIDER;

    @SubscribeEvent
    public void setupCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof PlayerEntity){
            Controller backend = new Controller(/*((ControlCardItem) event.getObject().getItem()).getOwner()*/);
            LazyOptional<IController> optionalStorage = LazyOptional.of(() -> backend);
            Capability<IController> capability = ControlCardCapability.VENDING_CONTROL_CAPABILITY;

            CONTROL_CARD_CAP_PROVIDER = new ICapabilitySerializable<INBT>() {
                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
                    if (cap == capability) {
                        return optionalStorage.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public INBT serializeNBT() {
                    return capability.getStorage().writeNBT(capability, backend, null);
                }

                @Override
                public void deserializeNBT(INBT nbt) {
                    capability.getStorage().readNBT(capability, backend, null, nbt);
                }
            };

            event.addCapability(new ResourceLocation(VendingMachine.MODID, "control_card_cap"), CONTROL_CARD_CAP_PROVIDER);
            event.addListener(optionalStorage::invalidate);
        }
    }
}
