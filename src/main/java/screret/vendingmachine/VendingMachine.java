package screret.vendingmachine;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.capabilities.configs.VendingMachineConfig;
import screret.vendingmachine.containers.gui.ControlCardScreen;
import screret.vendingmachine.containers.gui.VenderBlockPriceScreen;
import screret.vendingmachine.containers.gui.VenderBlockScreen;
import screret.vendingmachine.events.packets.*;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.items.MoneyItem;

import javax.annotation.Nullable;
import java.util.Optional;

//COMPLETELY RANDOM UUID AND USERNAME FOR TESTING: --uuid=76d4e724-c758-42b1-8006-00d5d676d4a7 --username=abgdef

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VendingMachine.MODID)
public class VendingMachine {
    public static final String MODID = "vendingmachine";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final ItemGroup MOD_TAB = new ItemGroup("vendingmachine") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.VENDER_ITEM_BLUE.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            CompoundNBT moneyValue;

            for(int value = 0; value < MoneyItem.MONEY_VALUES.length; value++){
                moneyValue = new CompoundNBT();
                moneyValue.putFloat(MoneyItem.MONEY_VALUE_TAG, MoneyItem.MONEY_VALUES[value]);
                ItemStack stack = new ItemStack(Registration.MONEY.get());
                stack.setTag(moneyValue);
                items.add(stack);
                moneyValue = null;
            }

            for(Item item : ForgeRegistries.ITEMS) {
                item.fillItemCategory(this, items);
            }
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
        ControlCardCapability.register();
        NETWORK_HANDLER.registerMessage(0, PacketSendBuy.class, PacketSendBuy::encode, PacketSendBuy::new, PacketSendBuy::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(1, PacketAllowItemTake.class, PacketAllowItemTake::encode, PacketAllowItemTake::new, PacketAllowItemTake::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(2, OpenVenderGUIPacket.class, OpenVenderGUIPacket::encode, OpenVenderGUIPacket::new, OpenVenderGUIPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(3, ChangePricePacket.class, ChangePricePacket::encode, ChangePricePacket::new, ChangePricePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_HANDLER.registerMessage(4, DropMoneyOnClosePacket.class, DropMoneyOnClosePacket::encode, DropMoneyOnClosePacket::new, DropMoneyOnClosePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ScreenManager.register(Registration.VENDER_CONT.get(), VenderBlockScreen::new));
        event.enqueueWork(() -> ScreenManager.register(Registration.VENDER_CONT_PRICES.get(), VenderBlockPriceScreen::new));
        event.enqueueWork(() -> ScreenManager.register(Registration.CONTAINER_CONTROL_CARD.get(), ControlCardScreen::new));

        event.enqueueWork(() -> ItemModelsProperties.register(Registration.MONEY.get(), new ResourceLocation(VendingMachine.MODID, "money_value"), new IItemPropertyGetter() {
            @Override
            public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity holdingEntity) {
                return MoneyItem.getMoneyValue(stack);
            }
        }));
    }
}
