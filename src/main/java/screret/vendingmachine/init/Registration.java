package screret.vendingmachine.init;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.CashConverterBlock;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.containers.*;
import screret.vendingmachine.items.ControlCardItem;
import screret.vendingmachine.items.MoneyItem;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;
import screret.vendingmachine.items.WalletItem;
import screret.vendingmachine.recipes.MoneyConversionRecipe;

import java.util.UUID;

public class Registration {

    //registries
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VendingMachine.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, VendingMachine.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VendingMachine.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, VendingMachine.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, VendingMachine.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VendingMachine.MODID);


    //blocks
    public static final RegistryObject<Block> VENDER_BLUE = BLOCKS.register("vending_machine_blue", () -> vendingMachine(DyeColor.BLUE));
    public static final RegistryObject<Block> VENDER_RED = BLOCKS.register("vending_machine_red", () -> vendingMachine(DyeColor.RED));
    public static final RegistryObject<Block> VENDER_WHITE = BLOCKS.register("vending_machine_white", () -> vendingMachine(DyeColor.WHITE));
    public static final RegistryObject<Block> VENDER_GRAY = BLOCKS.register("vending_machine_gray", () -> vendingMachine(DyeColor.GRAY));
    public static final RegistryObject<Block> VENDER_LIGHT_GRAY = BLOCKS.register("vending_machine_light_gray", () -> vendingMachine(DyeColor.LIGHT_GRAY));
    public static final RegistryObject<Block> VENDER_BLACK = BLOCKS.register("vending_machine_black", () -> vendingMachine(DyeColor.BLACK));
    public static final RegistryObject<Block> VENDER_ORANGE = BLOCKS.register("vending_machine_orange", () -> vendingMachine(DyeColor.ORANGE));
    public static final RegistryObject<Block> VENDER_MAGENTA = BLOCKS.register("vending_machine_magenta", () -> vendingMachine(DyeColor.MAGENTA));
    public static final RegistryObject<Block> VENDER_LIGHT_BLUE = BLOCKS.register("vending_machine_light_blue", () -> vendingMachine(DyeColor.LIGHT_BLUE));
    public static final RegistryObject<Block> VENDER_YELLOW = BLOCKS.register("vending_machine_yellow", () -> vendingMachine(DyeColor.YELLOW));
    public static final RegistryObject<Block> VENDER_LIME = BLOCKS.register("vending_machine_lime", () -> vendingMachine(DyeColor.LIME));
    public static final RegistryObject<Block> VENDER_PINK = BLOCKS.register("vending_machine_pink", () -> vendingMachine(DyeColor.PINK));
    public static final RegistryObject<Block> VENDER_CYAN = BLOCKS.register("vending_machine_cyan", () -> vendingMachine(DyeColor.CYAN));
    public static final RegistryObject<Block> VENDER_BROWN = BLOCKS.register("vending_machine_brown", () -> vendingMachine(DyeColor.BROWN));
    public static final RegistryObject<Block> VENDER_PURPLE = BLOCKS.register("vending_machine_purple", () -> vendingMachine(DyeColor.PURPLE));
    public static final RegistryObject<Block> VENDER_GREEN = BLOCKS.register("vending_machine_green", () -> vendingMachine(DyeColor.GREEN));

    public static final RegistryObject<Block> CASH_CONVERTER = BLOCKS.register("cash_converter", () -> new CashConverterBlock(Block.Properties.of(Material.HEAVY_METAL).strength(3.5f).lightLevel(a -> 10)));

    private static VendingMachineBlock vendingMachine(DyeColor color) {
        return new VendingMachineBlock(color, Block.Properties.of(Material.HEAVY_METAL).strength(3.5F).lightLevel(a -> 10));
    }

    //tile entities
    public static final RegistryObject<BlockEntityType<VendingMachineBlockEntity>> VENDER_TILE = TILES.register("vending_machine_tile", () ->
            BlockEntityType.Builder.of(VendingMachineBlockEntity::new,
                    VENDER_BLUE.get(), VENDER_RED.get(), VENDER_WHITE.get(), VENDER_GRAY.get(), VENDER_LIGHT_GRAY.get(), VENDER_BLACK.get(), VENDER_ORANGE.get(), VENDER_MAGENTA.get(), VENDER_LIGHT_BLUE.get(), VENDER_YELLOW.get(), VENDER_LIME.get(), VENDER_PINK.get(), VENDER_CYAN.get(), VENDER_BROWN.get(), VENDER_PURPLE.get(), VENDER_GREEN.get()
                    ).build(null));



    //Items
    public static final RegistryObject<Item> VENDER_ITEM_WHITE = ITEMS.register("vending_machine_white", () -> new BlockItem(Registration.VENDER_WHITE.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_ORANGE = ITEMS.register("vending_machine_orange", () -> new BlockItem(Registration.VENDER_ORANGE.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_MAGENTA = ITEMS.register("vending_machine_magenta", () -> new BlockItem(Registration.VENDER_MAGENTA.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_LIGHT_BLUE = ITEMS.register("vending_machine_light_blue", () -> new BlockItem(Registration.VENDER_LIGHT_BLUE.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_YELLOW = ITEMS.register("vending_machine_yellow", () -> new BlockItem(Registration.VENDER_YELLOW.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_LIME = ITEMS.register("vending_machine_lime", () -> new BlockItem(Registration.VENDER_LIME.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_PINK = ITEMS.register("vending_machine_pink", () -> new BlockItem(Registration.VENDER_PINK.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_GRAY = ITEMS.register("vending_machine_gray", () -> new BlockItem(Registration.VENDER_GRAY.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_LIGHT_GRAY = ITEMS.register("vending_machine_light_gray", () -> new BlockItem(Registration.VENDER_LIGHT_GRAY.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_CYAN = ITEMS.register("vending_machine_cyan", () -> new BlockItem(Registration.VENDER_CYAN.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_PURPLE = ITEMS.register("vending_machine_purple", () -> new BlockItem(Registration.VENDER_PURPLE.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_BLUE = ITEMS.register("vending_machine_blue", () -> new BlockItem(Registration.VENDER_BLUE.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_BROWN = ITEMS.register("vending_machine_brown", () -> new BlockItem(Registration.VENDER_BROWN.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_GREEN = ITEMS.register("vending_machine_green", () -> new BlockItem(Registration.VENDER_GREEN.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_RED = ITEMS.register("vending_machine_red", () -> new BlockItem(Registration.VENDER_RED.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> VENDER_ITEM_BLACK = ITEMS.register("vending_machine_black", () -> new BlockItem(Registration.VENDER_BLACK.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> CASH_CONVERTER_ITEM = ITEMS.register("cash_converter", () -> new BlockItem(Registration.CASH_CONVERTER.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));

    //public static final RegistryObject<Item> VENDER_CONTROL_CARD = ITEMS.register("vender_controller", () -> new ControlCardItem(new Item.Properties().tab(VendingMachine.MOD_TAB)));

    public static final RegistryObject<Item> MONEY = ITEMS.register("money", () -> new MoneyItem(new Item.Properties().tab(VendingMachine.MOD_TAB)));
    public static final RegistryObject<Item> WALLET = ITEMS.register("wallet", () -> new WalletItem(new Item.Properties().tab(VendingMachine.MOD_TAB)));

    //recipe types
    public static final RegistryObject<RecipeType<MoneyConversionRecipe>> MONEY_CONVERSION_RECIPE_TYPE = RECIPE_TYPES.register("money_conversion", MoneyConversionRecipe.RecipeType::new);
    public static final RegistryObject<RecipeSerializer<MoneyConversionRecipe>> MONEY_CONVERSION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("money_conversion", MoneyConversionRecipe.Serializer::new);


    //containers
    public static final RegistryObject<MenuType<VenderBlockMenu>> VENDER_MENU = MENU_TYPES.register("vending_machine_menu", () -> IForgeMenuType.create((windowId, inv, buffer) -> {
        BlockPos pos = buffer.readBlockPos();
        Level world = inv.player.getCommandSenderWorld();
        VendingMachineBlockEntity tile = (VendingMachineBlockEntity) world.getBlockEntity(pos);
        if(tile == null){
            tile = (VendingMachineBlockEntity) world.getBlockEntity(pos.below());
        }
        return new VenderBlockMenu(windowId, inv, tile.inventory, tile.otherSlots, tile);
    }));

    public static final RegistryObject<MenuType<VenderPriceEditorMenu>> VENDER_PRICES_MENU = MENU_TYPES.register("vending_machine_prices_menu", () -> IForgeMenuType.create((windowId, inv, buffer) -> {
        BlockPos pos = buffer.readBlockPos();
        Level world = inv.player.getCommandSenderWorld();
        VendingMachineBlockEntity tile = (VendingMachineBlockEntity) world.getBlockEntity(pos);
        if(tile == null){
            tile = (VendingMachineBlockEntity) world.getBlockEntity(pos.below());
        }
        return new VenderPriceEditorMenu(windowId, inv, tile.inventory, tile);
    }));

    public static final RegistryObject<MenuType<CashConverterMenu>> CASH_CONVERTER_MENU = MENU_TYPES.register("cash_converter_menu", () -> IForgeMenuType.create((windowId, inv, buffer) -> {
        return new CashConverterMenu(windowId, inv, ContainerLevelAccess.create(inv.player.getCommandSenderWorld(), buffer.readBlockPos()));
    }));

    public static final RegistryObject<MenuType<ControlCardMenu>> CONTROL_CARD_MENU = MENU_TYPES.register("control_card_menu", () -> IForgeMenuType.create((windowId, inv, buffer) -> {
        UUID uuid = inv.player.getUUID();
        return new ControlCardMenu(windowId, inv, uuid, ControlCardItem.getController((ControlCardItem) inv.player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), inv.player.getUseItem()));
    }));

    public static final RegistryObject<MenuType<WalletItemMenu>> WALLET_MENU = MENU_TYPES.register("wallet_menu", () -> IForgeMenuType.create((windowId, inv, buffer) -> new WalletItemMenu(windowId, inv, inv.player.getMainHandItem())));
}
