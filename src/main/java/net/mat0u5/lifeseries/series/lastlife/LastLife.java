package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class LastLife extends Series {
    List<SessionAction> activeActions = new ArrayList<>();

    public LastLifeLives livesManager = new LastLifeLives();
    public LastLifeBoogeymen boogeymanManager = new LastLifeBoogeymen();

    @Override
    public SeriesList getSeries() {
        return SeriesList.LAST_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return new LastLifeBlacklist();
    }
    @Override
    public void overrideTick() {
        System.out.println("test_"+activeActions.size());
        if (activeActions == null) return;
        if (activeActions.isEmpty()) return;
        List<SessionAction> remaining = new ArrayList<>();
        for (SessionAction action : activeActions) {
            boolean triggered = action.tick(passedTime);
            if (!triggered) {
                remaining.add(action);
            }
        }
        activeActions = remaining;
    }
    @Override
    public void sessionStart() {
        activeActions = List.of(
            livesManager.actionChooseLives,
            boogeymanManager.actionBoogeymanWarn1,
            boogeymanManager.actionBoogeymanWarn2,
            boogeymanManager.actionBoogeymanChoose
        );
    }
    @Override
    public void sessionEnd() {
        boogeymanManager.sessionEnd();
    }
    @Override
    public void playerLostAllLives(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SPECTATOR);
        boogeymanManager.playerLostAllLives(player);
    }
    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        Boogeyman boogeyman  = boogeymanManager.getBoogeyman(killer);
        if (boogeyman == null || boogeyman.cured) {
            if (isOnLastLife(killer)) return;
            OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "+killer.getNameForScoreboard() +
                    ", who is not §cred name§f, and is not a §cboogeyman§f!"));
            return;
        }
        boogeymanManager.cure(killer);
    }
    public List<ServerPlayerEntity> getNonRedPlayers() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllPlayers();
        if (players == null) return new ArrayList<>();
        if (players.isEmpty()) return new ArrayList<>();
        List<ServerPlayerEntity> nonRedPlayers = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            Boolean isOnLastLife = currentSeries.isOnLastLife(player);
            if (isOnLastLife == null) continue;
            if (isOnLastLife) continue;
            nonRedPlayers.add(player);
        }
        return nonRedPlayers;
    }
}
