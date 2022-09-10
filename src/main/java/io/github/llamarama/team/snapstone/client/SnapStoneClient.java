package io.github.llamarama.team.snapstone.client;

import io.github.llamarama.team.snapstone.SnapStone;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public class SnapStoneClient implements ClientModInitializer {

    public static final KeyBinding SNAP_KEY_BINDING =
            new KeyBinding("snapstone.snap.key",
                    InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_H,
                    "snapstone.snap.category");

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SNAP_KEY_BINDING);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!SNAP_KEY_BINDING.isPressed() && SNAP_KEY_BINDING.wasPressed()) {
                if (client.player != null) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    Vec3d playerPos = client.player.getPos();

                    buffer.writeDouble(playerPos.x);
                    buffer.writeDouble(playerPos.y);
                    buffer.writeDouble(playerPos.z);

                    ClientPlayNetworking.send(SnapStone.SNAP_CHANNEL_ID, buffer);
                }
            }
        });
    }

}
