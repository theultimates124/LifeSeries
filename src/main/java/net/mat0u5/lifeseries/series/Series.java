package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.mat0u5.lifeseries.utils.TeamUtils;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public abstract class Series extends Session {
    public abstract SeriesList getSeries();
    public abstract Blacklist createBlacklist();
    public void initialize() {
        createTeams();
        createScoreboards();
    }
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
    public Boolean isOnSpecificLives(ServerPlayerEntity player, int check) {
        if (!isAlive(player)) return null;
        Integer lives = currentSeries.getPlayerLives(player);
        return lives == check;
    }
    public void playerLostAllLives(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SPECTATOR);
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER);
    }
    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (isOnLastLife(attacker, false)) return true;
        if (attacker.getPrimeAdversary() == victim && (isOnLastLife(victim, true))) return true;
        return false;
    }
    /*
        Events
     */
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        removePlayerLife(player);
        if (source != null) {
            if (source.getAttacker() instanceof ServerPlayerEntity) {
                onPlayerKilledByPlayer(player, (ServerPlayerEntity) source.getAttacker());
                return;
            }
        }
        if (player.getPrimeAdversary() != null) {
            if (player.getPrimeAdversary() instanceof ServerPlayerEntity) {
                onPlayerKilledByPlayer(player, (ServerPlayerEntity) player.getPrimeAdversary());
            }
        }
    }
    public void onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {

    }
    public void onPlayerHeal(ServerPlayerEntity player, float amount) {

    }
    public void onMobDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity.getEntityWorld().isClient() || !(damageSource.getAttacker() instanceof ServerPlayerEntity)) {
            return;
        }
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

        // Drop the spawn egg with a 3% chance
        if (Math.random() <= 0.0333d) {
            entity.dropStack(spawnEggItem);
        }
    }
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {

    }
    public void onPlayerJoin(ServerPlayerEntity player) {
        reloadPlayerTeam(player);
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
