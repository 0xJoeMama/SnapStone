package io.github.llamarama.team.snapstone.common.block

import io.github.llamarama.team.snapstone.SNAP
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.WallMountedBlock
import net.minecraft.block.enums.WallMountLocation
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
open class SnapDetectorBlock(settings: Settings) : WallMountedBlock(settings) {

    companion object {
        val TRIGGERED: BooleanProperty = BooleanProperty.of("triggered")
        val POWER: IntProperty = Properties.POWER

        val NORTH_FLOOR_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 2.0, 16.0),
            Block.createCuboidShape(6.0, 2.0, 2.0, 10.0, 4.0, 6.0),
            BooleanBiFunction.OR)
        val EAST_FLOOR_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 2.0, 12.0),
            Block.createCuboidShape(10.0, 2.0, 6.0, 14.0, 4.0, 10.0),
            BooleanBiFunction.OR)
        val SOUTH_FLOOR_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 2.0, 16.0),
            Block.createCuboidShape(6.0, 2.0, 10.0, 10.0, 4.0, 14.0),
            BooleanBiFunction.OR)
        val WEST_FLOOR_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 2.0, 12.0),
            Block.createCuboidShape(2.0, 2.0, 6.0, 6.0, 4.0, 10.0),
            BooleanBiFunction.OR)
        val SOUTH_WALL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 16.0, 2.0),
            Block.createCuboidShape(6.0, 10.0, 2.0, 10.0, 14.0, 4.0),
            BooleanBiFunction.OR)
        val WEST_WALL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(14.0, 0.0, 4.0, 16.0, 16.0, 12.0),
            Block.createCuboidShape(12.0, 10.0, 6.0, 14.0, 14.0, 10.0),
            BooleanBiFunction.OR)
        val NORTH_WALL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(4.0, 0.0, 14.0, 12.0, 16.0, 16.0),
            Block.createCuboidShape(6.0, 10.0, 12.0, 10.0, 14.0, 14.0),
            BooleanBiFunction.OR)
        val EAST_WALL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(0.0, 0.0, 4.0, 2.0, 16.0, 12.0),
            Block.createCuboidShape(2.0, 10.0, 6.0, 4.0, 14.0, 10.0),
            BooleanBiFunction.OR)
        val NORTH_CEIL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(4.0, 14.0, 0.0, 12.0, 16.0, 16.0),
            Block.createCuboidShape(6.0, 12.0, 2.0, 10.0, 14.0, 6.0),
            BooleanBiFunction.OR)
        val EAST_CEIL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(0.0, 14.0, 4.0, 16.0, 16.0, 12.0),
            Block.createCuboidShape(10.0, 12.0, 6.0, 14.0, 14.0, 10.0),
            BooleanBiFunction.OR)
        val SOUTH_CEIL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(4.0, 14.0, 0.0, 12.0, 16.0, 16.0),
            Block.createCuboidShape(6.0, 12.0, 10.0, 10.0, 14.0, 14.0),
            BooleanBiFunction.OR)
        val WEST_CEIL_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(
            Block.createCuboidShape(0.0, 14.0, 4.0, 16.0, 16.0, 12.0),
            Block.createCuboidShape(2.0, 12.0, 6.0, 6.0, 14.0, 10.0),
            BooleanBiFunction.OR)
    }

    init {
        this.defaultState = this.stateManager.defaultState.with(TRIGGERED, false).with(POWER, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(TRIGGERED, POWER, FACE, FACING)
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?,
    ): VoxelShape {
        val facingDirection: Direction = state?.get(FACING) ?: Direction.NORTH

        return when (state?.get(FACE) ?: WallMountLocation.FLOOR) {
            WallMountLocation.FLOOR -> when (facingDirection) {
                Direction.NORTH -> NORTH_FLOOR_SHAPE
                Direction.EAST -> EAST_FLOOR_SHAPE
                Direction.SOUTH -> SOUTH_FLOOR_SHAPE
                Direction.WEST -> WEST_FLOOR_SHAPE
                else -> NORTH_FLOOR_SHAPE
            }
            WallMountLocation.WALL -> when (facingDirection) {
                Direction.NORTH -> NORTH_WALL_SHAPE
                Direction.SOUTH -> SOUTH_WALL_SHAPE
                Direction.WEST -> WEST_WALL_SHAPE
                Direction.EAST -> EAST_WALL_SHAPE
                else -> NORTH_WALL_SHAPE
            }
            WallMountLocation.CEILING -> when (facingDirection) {
                Direction.NORTH -> NORTH_CEIL_SHAPE
                Direction.SOUTH -> SOUTH_CEIL_SHAPE
                Direction.WEST -> WEST_CEIL_SHAPE
                Direction.EAST -> EAST_CEIL_SHAPE
                else -> NORTH_CEIL_SHAPE
            }
        }
    }

    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        world?.setBlockState(pos, state?.with(TRIGGERED, false)?.with(POWER, 0))
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
            world.setBlockState(pos, state.with(TRIGGERED, true).with(POWER, calculatePower(playerPos, pos)))
            world.blockTickScheduler.schedule(pos, this, 30)
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

    override fun emitsRedstonePower(state: BlockState?): Boolean = state?.get(TRIGGERED) ?: false


    override fun getWeakRedstonePower(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        direction: Direction?,
    ): Int = when {
        state?.get(TRIGGERED) == true -> 15
        else -> 0
    }

    override fun getStrongRedstonePower(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        direction: Direction?,
    ): Int {
        val resolvedDirection: Direction = this.decodeDirection(state ?: this.defaultState)

        return if (resolvedDirection == direction) this.getWeakRedstonePower(state, world, pos, direction) else 0
    }

    open fun decodeDirection(state: BlockState): Direction {
        return when (state.get(FACE)) {
            WallMountLocation.FLOOR -> Direction.DOWN
            WallMountLocation.CEILING -> Direction.UP
            else -> state.get(FACING)
        }
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean = state?.get(TRIGGERED) ?: false


    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?): Int = state?.get(POWER) ?: 0

}