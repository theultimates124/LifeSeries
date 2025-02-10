package net.mat0u5.lifeseries.entity.triviabot.goal;

import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public final class TriviaBotTeleportGoal extends Goal {
    @NotNull
    private final TriviaBot mob;
    private int teleportCooldown = 0;

    public TriviaBotTeleportGoal(@NotNull TriviaBot mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (teleportCooldown > 0) {
            teleportCooldown--;
            return false;
        }
        if (mob.getBoundPlayer() == null) {
            return false;
        }
        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        float distFromPlayer = mob.distanceTo(boundPlayer);
        boolean dimensionsAreSame = mob.getWorld().getRegistryKey().equals(boundPlayer.getWorld().getRegistryKey());
        return !dimensionsAreSame || distFromPlayer > TriviaBot.MAX_DISTANCE;
    }

    @Override
    public void start() {
        teleportCooldown = 20;
        mob.teleportNearPlayer(TriviaBot.TP_MIN_RANGE);
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }
}