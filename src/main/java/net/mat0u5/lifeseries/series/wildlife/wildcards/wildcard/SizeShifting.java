package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class SizeShifting extends Wildcard {

    public static double MIN_SIZE_HARD = 0.06;
    public static double MAX_SIZE_HARD = 16;

    public static double MIN_SIZE = 0.25;
    public static double MAX_SIZE = 3;

    public static double SIZE_CHANGE_MULTIPLIER = 1;
    public static double SNEAK_STEP = -0.0015;
    public static double JUMP_STEP = 0.015;
    public static int JUMP_STEP_TIME = 5;
    private static HashMap<UUID, Integer> ticksAddingSize = new HashMap<>();

    @Override
    public Wildcards getType() {
        return Wildcards.SIZE_SHIFTING;
    }

    @Override
    public void tick() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (TriviaBot.cursedGigantificationPlayers.contains(player.getUuid())) return;
            if (player.isSpectator()) continue;
            if (player.isSneaking()) {
                addPlayerSize(player, SNEAK_STEP * SIZE_CHANGE_MULTIPLIER);
            }
            if (ticksAddingSize.containsKey(player.getUuid())) {
                addPlayerSize(player, (JUMP_STEP / JUMP_STEP_TIME) * SIZE_CHANGE_MULTIPLIER);
                int remaining = ticksAddingSize.get(player.getUuid());
                remaining--;
                if (remaining > 0) {
                    ticksAddingSize.put(player.getUuid(), remaining);
                }
                else {
                    ticksAddingSize.remove(player.getUuid());
                }
            }
        }
    }

    public void onJump(ServerPlayerEntity player) {
        if (!active) return;
        ticksAddingSize.put(player.getUuid(), JUMP_STEP_TIME);
    }

    public static float getPlayerSize(ServerPlayerEntity player) {
        return player.getScale();
    }

    public static void addPlayerSize(ServerPlayerEntity player, double amount) {
        setPlayerSize(player, getPlayerSize(player)+amount);
    }

    public static void setPlayerSize(ServerPlayerEntity player, double size) {
        if (size < MIN_SIZE_HARD) size = MIN_SIZE_HARD;
        if (size > MAX_SIZE_HARD) size = MAX_SIZE_HARD;
        if (size < MIN_SIZE) size = MIN_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;


        //? if <=1.21 {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_SCALE)).setBaseValue(size);
         //?} else {
        /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.SCALE)).setBaseValue(size);
        *///?}
    }
    public static void setPlayerSizeUnchecked(ServerPlayerEntity player, double size) {
        //? if <=1.21 {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_SCALE)).setBaseValue(size);
        //?} else {
        /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.SCALE)).setBaseValue(size);
         *///?}
    }

    public static void resetSizesTick(boolean isActive) {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (!isActive || (player.isSpectator() && !currentSeries.isAlive(player))) {
                float size = getPlayerSize(player);
                if (TriviaBot.cursedGigantificationPlayers.contains(player.getUuid())) return;
                if (size == 1) return;
                if (size < 0.98) {
                    addPlayerSize(player, 0.01);
                }
                else if (size > 1.02) {
                    addPlayerSize(player, -0.01);
                }
                else {
                    setPlayerSize(player, 1);
                }
            }
        }
    }
}
