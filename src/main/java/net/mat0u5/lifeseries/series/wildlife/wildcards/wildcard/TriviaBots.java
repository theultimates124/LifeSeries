package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public class TriviaBots extends Wildcard {
    public static HashMap<UUID, TriviaBot> bots = new HashMap<>();
    int ticks = 0;
    @Override
    public Wildcards getType() {
        return Wildcards.TRIVIA_BOT;
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks % 200 == 0) {
            for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
                UUID playerUUID = player.getUuid();
                if (bots.containsKey(playerUUID)) {
                    TriviaBot bot = bots.get(playerUUID);
                    if (bot == null || bot.isDead() || bot.isRemoved()) {
                        bots.remove(playerUUID);
                    }
                }
            }
        }
    }

    @Override
    public void activate() {
        bots.clear();
        super.activate();
    }

    @Override
    public void deactivate() {
        bots.clear();
        killAllBots();
        killAllTriviaSnails();
        super.deactivate();
    }

    public static void handleAnswer(ServerPlayerEntity player, int answer) {
        if (bots.containsKey(player.getUuid())) {
            TriviaBot bot = bots.get(player.getUuid());
            if (!bot.isDead() && !bot.isRemoved()) {
                bot.handleAnswer(answer);
            }
        }
    }

    public static void spawnBotFor(ServerPlayerEntity player) {
        resetPlayerOnBotSpawn(player);
        TriviaBot bot = MobRegistry.TRIVIA_BOT.spawn(player.getServerWorld(), player.getBlockPos().add(0,50,0), SpawnReason.COMMAND);
        if (bot != null) {
            bot.setBoundPlayer(player);
            bots.put(player.getUuid(), bot);
            bot.teleportAbovePlayer(10, 50);
        }
    }

    public static void resetPlayerOnBotSpawn(ServerPlayerEntity player) {
        if (bots.containsKey(player.getUuid())) {
            TriviaBot bot = bots.get(player.getUuid());
            if (!bot.isDead() && !bot.isRemoved()) {
                bot.despawn();
            }
        }
        killTriviaSnailFor(player);
    }

    public static void killAllBots() {
        if (server == null) return;
        List<Entity> toKill = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof TriviaBot) {
                    toKill.add(entity);
                }
            }
        }
        toKill.forEach(Entity::discard);
    }
    public static void killAllTriviaSnails() {
        if (server == null) return;
        List<Entity> toKill = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof Snail snail) {
                    if (snail.fromTrivia) {
                        toKill.add(entity);
                    }
                }
            }
        }
        toKill.forEach(Entity::discard);
    }
    public static void killTriviaSnailFor(ServerPlayerEntity player) {
        if (server == null) return;
        List<Entity> toKill = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof Snail snail) {
                    if (snail.fromTrivia) {
                        UUID boundPlayer = snail.boundPlayerUUID;
                        if (boundPlayer == null || boundPlayer.equals(player.getUuid())) {
                            toKill.add(entity);
                        }
                    }
                }
            }
        }
        toKill.forEach(Entity::discard);
    }
}
