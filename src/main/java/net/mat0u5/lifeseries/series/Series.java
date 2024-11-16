package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.TeamUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public abstract class Series {
    public abstract SeriesList getSeries();
    public void initialize(MinecraftServer server) {
        createTeams(server);
        createScoreboards(server);
    }
    public void createTeams(MinecraftServer server) {
        TeamUtils.createTeam(server, "Dead", Formatting.DARK_GRAY);
        TeamUtils.createTeam(server, "Unassigned", Formatting.GRAY);

        TeamUtils.createTeam(server, "Red", Formatting.DARK_RED);
        TeamUtils.createTeam(server, "Yellow", Formatting.YELLOW);
        TeamUtils.createTeam(server, "Green", Formatting.GREEN);
        TeamUtils.createTeam(server, "DarkGreen", Formatting.DARK_GREEN);

    }
    public void createScoreboards(MinecraftServer server) {
        ScoreboardUtils.createObjective(server, "Lives");
    }
    public void reloadAllPlayerTeams(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            reloadPlayerTeam(server,player);
        }
    }
    public void reloadPlayerTeam(MinecraftServer server, ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (lives == null) TeamUtils.addPlayerToTeam(server, "Unassigned",player);
        else if (lives == 0) TeamUtils.addPlayerToTeam(server, "Dead",player);
        else if (lives == 1) TeamUtils.addPlayerToTeam(server, "Red",player);
        else if (lives == 2) TeamUtils.addPlayerToTeam(server, "Yellow",player);
        else if (lives == 3) TeamUtils.addPlayerToTeam(server, "Green",player);
        else if (lives >= 4) TeamUtils.addPlayerToTeam(server, "DarkGreen",player);
    }
    public Integer getPlayerLives(ServerPlayerEntity player) {
        return ScoreboardUtils.getScore(player.getServer(), ScoreHolder.fromName(player.getNameForScoreboard()), "Lives");
    }
    public boolean isAlive(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (lives == null) return false;
        return lives > 0;
    }
    public void removePlayerLife(MinecraftServer server, ServerPlayerEntity player) {
        Integer currentLives = getPlayerLives(player);
        if (currentLives != null) {
            int lives = currentLives-1;
            if (lives < 0) lives = 0;
            if (lives == 0) {
                playerLostAllLives(player);
            }
            setPlayerLives(server,player,lives);
        }
    }
    public void addPlayerLife(MinecraftServer server, ServerPlayerEntity player) {
        Integer currentLives = getPlayerLives(player);
        if (currentLives != null) {
            int lives = currentLives+1;
            setPlayerLives(server,player,lives);
        }
    }
    public void setPlayerLives(MinecraftServer server, ServerPlayerEntity player, int lives) {
        ScoreboardUtils.setScore(server, ScoreHolder.fromName(player.getNameForScoreboard()), "Lives", lives);
        reloadPlayerTeam(server, player);
    }
    public void playerLostAllLives(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SPECTATOR);
    }

    /*
        Events
     */
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        removePlayerLife(player.getServer(), player);
    }
    public void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
        reloadPlayerTeam(server,player);
    }
}
