package screret.vendingmachine.init;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.FoliagePlacerType;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.treedecorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.blocks.VendingMachineBlock;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

//import io.screret.github.juicesandsodas.entities.KoolaidMan;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = VendingMachine.MODID)
public class Registration {

    public Registration() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.BLOCKS.register(modEventBus);
        Registration.ITEMS.register(modEventBus);
        Registration.ENTITIES.register(modEventBus);
        Registration.TILES.register(modEventBus);
        Registration.CONTAINERS.register(modEventBus);
        Registration.RECIPE_SERIALIZERS.register(modEventBus);
    }

    //registries
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VendingMachine.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, VendingMachine.MODID);
    public static final DeferredRegister<Attribute> ENTITY_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, VendingMachine.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, VendingMachine.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, VendingMachine.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, VendingMachine.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VendingMachine.MODID);


    //blocks
    public static final RegistryObject<Block> VENDER = BLOCKS.register("vending_machine", () -> new VendingMachineBlock(AbstractBlock.Properties.of(Material.HEAVY_METAL)));


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
        return new VenderBlockContainer(windowId, inv, tile.combinedInvWrapper, tile);
    }));
}
