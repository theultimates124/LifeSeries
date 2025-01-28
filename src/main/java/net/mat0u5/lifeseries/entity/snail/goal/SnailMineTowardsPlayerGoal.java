package net.mat0u5.lifeseries.entity.snail.goal;


import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public final class SnailMineTowardsPlayerGoal extends SnailFlyGoal {

    public SnailMineTowardsPlayerGoal(@NotNull Snail mob) {
        super(mob);
    }

    @Override
    public boolean canStart() {
        if (!getMob().flying) {
            return false;
        }

        if (getMob().getBoundPlayer() == null) {
            return false;
        }

        if (getMob().getNavigation().getCurrentPath() == null) {
            return false;
        }

        if (!getMob().getNavigation().getCurrentPath().isFinished()) {
            return false;
        }

        boolean canFlyToPlayer = getMob().canPathToPlayer(true) || getMob().isInAttackRange(getMob().getBoundPlayer());

        return !canFlyToPlayer && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        if (getMob().getBoundPlayer() == null) {
            return false;
        }

        boolean canFlyToPlayer = getMob().canPathToPlayer(true) || getMob().isInAttackRange(getMob().getBoundPlayer());

        return !canFlyToPlayer && super.shouldContinue();
    }

    @Override
    public void start() {
        OtherUtils.broadcastMessage(Text.of("test_SnailMineTowardsPlayerGoal"));
        super.start();
        getMob().mining = true;
        getMob().updateNavigation();
    }

    @Override
    public void stop() {
        super.stop();
        getMob().mining = false;
        getMob().updateNavigation();
    }
}