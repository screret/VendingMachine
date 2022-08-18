package screret.vendingmachine.integration.polymorph;

import net.minecraftforge.fml.loading.FMLLoader;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.mixin.compat.AccessorPolymorphIntegrations;
import top.theillusivec4.polymorph.common.integration.AbstractCompatibilityModule;

import java.util.function.Supplier;

public class VendingMachinesCompatibilityLoader {

    public static final String POLYMORPH_MODID = "polymorph";

    public static void init(){
        final Supplier<AbstractCompatibilityModule> sup = VendingMachinesCompatibilityModule::new;
        AccessorPolymorphIntegrations.getConfigActivated().add(VendingMachine.MODID);
        AccessorPolymorphIntegrations.getIntegrations().put(VendingMachine.MODID, () -> sup);
        AccessorPolymorphIntegrations.getActiveIntegrations().add(sup.get());
    }
}
