package net.mat0u5.lifeseries.series.limitedlife;

import net.mat0u5.lifeseries.series.*;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.TeamUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class LimitedLife extends Series {

    public BoogeymanManager boogeymanManager = new BoogeymanManager();
    public static int delayUntilRemove = 20;

    @Override
    public SeriesList getSeries() {
        return SeriesList.LIMITED_LIFE;
    }

    @Override
    public Blacklist createBlacklist() {
        return new LimitedLifeBlacklist();
    }

    @Override
    public void tickSessionOn() {
        super.tickSessionOn();
        delayUntilRemove--;
        if (delayUntilRemove <= 0) {
            delayUntilRemove = 20;
            secondPassed();
        }
    }
    public void secondPassed() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (!hasAssignedLives(player)) continue;
            if (!isAlive(player)) continue;
            removePlayerLife(player);
            player.sendMessage(getFormattedLives(getPlayerLives(player)), true);
        }
    }
    @Override
    public Formatting getColorForLives(Integer lives) {
        if (lives == null) return Formatting.GRAY;
        if (lives >= 57600) return Formatting.GREEN;
        if (lives >= 28800) return Formatting.YELLOW;
        if (lives >= 1) return Formatting.DARK_RED;
        return Formatting.DARK_GRAY;
    }
    @Override
    public Text getFormattedLives(Integer lives) {
        if (lives == null) return Text.empty();
        Formatting color = getColorForLives(lives);
        return Text.literal(OtherUtils.formatTime(lives*20)).formatted(color);
    }
    @Override
    public void reloadPlayerTeamActual(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (lives == null) TeamUtils.addPlayerToTeam("Unassigned",player);
        else if (lives <= 0) TeamUtils.addPlayerToTeam("Dead",player);
        else if (lives >= 57600) TeamUtils.addPlayerToTeam("Green",player);
        else if (lives >= 28800) TeamUtils.addPlayerToTeam("Yellow",player);
        else if (lives >= 1) TeamUtils.addPlayerToTeam("Red",player);
    }
    @Override
    public void setPlayerLives(ServerPlayerEntity player, int lives) {
        Formatting colorBefore = player.getScoreboardTeam().getColor();
        ScoreboardUtils.setScore(ScoreHolder.fromName(player.getNameForScoreboard()), "Lives", lives);
        if (lives <= 0) {
            playerLostAllLives(player);
        }
        Formatting colorNow = getColorForLives(lives);
        if (colorBefore != colorNow) {
            if (player.isSpectator() && lives > 0) {
                player.changeGameMode(GameMode.SURVIVAL);
            }
            reloadPlayerTeam(player);
        }
    }
    @Override
    public Boolean isOnLastLife(ServerPlayerEntity player) {
        if (!isAlive(player)) return null;
        Integer lives = currentSeries.getPlayerLives(player);
        return lives < 28800;
    }
    @Override
    public Boolean isOnSpecificLives(ServerPlayerEntity player, int check) {
        if (!isAlive(player)) return null;
        Integer lives = currentSeries.getPlayerLives(player);
        return lives == check;
    }
    @Override
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        super.onPlayerDeath(player, source);
    }
    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        Boogeyman boogeyman  = boogeymanManager.getBoogeyman(killer);
        if (boogeyman == null || boogeyman.cured) {
            if (isAllowedToAttack(killer, victim)) {
                addToPlayerLives(victim, -3600);
                PlayerUtils.sendTitle(victim, Text.literal("-1 hour").formatted(Formatting.RED), 20, 80, 20);
                addToPlayerLives(killer, 1800);
                PlayerUtils.sendTitle(killer, Text.literal("+30 minutes").formatted(Formatting.GREEN), 20, 80, 20);
                return;
            }
            OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "+killer.getNameForScoreboard() +
                    ", who is not §cred name§f, and is not a §cboogeyman§f!"));
            OtherUtils.broadcastMessageToAdmins(Text.of("No time has been removed from either of the players."));
            return;
        }
        boogeymanManager.cure(killer);

        //Victim was killed by boogeyman - remove 2 hours from victim and add extra 1 hour to boogey
        addToPlayerLives(victim, -7200);
        PlayerUtils.sendTitle(victim, Text.literal("-2 hours").formatted(Formatting.RED), 20, 80, 20);
        addToPlayerLives(killer, 3600);
        PlayerUtils.sendTitle(killer, Text.literal("+1 hour").formatted(Formatting.GREEN), 20, 80, 20);
    }
    @Override
    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (isOnLastLife(attacker, false)) return true;
        if (attacker.getPrimeAdversary() == victim && isOnLastLife(victim, true)) return true;
        if (isOnSpecificLives(attacker, 2) && isOnSpecificLives(victim, 3)) return true;
        if (attacker.getPrimeAdversary() == victim && (isOnSpecificLives(victim, 2) && isOnSpecificLives(attacker, 3))) return true;
        Boogeyman boogeymanAttacker = boogeymanManager.getBoogeyman(attacker);
        Boogeyman boogeymanVictim = boogeymanManager.getBoogeyman(victim);
        if (boogeymanAttacker != null && !boogeymanAttacker.cured) return true;
        if (attacker.getPrimeAdversary() == victim && (boogeymanVictim != null && !boogeymanVictim.cured)) return true;
        return false;
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        super.onPlayerJoin(player);
        boogeymanManager.onPlayerJoin(player);
        if (!hasAssignedLives(player)) {
            setPlayerLives(player,86400);
        }
    }
    @Override
    public void sessionStart() {
        super.sessionStart();
        boogeymanManager.resetBoogeymen();
        activeActions = List.of(
                boogeymanManager.actionBoogeymanWarn1,
                boogeymanManager.actionBoogeymanWarn2,
                boogeymanManager.actionBoogeymanChoose
        );
    }
    @Override
    public void sessionEnd() {
        super.sessionEnd();
        boogeymanManager.sessionEnd();
    }
    @Override
    public void playerLostAllLives(ServerPlayerEntity player) {
        super.playerLostAllLives(player);
        boogeymanManager.playerLostAllLives(player);
        List<ServerPlayerEntity> players = PlayerUtils.getAllPlayers();
        players.remove(player);
        PlayerUtils.sendTitle(player, Text.literal("You have run out of time!").formatted(Formatting.RED), 20, 160, 20);
        PlayerUtils.sendTitleWithSubtitleToPlayers(players, player.getStyledDisplayName(), Text.literal("ran out of time!"), 20, 80, 20);
        OtherUtils.broadcastMessage(Text.literal("").append(player.getStyledDisplayName()).append(Text.of(" ran out of time")));
    }

}
