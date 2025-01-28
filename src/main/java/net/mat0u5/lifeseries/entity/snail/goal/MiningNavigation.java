package net.mat0u5.lifeseries.entity.snail.goal;

import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class MiningNavigation extends MobNavigation {

    public MiningNavigation(MobEntity mob, World world) {
        super(mob, world);
    }

    @Override
    protected boolean isAtValidPosition() {
        BlockPos targetPos = null;
        if (this.currentPath != null && !this.currentPath.isFinished()) {
            targetPos = Objects.requireNonNull(this.currentPath.getEnd()).getBlockPos();
        }

        if (targetPos != null && !this.world.getBlockState(targetPos).isAir()) {
            this.world.breakBlock(targetPos, true, this.entity);
        }

        return super.isAtValidPosition();
    }
}
