package screret.vendingmachine.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.capabilities.ControlCardCapability;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.containers.ContainerControlCard;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.containers.VenderPriceEditorContainer;
import screret.vendingmachine.items.ControlCardItem;
import screret.vendingmachine.items.MoneyItem;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.Arrays;
import java.util.Comparator;

public class Registration {

    //registries
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VendingMachine.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, VendingMachine.MODID);
    public static final DeferredRegister<Attribute> ENTITY_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, VendingMachine.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, VendingMachine.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, VendingMachine.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, VendingMachine.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VendingMachine.MODID);


    //blocks
    public static final RegistryObject<Block> VENDER_BLUE = BLOCKS.register("vending_machine_blue", () -> vender(DyeColor.BLUE));
    public static final RegistryObject<Block> VENDER_RED = BLOCKS.register("vending_machine_red", () -> vender(DyeColor.RED));
    public static final RegistryObject<Block> VENDER_WHITE = BLOCKS.register("vending_machine_white", () -> vender(DyeColor.WHITE));
    public static final RegistryObject<Block> VENDER_GRAY = BLOCKS.register("vending_machine_gray", () -> vender(DyeColor.GRAY));
    public static final RegistryObject<Block> VENDER_LIGHT_GRAY = BLOCKS.register("vending_machine_light_gray", () -> vender(DyeColor.LIGHT_GRAY));
    public static final RegistryObject<Block> VENDER_BLACK = BLOCKS.register("vending_machine_black", () -> vender(DyeColor.BLACK));
    public static final RegistryObject<Block> VENDER_ORANGE = BLOCKS.register("vending_machine_orange", () -> vender(DyeColor.ORANGE));
    public static final RegistryObject<Block> VENDER_MAGENTA = BLOCKS.register("vending_machine_magenta", () -> vender(DyeColor.MAGENTA));
    public static final RegistryObject<Block> VENDER_LIGHT_BLUE = BLOCKS.register("vending_machine_light_blue", () -> vender(DyeColor.LIGHT_BLUE));
    public static final RegistryObject<Block> VENDER_YELLOW = BLOCKS.register("vending_machine_yellow", () -> vender(DyeColor.YELLOW));
    public static final RegistryObject<Block> VENDER_LIME = BLOCKS.register("vending_machine_lime", () -> vender(DyeColor.LIME));
    public static final RegistryObject<Block> VENDER_PINK = BLOCKS.register("vending_machine_pink", () -> vender(DyeColor.PINK));
    public static final RegistryObject<Block> VENDER_CYAN = BLOCKS.register("vending_machine_cyan", () -> vender(DyeColor.CYAN));
    public static final RegistryObject<Block> VENDER_BROWN = BLOCKS.register("vending_machine_brown", () -> vender(DyeColor.BROWN));
    public static final RegistryObject<Block> VENDER_PURPLE = BLOCKS.register("vending_machine_purple", () -> vender(DyeColor.PURPLE));
    public static final RegistryObject<Block> VENDER_GREEN = BLOCKS.register("vending_machine_green", () -> vender(DyeColor.GREEN));

    private static VendingMachineBlock vender(DyeColor color) {
        return new VendingMachineBlock(color, AbstractBlock.Properties.of(Material.HEAVY_METAL).harvestTool(ToolType.PICKAXE).strength(3.5F).lightLevel(a -> 10));
    }

    //tile entities
    public static final RegistryObject<TileEntityType<VendingMachineTile>> VENDER_TILE = TILES.register("vending_machine_tile", () -> TileEntityType.Builder.of(VendingMachineTile::new, VENDER_BLUE.get()).build(null));



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

    //TODO: make this work
    //public static final RegistryObject<Item> VENDER_CONTROL_CARD = ITEMS.register("vending_machine_controller", () -> new ControlCardItem(new Item.Properties().tab(VendingMachine.MOD_TAB).stacksTo(1), null));

    public static final RegistryObject<Item> MONEY = ITEMS.register("money", () -> new MoneyItem(new Item.Properties()));

    //containers
    public static final RegistryObject<ContainerType<VenderBlockContainer>> VENDER_CONT = CONTAINERS.register("container_vending_machine", () -> IForgeContainerType.create((windowId, inv, buffer) -> {
        BlockPos pos = buffer.readBlockPos();
        World world = inv.player.getCommandSenderWorld();
        VendingMachineTile tile = (VendingMachineTile) world.getBlockEntity(pos);
        if(tile == null){
            tile = (VendingMachineTile) world.getBlockEntity(pos.below());
        }
        return new VenderBlockContainer(windowId, inv, tile.inputSlot, tile.outputSlot, tile.moneySlot, tile);
    }));

    public static final RegistryObject<ContainerType<VenderPriceEditorContainer>> VENDER_CONT_PRICES = CONTAINERS.register("container_vending_machine_prices", () -> IForgeContainerType.create((windowId, inv, buffer) -> {
        BlockPos pos = buffer.readBlockPos();
        World world = inv.player.getCommandSenderWorld();
        VendingMachineTile tile = (VendingMachineTile) world.getBlockEntity(pos);
        if(tile == null){
            tile = (VendingMachineTile) world.getBlockEntity(pos.below());
        }
        return new VenderPriceEditorContainer(windowId, inv, tile);
    }));

    public static final RegistryObject<ContainerType<ContainerControlCard>> CONTAINER_CONTROL_CARD = CONTAINERS.register("container_control_card", () -> IForgeContainerType.create((windowId, inv, buffer) -> {
        PlayerEntity player = inv.player;
        ItemStack stack = player.getItemInHand(Hand.MAIN_HAND);
        if(stack.isEmpty()){
            stack = player.getItemInHand(Hand.OFF_HAND);
        }
        ControlCardItem item = (ControlCardItem) stack.getItem();

        if(item.getOwner() == null){
            item.setOwner(inv.player.getUUID());
        }
        return new ContainerControlCard(windowId, inv, item.getOwner(), ControlCardItem.getController(item, stack));
    }));
}
