package net.mat0u5.lifeseries.entity.triviabot.goal;

import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class TriviaBotLookAtPlayerGoal extends Goal {
    protected final TriviaBot mob;
    @Nullable
    protected ServerPlayerEntity target;
    protected final double rangeSquared = 400;
    private int lookTime;

    public TriviaBotLookAtPlayerGoal(TriviaBot mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.LOOK));
    }

    public boolean canStart() {
        if (!mob.interactedWith) return false;

        target = mob.getBoundPlayer();
        if (target == null) return false;

        return this.mob.squaredDistanceTo(this.target) <= rangeSquared;
    }

    public boolean shouldContinue() {
        if (this.target == null) return false;
        if (!this.target.isAlive()) return false;
        if (this.mob.squaredDistanceTo(this.target) > rangeSquared) return false;
        return this.lookTime > 0;
    }

    public void start() {
        this.lookTime = this.getTickCount(40 + this.mob.getRandom().nextInt(40));
    }

    public void stop() {
        this.target = null;
    }

    public void tick() {
        if (this.target != null && this.target.isAlive()) {
            double d = this.mob.getEyeY();
            this.mob.getLookControl().lookAt(this.target.getX(), d, this.target.getZ());
            --this.lookTime;
        }
    }
}
