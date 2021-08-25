package io.github.llamarama.team.snapstone.common.block

import io.github.llamarama.team.snapstone.SNAP
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.*


class ToggledSnapDetectorBlock(settings: Settings) : SnapDetectorBlock(settings) {

    companion object {
        private val SHAPE: VoxelShape = listOf(
            createCuboidShape(2.0, 8.0, 2.0, 14.0, 11.0, 14.0),
            createCuboidShape(3.0, 3.0, 3.0, 13.0, 8.0, 13.0),
            createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0)
        ).reduce { shape1, shape2 -> VoxelShapes.combineAndSimplify(shape1, shape2, BooleanBiFunction.OR) }
    }

    override fun trigger(
        world: ServerWorld, state: BlockState, pos: BlockPos, playerPos: Vec3d, player: ServerPlayerEntity,
    ) {
        if (!world.blockTickScheduler.isScheduled(pos, this)) {
            val oldPower = state.get(POWER)
            val newPower = if (oldPower == 0) this.calculatePower(playerPos, pos) else 0
            world.playSoundFromEntity(null, player, SNAP, SoundCategory.PLAYERS, 1.0f, 1.0f)
            world.setBlockState(pos,
                state.with(TRIGGERED, !state.get(TRIGGERED)).with(POWER, newPower))
            world.blockTickScheduler.schedule(pos, this, 35)
            world.updateNeighborsAlways(pos.down(), state.block)
        }
    }

    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) = Unit

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = SHAPE
}