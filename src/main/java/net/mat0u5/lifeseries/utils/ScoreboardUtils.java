package net.mat0u5.lifeseries.utils;

import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.server;

public class ScoreboardUtils {

    public static void createObjective(String name) {
        createObjective(name, name, ScoreboardCriterion.DUMMY);
    }

    public static ScoreboardObjective createObjective(String name, String displayName, ScoreboardCriterion criterion) {
        if (server == null) return null;
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getNullableObjective(name) != null) return null;
        return scoreboard.addObjective(name, criterion, Text.literal(displayName), criterion.getDefaultRenderType(), false, null);
    }

    public static void removeObjective(String name) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(name);
        if (objective == null) return;
        scoreboard.removeObjective(objective);
    }

    public static void setScore(ScoreHolder holder, String objectiveName, int score) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective == null) return;
        scoreboard.getOrCreateScore(holder, objective).setScore(score);
    }

    public static Integer getScore(ScoreHolder holder, String objectiveName) {
        if (server == null) return null;
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective == null) return -1;
        ReadableScoreboardScore score = scoreboard.getScore(holder, objective);
        if (score == null) return null;
        return score.getScore();
    }

    public static void resetScore(ScoreHolder holder, String objectiveName) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective == null) return;
        scoreboard.removeScore(holder, objective);
    }
}
