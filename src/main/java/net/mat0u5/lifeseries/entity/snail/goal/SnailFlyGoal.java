package net.mat0u5.lifeseries.entity.snail.goal;


import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnailFlyGoal extends Goal {

    @NotNull
    private Snail mob;
    @Nullable
    private Path path;

    public SnailFlyGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @NotNull
    protected Snail getMob() {
        return this.mob;
    }

    @Override
    public boolean canStart() {
        if (!mob.flying || mob.gliding) {
            return false;
        }

        LivingEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer == null) {
            return false;
        }

        return getMob().canPathToPlayer(true);
    }

    @Override
    public boolean shouldContinue() {
        /*if (mob.isOnGround() && mob.flying) {
            mob.flying = false;
        }*/
        if (!mob.flying) return false;
        if (mob.getBoundPlayer() == null) return false;
        return true;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        mob.flying = false;
        mob.updateNavigation();
        mob.updateMoveControl();
    }
}