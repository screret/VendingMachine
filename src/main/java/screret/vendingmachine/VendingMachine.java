package screret.vendingmachine;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
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
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.gui.VenderBlockPriceScreen;
import screret.vendingmachine.containers.gui.VenderBlockScreen;
import screret.vendingmachine.events.packets.*;
import screret.vendingmachine.init.Registration;

import java.util.Optional;

//COMPLETELY RANDOM UUID AND USERNAME FOR TESTING: --uuid=76d4e724-c758-42b1-8006-00d5d676d4a7 --username=abgdef

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VendingMachine.MODID)
public class VendingMachine {
    public static final String MODID = "vendingmachine";

    public static final CreativeModeTab MOD_TAB = new CreativeModeTab("vendingmachine") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.VENDER_ITEM_BLUE.get());
        }
    };


    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VendingMachine.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

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
        VendingMachineConfig.DECRYPTED_PRICES = VendingMachineConfig.decryptPrices();

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
        NETWORK_HANDLER.registerMessage(3, ChangePricePacket.class, ChangePricePacket::encode, ChangePricePacket::new, ChangePricePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(4, DropMoneyOnClosePacket.class, DropMoneyOnClosePacket::encode, DropMoneyOnClosePacket::new, DropMoneyOnClosePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(Registration.VENDER_CONT.get(), VenderBlockScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.VENDER_CONT_PRICES.get(), VenderBlockPriceScreen::new));
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod

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
}
