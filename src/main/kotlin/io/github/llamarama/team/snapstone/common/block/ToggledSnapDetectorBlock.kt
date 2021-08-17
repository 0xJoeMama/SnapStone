package io.github.llamarama.team.snapstone.common.block

import io.github.llamarama.team.snapstone.SNAP
import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*

class ToggledSnapDetectorBlock(settings: Settings) : SnapDetectorBlock(settings) {

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
        }
    }

    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) = Unit

}