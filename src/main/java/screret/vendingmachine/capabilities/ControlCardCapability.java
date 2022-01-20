package screret.vendingmachine.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ControlCardCapability {

    public static Capability<IController> VENDING_CONTROL_CAPABILITY = CapabilityManager.get(new CapabilityToken<IController>(){});
}
