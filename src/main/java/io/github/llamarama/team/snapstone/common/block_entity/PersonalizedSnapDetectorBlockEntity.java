package io.github.llamarama.team.snapstone.common.block_entity;

import io.github.llamarama.team.snapstone.SnapStone;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class PersonalizedSnapDetectorBlockEntity extends BlockEntity {

    public static final String OWNER_NBT = "Owner";
    private UUID owner;

    public PersonalizedSnapDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(SnapStone.DETECTOR_BE, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.owner = nbt.getUuid(OWNER_NBT);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putUuid(OWNER_NBT, this.owner);
        return super.writeNbt(nbt);
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner.getUuid();
        this.markDirty();
    }

}
