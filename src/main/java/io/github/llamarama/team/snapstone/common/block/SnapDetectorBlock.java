package io.github.llamarama.team.snapstone.common.block;

import io.github.llamarama.team.snapstone.SnapStone;
import io.github.llamarama.team.snapstone.common.block_entity.PersonalizedSnapDetectorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.Stream;

public class SnapDetectorBlock extends Block {

    public static final BooleanProperty TRIGGERED = BooleanProperty.of("triggered");
    public static final IntProperty POWER = Properties.POWER;
    private static final VoxelShape NORMAL_SHAPE = Stream.of(
                    Block.createCuboidShape(2.0, 8.0, 2.0, 14.0, 11.0, 14.0),
                    Block.createCuboidShape(3.0, 3.0, 3.0, 13.0, 8.0, 13.0),
                    Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
                    Block.createCuboidShape(3.0, 11.0, 3.0, 13.0, 13.0, 13.0)
            ).reduce((voxelShape, voxelShape2) ->
                    VoxelShapes.combineAndSimplify(voxelShape, voxelShape2, BooleanBiFunction.OR))
            .orElseGet(VoxelShapes::empty);
    private static final VoxelShape TRIGGERED_SHAPE = Stream.of(
                    Block.createCuboidShape(2.0, 8.0, 2.0, 14.0, 11.0, 14.0),
                    Block.createCuboidShape(3.0, 3.0, 3.0, 13.0, 8.0, 13.0),
                    Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
                    Block.createCuboidShape(3.0, 11.0, 3.0, 13.0, 12.0, 13.0)
            ).reduce((voxelShape, voxelShape2) ->
                    VoxelShapes.combineAndSimplify(voxelShape, voxelShape2, BooleanBiFunction.OR))
            .orElseGet(VoxelShapes::empty);

    public SnapDetectorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(TRIGGERED, false)
                .with(POWER, 0)
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack stackInHand = player.getStackInHand(hand);

            if (stackInHand.isOf(Items.DIAMOND)) {
                stackInHand.decrement(1);

                world.setBlockState(pos, SnapStone.PERSONAL_DETECTOR.getDefaultState());
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if (blockEntity instanceof PersonalizedSnapDetectorBlockEntity detectorBlockEntity) {
                    detectorBlockEntity.setOwner(player);
                }

                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    public void trigger(ServerWorld world, BlockState state, BlockPos pos, Vec3d playerPos, ServerPlayerEntity player) {
        if (!world.getBlockTickScheduler().isScheduled(pos, this)) {
            world.playSoundFromEntity(null, player, SnapStone.SNAP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.setBlockState(pos,
                    state.with(TRIGGERED, true)
                            .with(POWER, this.calculatePower(playerPos, pos))
            );
            world.getBlockTickScheduler().schedule(pos, this, 30);
            world.updateNeighborsAlways(pos.down(), this);
        }
    }

    protected int calculatePower(Vec3d playerPos, BlockPos pos) {
        double distance = playerPos.distanceTo(Vec3d.ofCenter(pos));

        return (int) MathHelper.clamp(distance, 0, 15);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.with(TRIGGERED, false).with(POWER, 0));
        world.updateNeighborsAlways(pos.down(), this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(TRIGGERED) ? 15 : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? this.getWeakRedstonePower(state, world, pos, direction) : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return state.get(TRIGGERED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(POWER);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        world.updateNeighborsAlways(pos.down(), this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(TRIGGERED) ? TRIGGERED_SHAPE : NORMAL_SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TRIGGERED, POWER);
    }

}
