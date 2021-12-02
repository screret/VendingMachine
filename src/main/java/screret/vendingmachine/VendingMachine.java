package screret.vendingmachine;

import javafx.beans.property.ObjectProperty;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.gui.VenderBlockPriceScreen;
import screret.vendingmachine.containers.gui.VenderBlockScreen;
import screret.vendingmachine.events.packets.AddPricePacket;
import screret.vendingmachine.events.packets.OpenGUIPacket;
import screret.vendingmachine.events.packets.PacketAllowItemTake;
import screret.vendingmachine.events.packets.PacketSendBuy;
import screret.vendingmachine.init.Registration;

import java.util.Optional;
import java.util.stream.Collectors;

//COMPLETELY RANDOM UUID AND USERNAME FOR TESTING: --uuid=76d4e724-c758-42b1-8006-00d5d676d4a7 --username=abgdef

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VendingMachine.MODID)
public class VendingMachine {
    public static final String MODID = "vendingmachine";

    public static final ItemGroup MOD_TAB = new ItemGroup("vendingmachine") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.VENDER_ITEM.get());
        }
    };


    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VendingMachine.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public VendingMachine() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VendingMachineConfig.spec);

        Registration.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registration.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registration.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registration.TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NETWORK_HANDLER.registerMessage(0, PacketSendBuy.class, PacketSendBuy::encode, PacketSendBuy::new, PacketSendBuy::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(1, PacketAllowItemTake.class, PacketAllowItemTake::encode, PacketAllowItemTake::new, PacketAllowItemTake::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(2, OpenGUIPacket.class, OpenGUIPacket::encode, OpenGUIPacket::new, OpenGUIPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(3, AddPricePacket.class, AddPricePacket::encode, AddPricePacket::new, AddPricePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ScreenManager.register(Registration.VENDER_CONT.get(), VenderBlockScreen::new));
        event.enqueueWork(() -> ScreenManager.register(Registration.VENDER_CONT_PRICES.get(), VenderBlockPriceScreen::new));
        LOGGER.debug("Screens Registered");
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(MODID, "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    /*@SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }*/

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
