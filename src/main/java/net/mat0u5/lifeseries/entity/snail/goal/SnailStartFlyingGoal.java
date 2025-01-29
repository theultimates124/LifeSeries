package net.mat0u5.lifeseries.entity.snail.goal;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public final class SnailStartFlyingGoal extends Goal {

    @NotNull
    private final Snail mob;
    private int startFlyingCounter;
    private final int startFlyingDelay = 70;
    private boolean canWalk = true;

    public SnailStartFlyingGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (mob.getBoundPlayer() == null) {
            return false;
        }

        if (mob.flying) {
            return false;
        }

        /*
        if (!mob.isTargetOnGround()) {
            return false;
        }*/

        if (mob.getNavigation().getCurrentPath() == null) {
            return false;
        }

        canWalk = mob.canPathToPlayer(false);

        if (canWalk) {
            startFlyingCounter = 0;
        }
        else {
            startFlyingCounter++;
        }

        return startFlyingCounter >= startFlyingDelay;
    }

    @Override
    public void start() {
        OtherUtils.broadcastMessage(Text.of("test_SnailStartFlyingGoal"));
        mob.flying = true;
        mob.updateNavigation();
        mob.updateMoveControl();
    }

    @Override
    public void stop() {
        startFlyingCounter = 0;
        canWalk = true;
    }
}