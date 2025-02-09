package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.entity.pathfinder.PathFinder;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public class Snails extends Wildcard {

    public static HashMap<UUID, Snail> snails = new HashMap<>();
    int ticks = 0;

    @Override
    public Wildcards getType() {
        return Wildcards.SNAILS;
    }

    @Override
    public void activate() {
        snails.clear();
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            spawnSnailFor(player);
        }
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
            for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
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
                if (entity instanceof Snail || entity instanceof PathFinder) {
                    toKill.add(entity);
                }
            }
        }
        toKill.forEach(Entity::discard);
    }
}
