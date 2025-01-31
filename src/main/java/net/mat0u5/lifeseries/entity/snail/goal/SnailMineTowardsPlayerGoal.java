package net.mat0u5.lifeseries.entity.snail.goal;


import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public final class SnailMineTowardsPlayerGoal extends SnailFlyGoal {

    public SnailMineTowardsPlayerGoal(@NotNull Snail mob) {
        super(mob);
    }

    @Override
    public boolean canStart() {

        if (getMob().getBoundPlayer() == null) {
            return false;
        }

        if (getMob().getNavigation().getCurrentPath() == null) {
            return false;
        }

        if (!getMob().getNavigation().getCurrentPath().isFinished()) {
            return false;
        }

        boolean canWalk = getMob().canPathToPlayer(false);
        boolean canFly = getMob().canPathToPlayer(true);

        return !canWalk && !canFly;
    }

    @Override
    public boolean shouldContinue() {
        if (getMob().getBoundPlayer() == null) {
            return false;
        }

        boolean canWalk = getMob().canPathToPlayer(false);
        boolean canFly = getMob().canPathToPlayer(true);

        return !canWalk && !canFly;
    }

    @Override
    public void start() {
        OtherUtils.broadcastMessage(Text.of("test_SnailMineTowardsPlayerGoal"));
        getMob().mining = true;
        getMob().updateNavigation();
    }

    @Override
    public void stop() {
        OtherUtils.broadcastMessage(Text.of("test_STOPP_SnailMineTowardsPlayerGoal"));
        getMob().mining = false;
        getMob().flying = true;
        getMob().updateNavigation();
    }
}