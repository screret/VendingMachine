package screret.vendingmachine.mixin.compat;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(value = PolymorphIntegrations.class, remap = false)
public interface AccessorPolymorphIntegrations {

    @Accessor(value = "INTEGRATIONS", remap = false)
    public static Map<String, Supplier<Supplier<AbstractCompatibilityModule>>> getIntegrations() {
        throw new NotImplementedException("AccessorPolymorphIntegrations mixin failed to apply");
    }

    @Accessor(value = "ACTIVE_INTEGRATIONS", remap = false)
    public static Set<AbstractCompatibilityModule> getActiveIntegrations(){
        throw new NotImplementedException("AccessorPolymorphIntegrations mixin failed to apply");
    }

    @Accessor(value = "CONFIG_ACTIVATED", remap = false)
    public static Set<String> getConfigActivated(){
        throw new NotImplementedException("AccessorPolymorphIntegrations mixin failed to apply");
    }
}
