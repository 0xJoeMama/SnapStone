package io.github.llamarama.team.snapstone.common.block

import io.github.llamarama.team.snapstone.getLogger
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class SnapDetectorBlock(settings: Settings) : Block(settings) {

    companion object {
        val TRIGGERED: BooleanProperty = BooleanProperty.of("triggered")
        val POWER: IntProperty = Properties.POWER
    }

    init {
        this.defaultState = this.stateManager.defaultState.with(TRIGGERED, false).with(POWER, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(TRIGGERED, POWER)
    }

    @Suppress("DEPRECATION")
    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        super.scheduledTick(state, world, pos, random)
        world?.setBlockState(pos, state?.with(TRIGGERED, false)?.with(POWER, 0))
    }

    fun trigger(world: ServerWorld, state: BlockState, pos: BlockPos, playerPos: Vec3d) {
        if (!world.blockTickScheduler.isScheduled(pos, this)) {
            world.setBlockState(pos,
                state.with(TRIGGERED, true).with(POWER, calculatePower(playerPos, pos)))
            world.blockTickScheduler.schedule(pos, this, 60)
        }
    }

    private fun calculatePower(playerPos: Vec3d, blockPos: BlockPos): Int {
        val distance = playerPos.distanceTo(Vec3d.ofCenter(blockPos))

        getLogger().info(distance)
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
    ): Int {
        return when {
            state?.get(TRIGGERED) == true -> 15
            else -> 0
        }
    }


    override fun hasComparatorOutput(state: BlockState?): Boolean = state?.get(TRIGGERED) == true


    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?): Int = state?.get(POWER) ?: 0

}