package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.utils.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.mat0u5.lifeseries.Main.*;

public abstract class Series extends Session {
    public static final String RESOURCEPACK_MAIN_URL = "https://github.com/Mat0u5/LifeSeries-Resources/releases/download/release-main-af45fc947c22c9ee91ec021d998318a5f2d5bdaf/RP.zip";
    public static final String RESOURCEPACK_MAIN_SHA ="38a74dc7c112e1e9c009e71f544b1b050a01560e";
    public boolean NO_HEALING = false;

    public abstract SeriesList getSeries();
    public abstract ConfigManager getConfig();

    public Blacklist createBlacklist() {
        return new Blacklist();
    }

    public void initialize() {
        createTeams();
        createScoreboards();
        updateStuff();
        reload();
    }

    public void updateStuff() {
        if (server == null) return;
        if (server.getOverworld().getWorldBorder().getSize() > 1000000 && seriesConfig.getOrCreateBoolean("auto_set_worldborder", true)) {
            OtherUtils.executeCommand("worldborder set 500");
        }
        if (seriesConfig.getOrCreateBoolean("auto_keep_inventory", true)) {
            OtherUtils.executeCommand("gamerule keepInventory true");
        }
        if (NO_HEALING) {
            OtherUtils.executeCommand("gamerule naturalRegeneration false");
        }
        else {
            OtherUtils.executeCommand("gamerule naturalRegeneration true");
        }
    }

    public void reload() {}

    public void createTeams() {
        TeamUtils.createTeam("Dead", Formatting.DARK_GRAY);
        TeamUtils.createTeam("Unassigned", Formatting.GRAY);

        TeamUtils.createTeam("Red", Formatting.DARK_RED);
        TeamUtils.createTeam("Yellow", Formatting.YELLOW);
        TeamUtils.createTeam("Green", Formatting.GREEN);
        TeamUtils.createTeam("DarkGreen", Formatting.DARK_GREEN);
    }

    public Formatting getColorForLives(Integer lives) {
        if (lives == null) return Formatting.GRAY;
        if (lives == 1) return Formatting.DARK_RED;
        if (lives == 2) return Formatting.YELLOW;
        if (lives == 3) return Formatting.GREEN;
        if (lives >= 4) return Formatting.DARK_GREEN;
        return Formatting.DARK_GRAY;
    }

    public Text getFormattedLives(ServerPlayerEntity player) {
        return getFormattedLives(getPlayerLives(player));
    }

    public Text getFormattedLives(Integer lives) {
        if (lives == null) return Text.empty();
        Formatting color = getColorForLives(lives);
        return Text.literal(String.valueOf(lives)).formatted(color);
    }

    public void createScoreboards() {
        ScoreboardUtils.createObjective("Lives");
    }

