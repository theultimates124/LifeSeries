package net.mat0u5.lifeseries.entity.snail.goal;


import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class SnailTeleportGoal extends Goal {


    @NotNull
    private final Snail mob;
    private int ticksSinceLastPositionChange;
    private final int maxTicksSinceLastPositionChange;
    @NotNull
    private BlockPos lastPosition;

    public SnailTeleportGoal(@NotNull Snail mob) {
        this.mob = mob;
        this.maxTicksSinceLastPositionChange = Snail.STATIONARY_TP_COOLDOWN;
        this.lastPosition = BlockPos.ORIGIN;
    }

    @Override
    public boolean canStart() {
        if (!mob.getBlockPos().equals(this.lastPosition)) {
            this.ticksSinceLastPositionChange = 0;
            this.lastPosition = mob.getBlockPos();
        }

        this.ticksSinceLastPositionChange++;

        if (mob.getBoundPlayer() == null) {
            return false;
        }

        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        float distFromPlayer = mob.distanceTo(boundPlayer);
        boolean dimensionsAreSame = mob.getWorld().getRegistryKey().equals(boundPlayer.getWorld().getRegistryKey());
        return !dimensionsAreSame || distFromPlayer > Snail.MAX_DISTANCE || this.ticksSinceLastPositionChange > this.maxTicksSinceLastPositionChange;
    }

    @Override
    public void start() {
        OtherUtils.broadcastMessage(Text.of("test_SnailTeleportNearTargetGoal"));
        mob.teleportNearPlayer(Snail.TP_MIN_RANGE);
    }

    @Override
    public void tick() {
        start();
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }
}