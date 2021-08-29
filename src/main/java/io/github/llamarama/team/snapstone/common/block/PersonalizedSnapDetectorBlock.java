package io.github.llamarama.team.snapstone.common.block;

import io.github.llamarama.team.snapstone.SnapStone;
import io.github.llamarama.team.snapstone.common.block_entity.PersonalizedSnapDetectorBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PersonalizedSnapDetectorBlock extends SnapDetectorBlock implements BlockEntityProvider {

    public PersonalizedSnapDetectorBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return SnapStone.DETECTOR_BE.instantiate(pos, state);
    }

    @Override
    public void trigger(ServerWorld world, BlockState state, BlockPos pos, Vec3d playerPos, ServerPlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof PersonalizedSnapDetectorBlockEntity detectorBlockEntity &&
                detectorBlockEntity.getOwner() == player.getUuid()) {
            super.trigger(world, state, pos, playerPos, player);
        }
    }

}