    public void reloadAllPlayerTeams() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            reloadPlayerTeam(player);
        }
    }

    public void reloadPlayerTeam(ServerPlayerEntity player) {
        if (!player.isDead()) {
            reloadPlayerTeamActual(player);
        }
        else {
            TaskScheduler.scheduleTask(2, () -> reloadPlayerTeamActual(player));
        }
    }

    public void reloadPlayerTeamActual(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (lives == null) TeamUtils.addPlayerToTeam("Unassigned",player);
        else if (lives <= 0) TeamUtils.addPlayerToTeam("Dead",player);
        else if (lives == 1) TeamUtils.addPlayerToTeam("Red",player);
        else if (lives == 2) TeamUtils.addPlayerToTeam("Yellow",player);
        else if (lives == 3) TeamUtils.addPlayerToTeam("Green",player);
        else if (lives >= 4) TeamUtils.addPlayerToTeam("DarkGreen",player);
    }

    public Integer getPlayerLives(ServerPlayerEntity player) {
        return ScoreboardUtils.getScore(ScoreHolder.fromName(player.getNameForScoreboard()), "Lives");
    }

    public boolean hasAssignedLives(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        return lives != null;
    }

    public boolean isAlive(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (!hasAssignedLives(player)) return false;
        return lives > 0;
    }

    public void removePlayerLife(ServerPlayerEntity player) {
        addToPlayerLives(player,-1);
    }

    public void resetPlayerLife(ServerPlayerEntity player) {
        ScoreboardUtils.resetScore(ScoreHolder.fromName(player.getNameForScoreboard()), "Lives");
        reloadPlayerTeam(player);
    }

    public void resetAllPlayerLives() {
        ScoreboardUtils.removeObjective("Lives");
        createScoreboards();
        currentSeries.reloadAllPlayerTeams();
    }

    public void addPlayerLife(ServerPlayerEntity player) {
        addToPlayerLives(player,1);
    }

    public void addToPlayerLives(ServerPlayerEntity player, int amount) {
        Integer currentLives = getPlayerLives(player);
        if (currentLives == null) currentLives = 0;
        int lives = currentLives + amount;
        if (lives < 0) lives = 0;
        setPlayerLives(player,lives);
    }

    public void setPlayerLives(ServerPlayerEntity player, int lives) {
        ScoreboardUtils.setScore(ScoreHolder.fromName(player.getNameForScoreboard()), "Lives", lives);
        if (lives <= 0) {
            playerLostAllLives(player);
        }
        else if (player.isSpectator()) {
            player.changeGameMode(GameMode.SURVIVAL);
        }
        reloadPlayerTeam(player);
    }

    @Nullable
    public Boolean isOnLastLife(ServerPlayerEntity player) {
        if (!isAlive(player)) return null;
        Integer lives = currentSeries.getPlayerLives(player);
        return lives == 1;
    }

    public boolean isOnLastLife(ServerPlayerEntity player, boolean fallback) {
        Boolean isOnLastLife = isOnLastLife(player);
        if (isOnLastLife == null) return fallback;
        return isOnLastLife;
    }

    @Nullable
    public Boolean isOnSpecificLives(ServerPlayerEntity player, int check) {
        if (!isAlive(player)) return null;
        Integer lives = currentSeries.getPlayerLives(player);
        return lives == check;
    }

    public boolean isOnSpecificLives(ServerPlayerEntity player, int check, boolean fallback) {
        Boolean isOnLife = isOnSpecificLives(player, check);
        if (isOnLife == null) return fallback;
        return isOnLife;
    }

    @Nullable
    public Boolean isOnAtLeastLives(ServerPlayerEntity player, int check) {
        if (!isAlive(player)) return null;
        Integer lives = currentSeries.getPlayerLives(player);
        return lives >= check;
    }

    public boolean isOnAtLeastLives(ServerPlayerEntity player, int check, boolean fallback) {
        Boolean isOnAtLeast = isOnAtLeastLives(player, check);
        if (isOnAtLeast == null) return fallback;
        return isOnAtLeast;
    }


    private HashMap<UUID, HashMap<Vec3d,List<Float>>> respawnPositions = new HashMap<>();
    public void playerLostAllLives(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SPECTATOR);
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER);
        WorldUitls.summonHarmlessLightning(player.getServerWorld(), player);
        Vec3d pos = player.getPos();
        HashMap<Vec3d, List<Float>> info = new HashMap<>();
        info.put(pos, List.of(player.getYaw(),player.getPitch()));
        respawnPositions.put(player.getUuid(), info);
        dropItemsOnLastDeath(player);
        showDeathTitle(player);
        Stats.onPlayerLostAllLives(player);
    }

    public void dropItemsOnLastDeath(ServerPlayerEntity player) {
        boolean doDrop = seriesConfig.getOrCreateBoolean("players_drop_items_on_last_death", false);
        boolean keepInventory = player.server.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
        if (doDrop && keepInventory) {
            for (ItemStack item : PlayerUtils.getPlayerInventory(player)) {
                //? if <= 1.21 {
                player.dropStack(item);
                //?} else
                /*player.dropStack(player.getServerWorld(), item);*/
            }
        }
    }

    public void showDeathTitle(ServerPlayerEntity player) {
        if (!seriesConfig.getOrCreateBoolean("show_death_title_on_last_death",true)) return;
        PlayerUtils.sendTitleWithSubtitleToPlayers(PlayerUtils.getAllPlayers(), player.getStyledDisplayName(), Text.literal("ran out of lives!"), 20, 80, 20);
        OtherUtils.broadcastMessage(Text.literal("").append(player.getStyledDisplayName()).append(Text.of(" ran out of lives.")));
    }

    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (isOnLastLife(attacker, false)) return true;
        if (attacker.getPrimeAdversary() == victim && (isOnLastLife(victim, false))) return true;
        return false;
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

    public List<ServerPlayerEntity> getAlivePlayers() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllPlayers();
        if (players == null) return new ArrayList<>();
        if (players.isEmpty()) return new ArrayList<>();
        List<ServerPlayerEntity> alivePlayers = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            if (!isAlive(player)) continue;
            alivePlayers.add(player);
        }
        return alivePlayers;
    }

    public boolean getGreenPlayers() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (isOnSpecificLives(player, 3, false)) return true;
        }
        return false;
    }

    public boolean getYellowPlayers() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (isOnSpecificLives(player, 2, false)) return true;
        }
        return false;
    }
    /*
        Events
     */

    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        Stats.onPlayerDeath(player, source);
        boolean killedByPlayer = false;
        if (source != null) {
            if (source.getAttacker() instanceof ServerPlayerEntity) {
                if (player != source.getAttacker()) {
                    onPlayerKilledByPlayer(player, (ServerPlayerEntity) source.getAttacker());
                    killedByPlayer = true;
                }
            }
        }
        if (player.getPrimeAdversary() != null && !killedByPlayer) {
            if (player.getPrimeAdversary() instanceof ServerPlayerEntity) {
                if (player != player.getPrimeAdversary()) {
                    onPlayerKilledByPlayer(player, (ServerPlayerEntity) player.getPrimeAdversary());
                    killedByPlayer = true;
                }
            }
        }
        if (!killedByPlayer) {
            onPlayerDiedNaturally(player);
        }
        removePlayerLife(player);
    }

    public void onPlayerDiedNaturally(ServerPlayerEntity player) {
        if (server == null) return;
        playerNaturalDeathLog.remove(player.getUuid());
        playerNaturalDeathLog.put(player.getUuid(), server.getTicks());
    }

    public void onPlayerRespawn(ServerPlayerEntity player) {
        if (!respawnPositions.containsKey(player.getUuid())) return;
        HashMap<Vec3d, List<Float>> info = respawnPositions.get(player.getUuid());
        respawnPositions.remove(player.getUuid());
        for (Map.Entry<Vec3d, List<Float>> entry : info.entrySet()) {
            //? if <= 1.21 {
            player.teleport(player.getServerWorld(), entry.getKey().x, entry.getKey().y, entry.getKey().z, EnumSet.noneOf(PositionFlag.class), entry.getValue().get(0), entry.getValue().get(1));
            //?} else {
            /*player.teleport(player.getServerWorld(), entry.getKey().x, entry.getKey().y, entry.getKey().z, EnumSet.noneOf(PositionFlag.class), entry.getValue().get(0), entry.getValue().get(1), false);
            *///?}
            break;
        }
    }

    public void onClaimKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        Stats.claimKill(killer, victim);
    }

    public void onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
    }

    public void onPlayerHeal(ServerPlayerEntity player, float amount) {
    }

    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
    }

    public void onMobDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity.getEntityWorld().isClient() || !(damageSource.getAttacker() instanceof ServerPlayerEntity)) {
            return;
        }
        modifyMobDrops(entity, damageSource);
    }

    public void modifyMobDrops(LivingEntity entity, DamageSource damageSource) {
        spawnEggChance(entity);
    }

    private void spawnEggChance(LivingEntity entity) {
        double chance = seriesConfig.getOrCreateDouble("spawn_egg_drop_chance", 0.05);
        if (chance <= 0) return;
        if (entity instanceof EnderDragonEntity) return;
        if (entity instanceof WitherEntity) return;
        if (entity instanceof WardenEntity) return;
        if (entity instanceof ElderGuardianEntity) return;
        if (entity.getCommandTags().contains("notNatural")) return;

        EntityType<?> entityType = entity.getType();
        SpawnEggItem spawnEgg = SpawnEggItem.forEntity(entityType);


        if (spawnEgg == null) return;
        ItemStack spawnEggItem = spawnEgg.getDefaultStack();
        if (spawnEggItem == null) return;
        if (spawnEggItem.isEmpty()) return;

        // Drop the spawn egg with a 5% chance
        if (Math.random() <= chance) {
            //? if <=1.21 {
            entity.dropStack(spawnEggItem);
            //?} else
            /*entity.dropStack((ServerWorld) entity.getWorld(), spawnEggItem);*/
        }
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        if (getSeries() != SeriesList.SECRET_LIFE) {
            double health = seriesConfig.getOrCreateDouble("max_player_health", 20.0d);
            //? if <=1.21 {
            Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(health);
             //?} else
            /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);*/
        }
        reloadPlayerTeam(player);
        TaskScheduler.scheduleTask(2, () -> {
            PlayerUtils.applyResourcepack(player.getUuid());
        });

        if (statusNotStarted() && PermissionManager.isAdmin(player)) {
            TaskScheduler.scheduleTask(100, () -> {
                player.sendMessage(Text.of("\nUse §b'/session timer set <time>'§f to set the desired session time."));
                player.sendMessage(Text.of("After that, use §b'/session start'§f to start the session."));
            });
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
    }
}
