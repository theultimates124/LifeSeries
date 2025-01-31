package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.entity.pathfinder.PathFinder;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public class Snails extends Wildcard {

    HashMap<UUID, Snail> snails = new HashMap<>();
    int ticks = 0;

    @Override
    public Wildcards getType() {
        return Wildcards.SNAILS;
    }

    @Override
    public void activate() {
        super.activate();
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            spawnSnailFor(player);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        killAllSnails();
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks % 200 == 0) {
            for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
                UUID playerUUID = player.getUuid();
                if (snails.containsKey(playerUUID)) {
                    Snail snail = snails.get(playerUUID);
                    if (snail == null || snail.isDead()) {
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
        server.getWorlds().forEach(world -> {
            world.iterateEntities().forEach(entity -> {
                if (entity instanceof Snail || entity instanceof PathFinder) {
                    //? if <= 1.21 {
                    entity.kill();
                    //?} else {
                    /*entity.kill((ServerWorld) entity.getWorld());
                     *///?}
                    entity.remove(Entity.RemovalReason.KILLED);
                }
            });
        });
    }
}
