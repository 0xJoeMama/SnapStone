package io.github.llamarama.team.snapstone.client

import io.github.llamarama.team.snapstone.SNAP_CHANNEL_ID
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

val SNAP_KEY_BINDING = KeyBinding(
    "snapstone.snap.key",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_H,
    "snapstone.snap.category"
)


@Suppress("unused")
fun init() {
    KeyBindingHelper.registerKeyBinding(SNAP_KEY_BINDING)

    ClientTickEvents.END_CLIENT_TICK.register {
        if (SNAP_KEY_BINDING.wasPressed() && SNAP_KEY_BINDING.isPressed) {
            val buf = PacketByteBufs.create()
            val pos = it.player?.pos
            if (pos != null) {
                buf.writeDouble(pos.x)
                buf.writeDouble(pos.y)
                buf.writeDouble(pos.z)
            }

            ClientPlayNetworking.send(SNAP_CHANNEL_ID, buf)
        }
    }
}