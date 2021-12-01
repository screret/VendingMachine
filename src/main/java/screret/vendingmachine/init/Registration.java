package screret.vendingmachine.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.tileEntities.VendingMachineTile;

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
    public static final RegistryObject<Block> VENDER = BLOCKS.register("vending_machine", () -> new VendingMachineBlock(AbstractBlock.Properties.of(Material.HEAVY_METAL).harvestTool(ToolType.PICKAXE).strength(3.5F).lightLevel(a -> 10)));


    //tile entities
    public static final RegistryObject<TileEntityType<VendingMachineTile>> VENDER_TILE = TILES.register("vending_machine_tile", () -> TileEntityType.Builder.of(VendingMachineTile::new, VENDER.get()).build(null));



    //Items
    public static final RegistryObject<Item> VENDER_ITEM = ITEMS.register("item_vending_machine", () -> new BlockItem(Registration.VENDER.get(), new Item.Properties().tab(VendingMachine.MOD_TAB)));

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

    public static final RegistryObject<ContainerType<VenderBlockContainer>> VENDER_CONT_PRICES = CONTAINERS.register("container_vending_machine_prices", () -> IForgeContainerType.create((windowId, inv, buffer) -> {
        BlockPos pos = buffer.readBlockPos();
        World world = inv.player.getCommandSenderWorld();
        VendingMachineTile tile = (VendingMachineTile) world.getBlockEntity(pos);
        if(tile == null){
            tile = (VendingMachineTile) world.getBlockEntity(pos.below());
        }
        return new VenderBlockContainer(windowId, inv, tile.inputSlot, tile.outputSlot, tile.moneySlot, tile);
    }));
}
