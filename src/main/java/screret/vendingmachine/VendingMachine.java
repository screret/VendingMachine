package screret.vendingmachine;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.NonNullList;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.gui.CashConverterScreen;
import screret.vendingmachine.containers.gui.ControlCardScreen;
import screret.vendingmachine.containers.gui.VenderBlockPriceScreen;
import screret.vendingmachine.containers.gui.VenderBlockScreen;
import screret.vendingmachine.events.packets.*;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.integration.polymorph.VendingMachinesCompatibilityModule;
import screret.vendingmachine.items.MoneyItem;
import screret.vendingmachine.mixin.AccessorPolymorphIntegrations;
import screret.vendingmachine.recipes.builders.MoneyConversionRecipeProvider;

import java.util.Optional;
import java.util.function.Supplier;

//COMPLETELY RANDOM UUID AND USERNAME FOR TESTING: --uuid=76d4e724-c758-42b1-8006-00d5d676d4a7 --username=abgdef

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VendingMachine.MODID)
public class VendingMachine {
    public static final String MODID = "vendingmachine";
    public static final String POLYMORPH_MODID = "polymorph";

    public static final CreativeModeTab MOD_TAB = new CreativeModeTab("vendingmachine") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.VENDER_ITEM_BLUE.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            CompoundTag moneyValue;

            for(int value = 0; value < MoneyItem.MONEY_VALUES.length; value++){
                moneyValue = new CompoundTag();
                moneyValue.putFloat(MoneyItem.MONEY_VALUE_TAG, MoneyItem.MONEY_VALUES[value]);
                ItemStack stack = new ItemStack(Registration.MONEY.get());
                stack.setTag(moneyValue);
                items.add(stack);
                moneyValue = null;
            }

            for(Item item : ForgeRegistries.ITEMS) {
                if(item != Registration.MONEY.get())
                    item.fillItemCategory(this, items);
            }
        }
    };

    public static final Logger LOGGER = LogManager.getLogger(VendingMachine.MODID);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VendingMachine.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    static {
        if(ModList.get().isLoaded(POLYMORPH_MODID)){
            final Supplier<AbstractCompatibilityModule> sup = VendingMachinesCompatibilityModule::new;
            AccessorPolymorphIntegrations.getConfigActivated().add(MODID);
            AccessorPolymorphIntegrations.getIntegrations().put(MODID, () -> sup);
            AccessorPolymorphIntegrations.getActiveIntegrations().add(sup.get());
        }
    }

    public VendingMachine() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        modEventBus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        modEventBus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        modEventBus.addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(this::doClientStuff);
        //register datagen for modloading
        modEventBus.addListener(this::gatherData);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VendingMachineConfig.spec);

        Registration.BLOCKS.register(modEventBus);
        Registration.ITEMS.register(modEventBus);
        Registration.CONTAINERS.register(modEventBus);
        Registration.TILES.register(modEventBus);
        Registration.RECIPE_SERIALIZERS.register(modEventBus);
        Registration.RECIPE_TYPES.register(modEventBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NETWORK_HANDLER.registerMessage(0, PacketSendBuy.class, PacketSendBuy::encode, PacketSendBuy::new, PacketSendBuy::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        //NETWORK_HANDLER.registerMessage(1, PacketAllowItemTake.class, PacketAllowItemTake::encode, PacketAllowItemTake::new, PacketAllowItemTake::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(2, OpenVenderGUIPacket.class, OpenVenderGUIPacket::encode, OpenVenderGUIPacket::new, OpenVenderGUIPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(3, ChangePricePacket.class, ChangePricePacket::encode, ChangePricePacket::new, ChangePricePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(4, CashOutPacket.class, CashOutPacket::encode, CashOutPacket::new, CashOutPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        //NETWORK_HANDLER.registerMessage(5, SendOwnerToClientPacket.class, SendOwnerToClientPacket::encode, SendOwnerToClientPacket::new, SendOwnerToClientPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        //NETWORK_HANDLER.registerMessage(6, LoadChunkPacket.class, LoadChunkPacket::encode, LoadChunkPacket::new, LoadChunkPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(Registration.VENDER_CONT.get(), VenderBlockScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.VENDER_CONT_PRICES.get(), VenderBlockPriceScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.CONTAINER_CONTROL_CARD.get(), ControlCardScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.CASH_CONVERTER_CONT.get(), CashConverterScreen::new));

        event.enqueueWork(() -> ItemProperties.register(Registration.MONEY.get(), new ResourceLocation(VendingMachine.MODID, "money_value"), (stack, world, holdingEntity, entityId) -> MoneyItem.getMoneyValue(stack)));

        //event.enqueueWork(() -> PolymorphIntegrations.clientSetup());
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod

    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        ControlCardCapability.register(event);
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods

    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    /*@SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }*/

    private void gatherData(final GatherDataEvent event){
        DataGenerator gen = event.getGenerator();

        gen.addProvider(event.includeServer(), new MoneyConversionRecipeProvider(gen));
    }
}
