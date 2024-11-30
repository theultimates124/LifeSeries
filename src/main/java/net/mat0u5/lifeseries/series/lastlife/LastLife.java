package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.*;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.*;

public class LastLife extends Series {

    public LastLifeLivesManager livesManager = new LastLifeLivesManager();
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
        super.sessionStart();
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
        super.sessionEnd();
        boogeymanManager.sessionEnd();
    }
    @Override
    public void playerLostAllLives(ServerPlayerEntity player) {
        super.playerLostAllLives(player);
        boogeymanManager.playerLostAllLives(player);
    }
    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        Boogeyman boogeyman  = boogeymanManager.getBoogeyman(killer);
        if (boogeyman == null || boogeyman.cured) {
            if (isAllowedToAttack(killer, victim)) return;
            OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "+killer.getNameForScoreboard() +
                    ", who is not §cred name§f, and is not a §cboogeyman§f!"));
            return;
        }
        boogeymanManager.cure(killer);
    }
    @Override
    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (isOnLastLife(attacker, false)) return true;
        if (attacker.getPrimeAdversary() == victim && isOnLastLife(victim, false)) return true;
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
    }
}
