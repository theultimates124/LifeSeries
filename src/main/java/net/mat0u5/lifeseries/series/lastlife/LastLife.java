package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.*;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class LastLife extends Series {

    public LastLifeLives livesManager = new LastLifeLives();
    public BoogeymanManager boogeymanManager = new BoogeymanManager();

    @Override
    public SeriesList getSeries() {
        return SeriesList.LAST_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return new LastLifeBlacklist();
    }
    @Override
    public void sessionStart() {
        boogeymanManager.resetBoogeymen();
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
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER);
        boogeymanManager.playerLostAllLives(player);
    }
    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        Boogeyman boogeyman  = boogeymanManager.getBoogeyman(killer);
        if (boogeyman == null || boogeyman.cured) {
            if (isOnLastLife(killer)) return;
            if (killer.getPrimeAdversary() == victim && (isOnLastLife(victim) || boogeymanManager.isBoogeyman(victim))) return;
            OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "+killer.getNameForScoreboard() +
                    ", who is not §cred name§f, and is not a §cboogeyman§f!"));
            return;
        }
        boogeymanManager.cure(killer);
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        reloadPlayerTeam(player);
        boogeymanManager.onPlayerJoin(player);
    }
}
