package net.mat0u5.lifeseries.series;

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
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public abstract class Series extends Session {
    public boolean DROP_SPAWN_EGGS = true;
    public boolean CUSTOM_ENCHANTMENT_TABLE_ALGORITHM = false;
    public boolean NO_HEALING = false;

    public abstract SeriesList getSeries();
    public abstract Blacklist createBlacklist();
    public String getResourcepackURL() {
        return "https://github.com/Mat0u5/LifeSeries-Resources/releases/download/release-main-0c89c6fd068f52aeb882e1c0bda935eb46f24331/RP.zip";
    }
    public String getResourcepackSHA1() {
        return "7f4eba01453f6cf58bf08131fc5576f5ed873679";
    }
    public void initialize() {
        createTeams();
        createScoreboards();
        updateStuff();
    }
    public void updateStuff() {
        if (server.getOverworld().getWorldBorder().getSize() > 1000000) {
            OtherUtils.executeCommand("worldborder set 500");
        }
        OtherUtils.executeCommand("gamerule keepInventory true");
        if (NO_HEALING) {
            OtherUtils.executeCommand("gamerule naturalRegeneration false");
        }
        else {
            OtherUtils.executeCommand("gamerule naturalRegeneration true");
        }
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
    public boolean isOnSpecificLives(ServerPlayerEntity player, int check, boolean fallback) {
        Boolean isOnLife = isOnSpecificLives(player, check);
        if (isOnLife == null) return fallback;
        return isOnLife;
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
    }
    public void getRespawnTarget(ServerPlayerEntity player, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        if (!respawnPositions.containsKey(player.getUuid())) return;
        HashMap<Vec3d, List<Float>> info = respawnPositions.get(player.getUuid());
        respawnPositions.remove(player.getUuid());
        for (Map.Entry<Vec3d, List<Float>> entry : info.entrySet()) {
            cir.setReturnValue(new TeleportTarget(player.getServerWorld(), entry.getKey(), Vec3d.ZERO, entry.getValue().get(0), entry.getValue().get(1), postDimensionTransition));
            break;
        }
    }
    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (isOnLastLife(attacker, false)) return true;
        if (attacker.getPrimeAdversary() == victim && (isOnLastLife(victim, false))) return true;
        return false;
    }
    /*
        Events
     */
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
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
        if (playerNaturalDeathLog.containsKey(player.getUuid())) {
            playerNaturalDeathLog.remove(player.getUuid());
        }
        playerNaturalDeathLog.put(player.getUuid(), server.getTicks());
    }
    public void onClaimKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
    }
    public void onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {

    }
    public void onPlayerHeal(ServerPlayerEntity player, float amount) {

    }
    public void onMobDeath(LivingEntity entity, DamageSource damageSource) {
        if (!DROP_SPAWN_EGGS) return;
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

        // Drop the spawn egg with a 5% chance
        if (Math.random() <= 0.05d) {
            entity.dropStack(spawnEggItem);
        }
    }
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {

    }
    public void onPlayerJoin(ServerPlayerEntity player) {
        if (getSeries() != SeriesList.SECRET_LIFE) {
            Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20);
        }
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
    public boolean anyGreenPlayers() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (isOnSpecificLives(player, 3, false)) return true;
        }
        return false;
    }
    public boolean anyYellowPlayers() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (isOnSpecificLives(player, 2, false)) return true;
        }
        return false;
    }
}
