package net.mat0u5.lifeseries.utils;

import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.server;

public class ScoreboardUtils {

    public static ScoreboardObjective createObjective(String name) {
        return createObjective(name, name, ScoreboardCriterion.DUMMY);
    }
    public static ScoreboardObjective createObjective(String name, String displayName, ScoreboardCriterion criterion) {
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getNullableObjective(name) != null) return null;
        return scoreboard.addObjective(name, criterion, Text.literal(displayName), criterion.getDefaultRenderType(), false, null);
    }

    public static boolean removeObjective(String name) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(name);
        if (objective == null) return false;
        scoreboard.removeObjective(objective);
        return true;
    }

    public static boolean setScore(ScoreHolder holder, String objectiveName, int score) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective == null) return false;
        scoreboard.getOrCreateScore(holder, objective).setScore(score);
        return true;
    }

    public static Integer getScore(ScoreHolder holder, String objectiveName) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective == null) return -1;
        ReadableScoreboardScore score = scoreboard.getScore(holder, objective);
        if (score == null) return null;
        return score.getScore();
    }

    public static boolean resetScore(ScoreHolder holder, String objectiveName) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective == null) return false;
        scoreboard.removeScore(holder, objective);
        return true;
    }
}
