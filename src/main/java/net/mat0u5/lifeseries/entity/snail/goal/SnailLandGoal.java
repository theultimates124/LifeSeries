package net.mat0u5.lifeseries.entity.snail.goal;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class SnailLandGoal extends Goal {

    @NotNull
    private final Snail mob;
    private int noTargetTicks;

    public SnailLandGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (!mob.flying || mob.gliding) {
            return false;
        }

        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer == null) {
            noTargetTicks++;
        } else {
            noTargetTicks = 0;
        }

        if (noTargetTicks >= 40) {
            return true;
        }

        if (boundPlayer == null) {
            return false;
        }

        boolean isMobAboveTarget = mob.getY() - boundPlayer.getY() > 0.0D;

        if (!isMobAboveTarget) {
            return false;
        }

        double height = mob.getDistanceToGroundBlock();
        boolean shouldLand = height <= 1 + mob.getY() - boundPlayer.getY();

        return shouldLand && canPathfindFromGround();
    }

    @Override
    public boolean shouldContinue() {
        return mob.getDistanceToGroundBlock() > 1.5D;
    }

    @Override
    public void tick() {
        land();
    }

    @Override
    public void start() {
        OtherUtils.broadcastMessage(Text.of("test_SnailLandGoal"));
    }

    @Override
    public void stop() {
        mob.gliding = false;
        mob.flying = false;
        mob.updateNavigation();
        mob.updateMoveControl();
    }

    private void land() {
        OtherUtils.broadcastMessage(Text.of("landddd"));
        mob.setVelocity(0.0D, -0.1D, 0.0D);
    }

    private boolean canPathfindFromGround() {

        Vec3d originalPos = mob.getPos();
        BlockPos groundPos = mob.getGroundBlock();
        if (groundPos == null) {
            return false;
        }

        mob.setPosition(groundPos.getX(), groundPos.getY() + 1, groundPos.getZ());
        boolean canPathfind = mob.canPathToPlayer(false);
        mob.setPosition(originalPos.getX(), originalPos.getY(), originalPos.getZ());

        return canPathfind;
    }
}