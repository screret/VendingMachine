package screret.vendingmachine.blocks;

import io.netty.buffer.Unpooled;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VendingMachineBlock extends HorizontalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public PlayerEntity owner;

    public VendingMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
//        Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/block/oak_planks.png"));
//        Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/block/glass.png"));
//        Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/block/iron_block.png"));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder){
        builder.add(FACING, HALF);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        owner = context.getPlayer();
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HALF, DoubleBlockHalf.LOWER);//.with(BlockStateProperties.HORIZONTAL_FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        if(world.getBlockState(pos.above()).getBlock() != Blocks.AIR){
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 0);
            //owner.addItem(new ItemStack(Registration.VENDER.get()));
            return;
        }
        world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, getHorizontalDirection()), 3);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
        if (!world.isClientSide) {
            TileEntity tileEntity = world.getBlockEntity(blockPos);
            if(tileEntity == null){
                tileEntity = world.getBlockEntity(blockPos.below());
                blockPos = blockPos.below();
            }
            if (tileEntity instanceof VendingMachineTile) {
                VendingMachineTile finalTileEntity = (VendingMachineTile)tileEntity;
                BlockPos finalBlockPos = blockPos;

                NetworkHooks.openGui((ServerPlayerEntity) player, finalTileEntity, buffer -> buffer.writeBlockPos(finalBlockPos));
                //player.openMenu(provider);

            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            LOGGER.info("opened a Vending Machine");
            return ActionResultType.SUCCESS;
        }else{
            return ActionResultType.SUCCESS;
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if(tileEntity == null){
            tileEntity = worldIn.getBlockEntity(pos.below());
        }
        ((VendingMachineTile)tileEntity).dropContents();
        worldIn.removeBlockEntity(pos);
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldReader, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = worldReader.getBlockState(blockpos);
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? blockstate.isFaceSturdy(worldReader, blockpos, Direction.UP) : blockstate.is(this);
    }

    @SubscribeEvent
    public void breakBlockEvent(BlockEvent.BreakEvent event) {
        if (owner != event.getPlayer()) {
            event.setCanceled(true);
        }
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
        if(state.getValue(HALF) == DoubleBlockHalf.LOWER){
            VendingMachineTile tile = new VendingMachineTile();
            tile.owner = owner;
            return tile;
        }else{
            return null;
        }

    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if(tileentity == null){
            tileentity = world.getBlockEntity(pos.below());
        }
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
    }

    public Direction getHorizontalDirection() {
        if(owner.getDirection() == Direction.NORTH || owner.getDirection() == Direction.SOUTH){
            return owner.getDirection().getAxis() == Direction.Axis.Y ? Direction.NORTH : owner.getDirection().getOpposite();
        }else {
            return owner.getDirection().getAxis() == Direction.Axis.Y ? Direction.NORTH : owner.getDirection();
        }
    }
}
