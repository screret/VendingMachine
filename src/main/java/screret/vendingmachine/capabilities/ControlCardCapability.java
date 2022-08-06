package screret.vendingmachine.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ControlCardCapability {
    public static Capability<IController> VENDING_CONTROL_CAPABILITY = CapabilityManager.get(new CapabilityToken<IController>(){});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IController.class);
    }
}
