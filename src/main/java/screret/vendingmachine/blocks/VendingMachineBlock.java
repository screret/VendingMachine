package screret.vendingmachine.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.UUID;

public class VendingMachineBlock extends HorizontalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public VendingMachineBlock(Properties properties) {
        super(properties);
        //HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
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
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);//.with(BlockStateProperties.HORIZONTAL_FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        if(world.getBlockState(pos.above()).getBlock() != Blocks.AIR){
            world.removeBlockEntity(pos);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof VendingMachineTile){
            VendingMachineTile _tile = (VendingMachineTile)tile;
            if(_tile.owner == null){
                _tile.owner = entity.getUUID();
            }
        }
        world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, state.getValue(FACING)), 3);
        world.blockUpdated(pos, this);
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
                //finalTileEntity.currentPlayer = player.getUUID();


                NetworkHooks.openGui((ServerPlayerEntity) player, finalTileEntity, buffer -> buffer.writeBlockPos(finalBlockPos));
                //player.openMenu(provider);

            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            return ActionResultType.CONSUME;
        }else{
            return ActionResultType.SUCCESS;
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getValue(HALF) == DoubleBlockHalf.LOWER){
            TileEntity tileEntity = worldIn.getBlockEntity(pos);
            if(tileEntity == null){
                tileEntity = worldIn.getBlockEntity(pos.below());
            }
            ((VendingMachineTile)tileEntity).dropContents();
            worldIn.removeBlockEntity(pos);
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
        return new VendingMachineTile();
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity playerEntity) {
        BlockPos blockpos = pos.below();
        BlockState blockState = world.getBlockState(blockpos);
        if(state.getBlock() == this && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            world.levelEvent(playerEntity, 2001, blockpos, Block.getId(blockState));
        } else if(state.getBlock() == this && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            blockpos = pos.above();
            blockState = world.getBlockState(blockpos);
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            world.levelEvent(playerEntity, 2001, blockpos, Block.getId(blockState));
        }
        super.playerWillDestroy(world, pos, state, playerEntity);
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if(tileentity == null){
            tileentity = world.getBlockEntity(pos.below());
        }
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
    }
}
