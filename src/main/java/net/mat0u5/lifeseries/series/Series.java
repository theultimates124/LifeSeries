package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.TeamUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public abstract class Series extends Session {
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
    public Formatting getColorForLives(Integer lives) {
        if (lives == null) return Formatting.GRAY;
        if (lives == 1) return Formatting.DARK_RED;
        if (lives == 2) return Formatting.YELLOW;
        if (lives == 3) return Formatting.GREEN;
        if (lives >= 4) return Formatting.DARK_GREEN;
        return Formatting.DARK_GRAY;
    }
    public Text getFormattedLives(Integer lives) {
        if (lives == null) return Text.empty();
        Formatting color = getColorForLives(lives);
        return Text.literal(String.valueOf(lives)).formatted(color);
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
        addToPlayerLives(server,player,-1);
    }
    public void addPlayerLife(MinecraftServer server, ServerPlayerEntity player) {
        addToPlayerLives(server,player,1);
    }
    public void addToPlayerLives(MinecraftServer server, ServerPlayerEntity player, int amount) {
        Integer currentLives = getPlayerLives(player);
        if (currentLives == null) currentLives = 0;
        int lives = currentLives + amount;
        if (lives < 0) lives = 0;
        setPlayerLives(server,player,lives);
    }
    public void setPlayerLives(MinecraftServer server, ServerPlayerEntity player, int lives) {
        ScoreboardUtils.setScore(server, ScoreHolder.fromName(player.getNameForScoreboard()), "Lives", lives);
        if (lives == 0) {
            playerLostAllLives(player);
        }
        else {
            if (player.isSpectator()) {
                player.changeGameMode(GameMode.SURVIVAL);
            }
        }
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
