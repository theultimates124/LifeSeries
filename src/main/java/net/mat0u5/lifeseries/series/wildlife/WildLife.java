package net.mat0u5.lifeseries.series.wildlife;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.*;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.TriviaWildcard;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PermissionManager;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

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

        NetworkHandlerServer.sendHandshake(player);
        NetworkHandlerServer.sendUpdatePacketTo(player);

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

        if (isOnSpecificLives(attacker, 2, false) && isOnAtLeastLives(victim, 4, false)) return true;
        if (attacker.getPrimeAdversary() == victim && isOnSpecificLives(victim, 2, false) && isOnAtLeastLives(attacker, 4, false)) return true;
        return false;
    }

    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        boolean gaveLife = false;
        boolean isAllowedToAttack = isAllowedToAttack(killer, victim);
        if (isOnAtLeastLives(victim, 4, false)) {
            addPlayerLife(killer);
            gaveLife = true;
        }
        if (isAllowedToAttack) return;
        OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + "§7 was killed by §f"
                +killer.getNameForScoreboard() + "§7, who is not §cred name§7 (nor a §eyellow name§7, with the victim being a §2dark green name§7)"));
        if (gaveLife) OtherUtils.broadcastMessageToAdmins(Text.of("§7Remember to remove a life from the killer (using §f/lives remove <player>§7) if this was indeed an unjustified kill."));
    }


    @Override
    public void onClaimKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        super.onClaimKill(killer, victim);
        if (isOnAtLeastLives(victim, 3, false)) {
            addPlayerLife(killer);
        }
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
        WildcardManager.onSessionEnd();
        super.sessionEnd();
    }

    @Override
    public void initialize() {
        super.initialize();
        Snails.loadConfig();
        Snails.loadSnailNames();
        TriviaBot.initializeItemSpawner();
    }

    @Override
    public void reload() {
        Hunger.SWITCH_DELAY = seriesConfig.getOrCreateInt("wildcard_hunger_randomize_interval", 36000);

        SizeShifting.MIN_SIZE = seriesConfig.getOrCreateDouble("wildcard_sizeshifting_min_size", 0.25);
        SizeShifting.MAX_SIZE = seriesConfig.getOrCreateDouble("wildcard_sizeshifting_max_size", 3);
        SizeShifting.SIZE_CHANGE_MULTIPLIER = seriesConfig.getOrCreateDouble("wildcard_sizeshifting_size_change_multiplier", 1);


        Snail.GLOBAL_SPEED_MULTIPLIER = seriesConfig.getOrCreateDouble("wildcard_snails_speed_multiplier", 1);
        Snail.SHOULD_DROWN_PLAYER = seriesConfig.getOrCreateBoolean("wildcard_snails_drown_players", true);

        TimeDilation.MIN_PLAYER_MSPT = (float) (1000.0 / seriesConfig.getOrCreateDouble("wildcard_timedilation_max_player_tps", 40));

        MobSwap.MAX_DELAY = seriesConfig.getOrCreateInt("wildcard_mobswap_start_spawn_delay", 7200);
        MobSwap.MIN_DELAY = seriesConfig.getOrCreateInt("wildcard_mobswap_end_spawn_delay", 2400);
        MobSwap.SPAWN_MOBS = seriesConfig.getOrCreateInt("wildcard_mobswap_spawn_mobs", 250);
        MobSwap.BOSS_CHANCE_MULTIPLIER = seriesConfig.getOrCreateDouble("wildcard_mobswap_boss_chance_multiplier", 1);

        TriviaBot.CAN_START_RIDING = seriesConfig.getOrCreateBoolean("wildcard_triviabot_can_enter_boats", true);
        TriviaWildcard.TRIVIA_BOTS_PER_PLAYER = seriesConfig.getOrCreateInt("wildcard_triviabot_bots_per_player", 5);

        Snails.loadConfig();
        Snails.loadSnailNames();
        Snails.reloadSnailNames();
        TriviaWildcard.reload();
    }

    @Override
    public void modifyMobDrops(LivingEntity entity, DamageSource damageSource) {
        super.modifyMobDrops(entity, damageSource);
        if (damageSource.getSource() instanceof PlayerEntity) {
            if (entity instanceof WardenEntity || entity instanceof WitherEntity) {

                //? if <= 1.21 {
                entity.dropStack(Items.TOTEM_OF_UNDYING.getDefaultStack());
                 //?} else {
                /*entity.dropStack((ServerWorld) entity.getWorld(), Items.TOTEM_OF_UNDYING.getDefaultStack());
                *///?}

            }
        }
    }

    @Override
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        super.onPlayerDeath(player, source);

        TriviaBot.cursedGigantificationPlayers.remove(player.getUuid());
        TriviaBot.cursedHeartPlayers.remove(player.getUuid());
        resetMaxPlayerHealth(player);

        TriviaBot.cursedMoonJumpPlayers.remove(player.getUuid());
        //? if <=1.21 {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH)).setBaseValue(0.41999998688697815);
        //?} else
        /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH)).setBaseValue(0.41999998688697815);*/
    }
}
