package screret.vendingmachine;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.NonNullList;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.gui.*;
import screret.vendingmachine.events.packets.*;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.integration.polymorph.VendingMachinesCompatibilityLoader;
import screret.vendingmachine.recipes.builders.MoneyConversionRecipeProvider;
import screret.vendingmachine.util.Util;

import java.util.Optional;

//COMPLETELY RANDOM UUID AND USERNAME FOR TESTING: --uuid=76d4e724-c758-42b1-8006-00d5d676d4a7 --username=abgdef

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VendingMachine.MODID)
public class VendingMachine {
    public static final String MODID = "vendingmachine";

    public static final CreativeModeTab MOD_TAB = new CreativeModeTab(MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.VENDER_ITEM_BLUE.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            CompoundTag moneyValue;

            for(int value = 0; value < Util.MONEY_VALUES.length; value++){
                ItemStack stack = Util.setMoneyValue(new ItemStack(Registration.MONEY.get()), Util.MONEY_VALUES[value]);
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
        if(FMLLoader.getLoadingModList().getModFileById(VendingMachinesCompatibilityLoader.POLYMORPH_MODID) != null) {
            //VendingMachinesCompatibilityLoader.init();
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
        //MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VendingMachineConfig.spec);

        Registration.BLOCKS.register(modEventBus);
        Registration.ITEMS.register(modEventBus);
        Registration.MENU_TYPES.register(modEventBus);
        Registration.TILES.register(modEventBus);
        Registration.RECIPE_SERIALIZERS.register(modEventBus);
        Registration.RECIPE_TYPES.register(modEventBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        int index = 0;
        NETWORK_HANDLER.registerMessage(index++, PacketBuyC2S.class, PacketBuyC2S::encode, PacketBuyC2S::new, PacketBuyC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(index++, OpenGuiPacketC2S.class, OpenGuiPacketC2S::encode, OpenGuiPacketC2S::new, OpenGuiPacketC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(index++, PacketPriceChangeC2S.class, PacketPriceChangeC2S::encode, PacketPriceChangeC2S::new, PacketPriceChangeC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(index++, PacketCashOutC2S.class, PacketCashOutC2S::encode, PacketCashOutC2S::new, PacketCashOutC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        NETWORK_HANDLER.registerMessage(index++, PacketInsertedMoneyS2C.class, PacketInsertedMoneyS2C::encode, PacketInsertedMoneyS2C::new, PacketInsertedMoneyS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        //NETWORK_HANDLER.registerMessage(5, SendOwnerToClientPacket.class, SendOwnerToClientPacket::encode, SendOwnerToClientPacket::new, SendOwnerToClientPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        //NETWORK_HANDLER.registerMessage(6, PacketLoadChunkC2S.class, PacketLoadChunkC2S::encode, PacketLoadChunkC2S::new, PacketLoadChunkC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(Registration.VENDER_MENU.get(), VenderBlockScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.VENDER_PRICES_MENU.get(), VenderBlockPriceScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.CONTROL_CARD_MENU.get(), ControlCardScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.CASH_CONVERTER_MENU.get(), CashConverterScreen::new));
        event.enqueueWork(() -> MenuScreens.register(Registration.WALLET_MENU.get(), WalletScreen::new));

        event.enqueueWork(() -> ItemProperties.register(Registration.MONEY.get(), new ResourceLocation(VendingMachine.MODID, "money_value"), (stack, world, holdingEntity, entityId) -> Util.getMoneyValue(stack)));

        //event.enqueueWork(() -> PolymorphIntegrations.clientSetup());
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

    private void gatherData(final GatherDataEvent event){
        DataGenerator gen = event.getGenerator();

        gen.addProvider(new MoneyConversionRecipeProvider(gen));
    }
}
