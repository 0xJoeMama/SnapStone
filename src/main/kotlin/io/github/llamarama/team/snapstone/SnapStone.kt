@file:Suppress("unused")

package io.github.llamarama.team.snapstone

import io.github.llamarama.team.snapstone.common.block.SnapDetectorBlock
import io.github.llamarama.team.snapstone.common.block.ToggledSnapDetectorBlock
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

internal val LOGGER: Logger = LogManager.getLogger("SnapStone")

const val MODID: String = "snapstone"

val SNAP_CHANNEL_ID: Identifier = id("snap")

val SNAP_DETECTOR = SnapDetectorBlock(AbstractBlock.Settings.copy(Blocks.STONE))
val TOGGLED_SNAP_DETECTOR = ToggledSnapDetectorBlock(AbstractBlock.Settings.copy(Blocks.STONE))
val SNAP = SoundEvent(id("snap"))

fun init() {
    Registry.register(Registry.BLOCK, id("snap_detector"), SNAP_DETECTOR)
    Registry.register(
        Registry.ITEM, id("snap_detector"),
        BlockItem(SNAP_DETECTOR, Item.Settings().group(ItemGroup.REDSTONE))
    )

    Registry.register(Registry.BLOCK, id("toggled_snap_detector"), TOGGLED_SNAP_DETECTOR)
    Registry.register(
        Registry.ITEM, id("toggled_snap_detector"),
        BlockItem(TOGGLED_SNAP_DETECTOR, Item.Settings().group(ItemGroup.REDSTONE))
    )

    ServerPlayNetworking.registerGlobalReceiver(SNAP_CHANNEL_ID) { server, player, _, buf, _ ->
        val x = buf.readDouble()
        val y = buf.readDouble()
        val z = buf.readDouble()

        server.execute {
            val playerPos = Vec3d(x, y, z)
            val world = player.serverWorld

            BlockPos.streamOutwards(BlockPos(playerPos), 15, 15, 15).filter {
                world.getBlockState(it).block is SnapDetectorBlock
            }.forEach {
                val state = world.getBlockState(it)
                val block = state.block

                if (block is SnapDetectorBlock) {
                    block.trigger(world, state, it, playerPos, player)
                }
            }
        }
    }
}

fun getLogger() = LOGGER

fun id(id: String) = Identifier(MODID, id)