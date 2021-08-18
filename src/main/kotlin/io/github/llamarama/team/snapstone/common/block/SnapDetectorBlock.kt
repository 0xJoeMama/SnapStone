package io.github.llamarama.team.snapstone.common.block

import io.github.llamarama.team.snapstone.SNAP
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*


@Suppress("DEPRECATION")
open class SnapDetectorBlock(settings: Settings) : Block(settings) {

    companion object {
        val TRIGGERED: BooleanProperty = BooleanProperty.of("triggered")
        val POWER: IntProperty = Properties.POWER
        private val SHAPE_NORMAL: VoxelShape = listOf(
            createCuboidShape(2.0, 8.0, 2.0, 14.0, 11.0, 14.0),
            createCuboidShape(3.0, 3.0, 3.0, 13.0, 8.0, 13.0),
            createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
            createCuboidShape(3.0, 11.0, 3.0, 13.0, 13.0, 13.0)
        ).reduce { shape1, shape2 -> VoxelShapes.combineAndSimplify(shape1, shape2, BooleanBiFunction.OR) }
        private val SHAPE_TRIGGERED: VoxelShape = listOf(
            createCuboidShape(2.0, 8.0, 2.0, 14.0, 11.0, 14.0),
            createCuboidShape(3.0, 3.0, 3.0, 13.0, 8.0, 13.0),
            createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
            createCuboidShape(3.0, 11.0, 3.0, 13.0, 12.0, 13.0)
        ).reduce { shape1, shape2 -> VoxelShapes.combineAndSimplify(shape1, shape2, BooleanBiFunction.OR) }
    }

    init {
        this.defaultState = this.stateManager.defaultState
            .with(TRIGGERED, false)
            .with(POWER, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(TRIGGERED, POWER)
    }

    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        world?.setBlockState(pos, state?.with(TRIGGERED, false)?.with(POWER, 0))
        world?.updateNeighborsAlways(pos?.down(), state?.block)
    }

    open fun trigger(
        world: ServerWorld,
        state: BlockState,
        pos: BlockPos,
        playerPos: Vec3d,
        player: ServerPlayerEntity,
    ) {
        if (!world.blockTickScheduler.isScheduled(pos, this)) {
            world.playSoundFromEntity(null, player, SNAP, SoundCategory.PLAYERS, 1.0f, 1.0f)
            world.setBlockState(pos,
                state.with(TRIGGERED, true)
                    .with(POWER, calculatePower(playerPos, pos)),
                NOTIFY_ALL)
            world.blockTickScheduler.schedule(pos, this, 30)
            world.updateNeighborsAlways(pos.down(), state.block)
        }
    }

    protected fun calculatePower(playerPos: Vec3d, blockPos: BlockPos): Int {
        val distance = playerPos.distanceTo(Vec3d.ofCenter(blockPos))

        return when {
            distance >= 15 -> 15
            distance < 15 && distance > 1 -> distance.toInt()
            distance <= 1 -> 1
            else -> 0
        }
    }

    override fun emitsRedstonePower(state: BlockState?): Boolean = true


    override fun getWeakRedstonePower(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        direction: Direction?,
    ): Int = if (state?.get(TRIGGERED) == true) 15 else 0

    override fun getStrongRedstonePower(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        direction: Direction?,
    ): Int = if (state?.get(TRIGGERED) == true and (direction == Direction.UP)) 15 else 0

    override fun hasComparatorOutput(state: BlockState?): Boolean = state?.get(TRIGGERED) ?: false

    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?): Int = state?.get(POWER) ?: 0

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?,
    ): VoxelShape {
        return if (state?.get(TRIGGERED) == true) SHAPE_TRIGGERED else SHAPE_NORMAL
    }

    override fun onStateReplaced(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        newState: BlockState?,
        moved: Boolean,
    ) {
        super.onStateReplaced(state, world, pos, newState, moved)
        world?.updateNeighborsAlways(pos?.down(), state?.block)
    }
}