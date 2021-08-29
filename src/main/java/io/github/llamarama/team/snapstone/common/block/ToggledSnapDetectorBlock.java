package io.github.llamarama.team.snapstone.common.block;

import io.github.llamarama.team.snapstone.SnapStone;
import io.github.llamarama.team.snapstone.common.block_entity.PersonalizedSnapDetectorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.Stream;

public class ToggledSnapDetectorBlock extends SnapDetectorBlock {

    private static final VoxelShape SHAPE = Stream.of(
                    Block.createCuboidShape(2.0, 8.0, 2.0, 14.0, 11.0, 14.0),
                    Block.createCuboidShape(3.0, 3.0, 3.0, 13.0, 8.0, 13.0),
                    Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0)
            ).reduce((voxelShape, voxelShape2) ->
                    VoxelShapes.combineAndSimplify(voxelShape, voxelShape2, BooleanBiFunction.OR))
            .orElseGet(VoxelShapes::empty);

    public ToggledSnapDetectorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void trigger(ServerWorld world, BlockState state, BlockPos pos, Vec3d playerPos, ServerPlayerEntity player) {
        if (!world.getBlockTickScheduler().isScheduled(pos, this)) {
            int oldPower = state.get(POWER);
            int newPower = oldPower == 0 ? this.calculatePower(playerPos, pos) : 0;

            world.playSoundFromEntity(null, player, SnapStone.SNAP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.setBlockState(pos, state.with(TRIGGERED, !state.get(TRIGGERED)).with(POWER, newPower));
            world.getBlockTickScheduler().schedule(pos, this, 35);
            world.updateNeighborsAlways(pos.down(), this);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);

        if (stackInHand.isOf(Items.DIAMOND)) {
            stackInHand.decrement(1);

            world.setBlockState(pos, SnapStone.PERSONAL_TOGGLED_DETECTOR.getDefaultState());
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof PersonalizedSnapDetectorBlockEntity detectorBlockEntity) {
                detectorBlockEntity.setOwner(player);
            }

            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

    }

}
