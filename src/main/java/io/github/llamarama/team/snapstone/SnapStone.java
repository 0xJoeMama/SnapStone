package io.github.llamarama.team.snapstone;

import io.github.llamarama.team.snapstone.common.block.PersonalSnapDetectorBlock;
import io.github.llamarama.team.snapstone.common.block.PersonalToggledSnapDetectorBlock;
import io.github.llamarama.team.snapstone.common.block_entity.PersonalizedSnapDetectorBlockEntity;
import io.github.llamarama.team.snapstone.common.block.SnapDetectorBlock;
import io.github.llamarama.team.snapstone.common.block.ToggledSnapDetectorBlock;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnapStone implements ModInitializer {

    public static final String MODID = "snapstone";
    public static final SoundEvent SNAP = new SoundEvent(id("snap"));
    public static final Identifier SNAP_CHANNEL_ID = id("snap");
    private static final Logger LOGGER = LogManager.getLogger("SnapStone");
    private static final AbstractBlock.Settings STONE_SETTINGS = AbstractBlock.Settings.copy(Blocks.STONE);
    // Blocks
    public static final Block SNAP_DETECTOR = new SnapDetectorBlock(STONE_SETTINGS);
    public static final Block TOGGLED_SNAP_DETECTOR = new ToggledSnapDetectorBlock(STONE_SETTINGS);
    public static final Block PERSONAL_DETECTOR = new PersonalSnapDetectorBlock(STONE_SETTINGS);
    public static final Block PERSONAL_TOGGLED_DETECTOR = new PersonalToggledSnapDetectorBlock(STONE_SETTINGS);

    // BEs
    public static final BlockEntityType<PersonalizedSnapDetectorBlockEntity> DETECTOR_BE =
            FabricBlockEntityTypeBuilder.create(PersonalizedSnapDetectorBlockEntity::new,
                            PERSONAL_DETECTOR,
                            PERSONAL_TOGGLED_DETECTOR)
                    .build();

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registry.SOUND_EVENT, id("snap"), SNAP);
        Registry.register(Registry.BLOCK, id("snap_detector"), SNAP_DETECTOR);
        Registry.register(
                Registry.ITEM, id("snap_detector"),
                new BlockItem(SNAP_DETECTOR, new Item.Settings().group(ItemGroup.REDSTONE))
        );

        Registry.register(Registry.BLOCK, id("toggled_snap_detector"), TOGGLED_SNAP_DETECTOR);
        Registry.register(
                Registry.ITEM, id("toggled_snap_detector"),
                new BlockItem(TOGGLED_SNAP_DETECTOR, new Item.Settings().group(ItemGroup.REDSTONE))
        );

        Registry.register(Registry.BLOCK_ENTITY_TYPE, id("snap_detector"), DETECTOR_BE);

        Registry.register(Registry.BLOCK, id("personal_snap_detector"), PERSONAL_DETECTOR);
        Registry.register(Registry.BLOCK, id("personal_toggled_snap_detector"), PERSONAL_TOGGLED_DETECTOR);

        ServerPlayNetworking.registerGlobalReceiver(SNAP_CHANNEL_ID, (server, player, handler, buf, responseSender) -> {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            Vec3d playerPos = new Vec3d(x, y, z);
            ServerWorld world = player.getWorld();

            server.execute(() -> BlockPos.streamOutwards(new BlockPos(playerPos), 15, 15, 15)
                    .filter(pos -> world.getBlockState(pos).getBlock() instanceof SnapDetectorBlock)
                    .forEach(pos -> {
                        BlockState state = world.getBlockState(pos);
                        ((SnapDetectorBlock) state.getBlock()).trigger(world, state, pos, playerPos, player);
                    }));
        });

        LOGGER.info("SnapStone is fully initialized!");
    }

}
