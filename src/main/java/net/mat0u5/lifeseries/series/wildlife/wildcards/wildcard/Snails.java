package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.config.StringListConfig;
import net.mat0u5.lifeseries.entity.pathfinder.PathFinder;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class Snails extends Wildcard {
    public static StringListConfig snailNameConfig;

    public static HashMap<UUID, Snail> snails = new HashMap<>();
    public static HashMap<UUID, String> snailNames = new HashMap<>();
    int ticks = 0;

    @Override
    public Wildcards getType() {
        return Wildcards.SNAILS;
    }

    @Override
    public void activate() {
        snails.clear();
        for (ServerPlayerEntity player : currentSeries.getAlivePlayers()) {
            spawnSnailFor(player);
        }
        loadSnailNames();
        super.activate();
    }

    @Override
    public void deactivate() {
        snails.clear();
        killAllSnails();
        super.deactivate();
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks % 200 == 0) {
            for (ServerPlayerEntity player : currentSeries.getAlivePlayers()) {
                UUID playerUUID = player.getUuid();
                if (snails.containsKey(playerUUID)) {
                    Snail snail = snails.get(playerUUID);
                    if (snail == null || snail.isDead() || snail.isRemoved()) {
                        snails.remove(playerUUID);
                        spawnSnailFor(player);
                    }
                }
                else {
                    spawnSnailFor(player);
                }
            }
        }
    }

    public void spawnSnailFor(ServerPlayerEntity player) {
        Snail snail = MobRegistry.SNAIL.spawn(player.getServerWorld(), player.getBlockPos().add(0,20,0), SpawnReason.COMMAND);
        if (snail != null) {
            snail.setBoundPlayer(player);
            snails.put(player.getUuid(), snail);
            snail.teleportNearPlayer(20);
        }
    }

    public static void killAllSnails() {
        if (server == null) return;
        List<Entity> toKill = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof Snail snail) {
                    if (!snail.fromTrivia) {
                        toKill.add(entity);
                    }
                }
                if (entity instanceof PathFinder pf) {
                    toKill.add(entity);
                }
            }
        }
        toKill.forEach(Entity::discard);
    }

    public static void reloadSnailNames() {
        for (Snail snail : snails.values()) {
            if (snail == null) return;
            snail.updateSnailName();
        }
    }

    public static void setSnailName(ServerPlayerEntity player, String name) {
        snailNames.put(player.getUuid(), name);
        reloadSnailNames();
        saveSnailNames();
    }

    public static void resetSnailName(ServerPlayerEntity player) {
        snailNames.remove(player.getUuid());
        reloadSnailNames();
        saveSnailNames();
    }

    public static String getSnailName(ServerPlayerEntity player) {
        if (snailNames.containsKey(player.getUuid())) {
            return snailNames.get(player.getUuid());
        }
        return player.getNameForScoreboard()+"'s Snail";
    }

    public static void saveSnailNames() {
        if (snailNameConfig == null) loadConfig();
        List<String> names = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : snailNames.entrySet()) {
            names.add(entry.getKey().toString()+"_"+entry.getValue().replaceAll("_",""));
        }
        snailNameConfig.save(names);
    }

    public static void loadSnailNames() {
        if (snailNameConfig == null) loadConfig();
        HashMap<UUID, String> newNames = new HashMap<>();
        for (String entry : snailNameConfig.load()) {
            if (!entry.contains("_")) continue;
            String[] split = entry.split("_");
            if (split.length != 2) continue;
            try {
                UUID uuid = UUID.fromString(split[0]);
                newNames.put(uuid, split[1]);
            } catch(Exception ignored) {}
        }
        snailNames = newNames;
    }

    public static void loadConfig() {
        snailNameConfig = new StringListConfig("./config/lifeseries/main", "DO_NOT_MODIFY_wildlife_snailnames.properties");
    }
}
