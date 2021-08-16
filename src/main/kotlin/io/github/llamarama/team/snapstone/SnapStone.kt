package io.github.llamarama.team.snapstone

import io.github.llamarama.team.snapstone.common.block.SnapDetectorBlock
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

internal val LOGGER: Logger = LogManager.getLogger("SnapStone")

const val MODID: String = "snapstone"

val SNAP_CHANNEL_ID = id("snap")

val SNAP_DETECTOR = SnapDetectorBlock(AbstractBlock.Settings.copy(Blocks.STONE))

@Suppress("unused")
fun init() {
    Registry.register(Registry.BLOCK, id("snap_detector"), SNAP_DETECTOR)
    Registry.register(
        Registry.ITEM, id("snap_detector"),
        BlockItem(SNAP_DETECTOR, Item.Settings().group(ItemGroup.REDSTONE))
    )

    ServerPlayNetworking.registerGlobalReceiver(SNAP_CHANNEL_ID) { server, player, _, buf, _ ->
        val x = buf.readDouble()
        val y = buf.readDouble()
        val z = buf.readDouble()

        server.execute {
            val playerPos = Vec3d(x, y, z)
            val world = player.serverWorld

            BlockPos.findClosest(BlockPos(playerPos), 7, 7) {
                world.getBlockState(it).isOf(SNAP_DETECTOR)
            }.ifPresent {
                val state = world.getBlockState(it)
                val block = state.block

                if (block is SnapDetectorBlock) {
                    block.trigger(world, state, it, playerPos)
                }
            }
        }
    }
}

fun getLogger() = LOGGER

fun id(id: String) = Identifier(MODID, id)