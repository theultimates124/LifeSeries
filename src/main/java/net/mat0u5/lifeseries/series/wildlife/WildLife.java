package net.mat0u5.lifeseries.series.wildlife;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.MobSwap;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.SizeShifting;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PermissionManager;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.mat0u5.lifeseries.Main.seriesConfig;

public class WildLife extends Series {


    @Override
    public SeriesList getSeries() {
        return SeriesList.WILD_LIFE;
    }

    @Override
    public ConfigManager getConfig() {
        return new WildLifeConfig();
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        super.onPlayerJoin(player);

        if (!hasAssignedLives(player)) {
            int lives = seriesConfig.getOrCreateInt("default_lives", 6);
            setPlayerLives(player, lives);
        }
        TaskScheduler.scheduleTask(99, () -> {
            if (PermissionManager.isAdmin(player)) {
                player.sendMessage(Text.of("§7Wild Life commands: §r/lifeseries, /session, /claimkill, /lives, /wildcard"));
            }
            else {
                player.sendMessage(Text.of("§7Wild Life non-admin commands: §r/claimkill, /lives"));
            }
        });
        WildcardManager.resetWildcardsOnPlayerJoin(player);
    }

    @Override
    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (isOnLastLife(attacker, false)) return true;
        if (attacker.getPrimeAdversary() == victim && (isOnLastLife(victim, false))) return true;

        if (isOnSpecificLives(attacker, 2, false) && (isOnSpecificLives(victim, 3, false) || isOnSpecificLives(victim, 4, false))) return true;
        if (attacker.getPrimeAdversary() == victim && (isOnSpecificLives(victim, 2, false) && (isOnSpecificLives(attacker, 3, false) || isOnSpecificLives(attacker, 4, false)))) return true;
        return false;
    }

    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        boolean gaveLife = false;
        if (isOnAtLeastLives(victim, 4, false)) {
            addPlayerLife(killer);
            gaveLife = true;
        }
        if (isAllowedToAttack(killer, victim)) return;
        OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + "§7 was killed by §f"
                +killer.getNameForScoreboard() + "§7, who is not §cred name§7 (nor a §eyellow name§7, with the victim being a §agreen name§7)"));
        if (gaveLife) OtherUtils.broadcastMessageToAdmins(Text.of("§7Remember to remove a life from the killer (using §f/lives remove <player>§7) if this was indeed an unjustified kill."));
    }

    @Override
    public void tickSessionOn(MinecraftServer server) {
        super.tickSessionOn(server);
        WildcardManager.tickSessionOn();
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);
        WildcardManager.tick();
    }

    @Override
    public boolean sessionStart() {
        if (super.sessionStart()) {
            WildcardManager.onSessionStart();
            activeActions.addAll(
                    List.of(WildcardManager.wildcardNotice, WildcardManager.startWildcards)
            );
            return true;
        }
        return false;
    }

    @Override
    public void sessionEnd() {
        super.sessionEnd();
        WildcardManager.onSessionEnd();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void reload() {
        Hunger.SWITCH_DELAY = seriesConfig.getOrCreateInt("wildcard_hunger_randomize_interval", 36000);

        SizeShifting.MIN_SIZE = seriesConfig.getOrCreateDouble("wildcard_sizeshifting_min_size", 0.25);
        SizeShifting.MAX_SIZE = seriesConfig.getOrCreateDouble("wildcard_sizeshifting_max_size", 3);
        SizeShifting.SIZE_CHANGE_MULTIPLIER = seriesConfig.getOrCreateDouble("wildcard_sizeshifting_size_change_multiplier", 1);

        Snail.GLOBAL_SPEED_MULTIPLIER = seriesConfig.getOrCreateDouble("wildcard_snails_speed_multiplier", 1);

        MobSwap.MAX_DELAY = seriesConfig.getOrCreateInt("wildcard_mobswap_start_spawn_delay", 7200);
        MobSwap.MIN_DELAY = seriesConfig.getOrCreateInt("wildcard_mobswap_end_spawn_delay", 2400);
        MobSwap.SPAWN_MOBS = seriesConfig.getOrCreateInt("wildcard_mobswap_spawn_mobs", 250);
        MobSwap.BOSS_CHANCE_MULTIPLIER = seriesConfig.getOrCreateDouble("wildcard_mobswap_boss_chance_multiplier", 1);
    }
}
