package screret.vendingmachine.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.util.UUID;

public class VendingMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    private DyeColor color;

    public VendingMachineBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER).setValue(COLOR, color));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(FACING, HALF, COLOR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER).setValue(COLOR, color);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        if(world.getBlockState(pos.above()).getBlock() != Blocks.AIR){
            world.removeBlockEntity(pos);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if(be instanceof VendingMachineTile venderBe){
            if(venderBe.owner == null){
                venderBe.owner = entity.getUUID();
            }
        }

        world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, state.getValue(FACING)), 3);
        world.blockUpdated(pos, this);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (!world.isClientSide) {
            BlockEntity tileEntity = world.getBlockEntity(blockPos);
            if(tileEntity == null){
                tileEntity = world.getBlockEntity(blockPos.below());
                blockPos = blockPos.below();
            }
            if (tileEntity instanceof VendingMachineTile finalTileEntity) {
                BlockPos finalBlockPos = blockPos;
                NetworkHooks.openGui((ServerPlayer) player, finalTileEntity, buffer -> buffer.writeBlockPos(finalBlockPos));

            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            return InteractionResult.CONSUME;
        }else{
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getValue(HALF) == DoubleBlockHalf.LOWER){
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if(worldIn instanceof ServerLevel && blockEntity instanceof VendingMachineTile venderBe){
                venderBe.dropContents();
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player playerEntity) {
        BlockPos blockpos = pos.below();
        BlockState blockState = world.getBlockState(blockpos);
        if(state.getBlock() == this && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            world.levelEvent(playerEntity, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockState));
        } else if(state.getBlock() == this && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            blockpos = pos.above();
            blockState = world.getBlockState(blockpos);
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            world.levelEvent(playerEntity, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockState));
        }
        super.playerWillDestroy(world, pos, state, playerEntity);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if(tileentity == null){
            tileentity = world.getBlockEntity(pos.below());
        }
        return tileentity instanceof MenuProvider ? (MenuProvider)tileentity : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? Registration.VENDER_TILE.get().create(pos, state) : null;
    }
}
