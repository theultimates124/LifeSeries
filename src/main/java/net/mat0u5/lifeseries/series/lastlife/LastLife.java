package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.utils.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.TeamUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class LastLife {
    public static void initialize(MinecraftServer server) {
        createTeams(server);
        createScoreboards(server);
    }
    public static void createTeams(MinecraftServer server) {
        TeamUtils.createTeam(server, "Dead", Formatting.DARK_GRAY);
        TeamUtils.createTeam(server, "Unassigned", Formatting.GRAY);

        TeamUtils.createTeam(server, "Red", Formatting.RED);
        TeamUtils.createTeam(server, "Yellow", Formatting.YELLOW);
        TeamUtils.createTeam(server, "Green", Formatting.GREEN);
        TeamUtils.createTeam(server, "DarkGreen", Formatting.DARK_GREEN);

    }
    public static void createScoreboards(MinecraftServer server) {
        ScoreboardUtils.createObjective(server, "Lives");
    }
    public static void reloadAllPlayerTeams(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            reloadPlayerTeam(server,player);
        }
    }
    public static void reloadPlayerTeam(MinecraftServer server, ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (lives == null) TeamUtils.addPlayerToTeam(server, "Unassigned",player);
        else if (lives == 0) TeamUtils.addPlayerToTeam(server, "Dead",player);
        else if (lives == 1) TeamUtils.addPlayerToTeam(server, "Red",player);
        else if (lives == 2) TeamUtils.addPlayerToTeam(server, "Yellow",player);
        else if (lives == 3) TeamUtils.addPlayerToTeam(server, "Green",player);
        else if (lives >= 4) TeamUtils.addPlayerToTeam(server, "DarkGreen",player);
    }
    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        removePlayerLife(player.getServer(), player);
    }
    public static Integer getPlayerLives(ServerPlayerEntity player) {
        return ScoreboardUtils.getScore(player.getServer(), ScoreHolder.fromName(player.getNameForScoreboard()), "Lives");
    }
    public static void removePlayerLife(MinecraftServer server, ServerPlayerEntity player) {
        Integer currentLives = getPlayerLives(player);
        if (currentLives != null) {
            int lives = currentLives-1;
            if (lives < 0) lives = 0;
            setPlayerLives(server,player,lives);
        }
    }
    public static void setPlayerLives(MinecraftServer server, ServerPlayerEntity player, int lives) {
        System.out.println("Setting " + player.getNameForScoreboard() + "'s lives to " + lives + ".");
        ScoreboardUtils.setScore(server, ScoreHolder.fromName(player.getNameForScoreboard()), "Lives", lives);
        reloadPlayerTeam(server, player);
    }
}
