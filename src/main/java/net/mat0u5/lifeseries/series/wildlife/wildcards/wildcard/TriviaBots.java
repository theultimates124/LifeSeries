package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

import static net.mat0u5.lifeseries.Main.*;

public class TriviaBots extends Wildcard {
    private static final Map<UUID, Queue<Integer>> playerSpawnQueue = new HashMap<>();
    private static final Map<UUID, Integer> spawnedBotsFor = new HashMap<>();
    private static boolean globalScheduleInitialized = false;
    public static HashMap<UUID, TriviaBot> bots = new HashMap<>();
    public static int activatedAt = -1;
    public static int TRIVIA_BOTS_PER_PLAYER = 5;
    public static int MIN_BOT_DELAY = 8400;

    @Override
    public Wildcards getType() {
        return Wildcards.TRIVIA_BOT;
    }

    @Override
    public void tickSessionOn() {
        int passedTime = (int) ((float) currentSession.passedTime - activatedAt);
        if (passedTime % 20 == 0) trySpawnBots();
        if (passedTime % 200 == 0) updateDeadBots();
    }

    public void trySpawnBots() {
        int currentTick = (int) currentSession.passedTime;
        int sessionStart = activatedAt;
        int sessionEnd = currentSession.sessionLength - 6000; // Don't spawn bots 5 minutes before the end
        int availableTime = sessionEnd - sessionStart;

        List<ServerPlayerEntity> players = currentSeries.getAlivePlayers();
        int numPlayers = players.size();
        int desiredTotalSpawns = numPlayers * TRIVIA_BOTS_PER_PLAYER;

        int interval = availableTime / desiredTotalSpawns;
        if (numPlayers * interval < MIN_BOT_DELAY) {
            interval = MIN_BOT_DELAY / numPlayers;
        }

        int maxSpawns = Math.min(desiredTotalSpawns, availableTime / interval);

        for (ServerPlayerEntity player : players) {
            UUID uuid = player.getUuid();
            if (!playerSpawnQueue.containsKey(uuid)) {
                playerSpawnQueue.put(uuid, new LinkedList<>());
                globalScheduleInitialized = false;
            }
        }

        if (!globalScheduleInitialized) {
            playerSpawnQueue.values().forEach(Collection::clear);
            for (int i = 0; i < maxSpawns; i++) {
                int spawnTime = sessionStart + 100 + i * interval;
                ServerPlayerEntity assignedPlayer = players.get(i % numPlayers);
                UUID uuid = assignedPlayer.getUuid();
                if (spawnTime > currentTick) {
                    playerSpawnQueue.get(uuid).offer(spawnTime);
                }
            }
            globalScheduleInitialized = true;
        }

        for (ServerPlayerEntity player : players) {
            UUID uuid = player.getUuid();
            Queue<Integer> queue = playerSpawnQueue.get(uuid);
            if (queue != null && !queue.isEmpty()) {
                if (currentTick >= queue.peek()) {
                    queue.poll();
                    if (spawnedBotsFor.containsKey(player.getUuid())) {
                        spawnedBotsFor.put(player.getUuid(), 1+spawnedBotsFor.get(player.getUuid()));
                    }
                    else {
                        spawnedBotsFor.put(player.getUuid(), 1);
                    }
                    if (spawnedBotsFor.get(player.getUuid()) <= TRIVIA_BOTS_PER_PLAYER) {
                        spawnBotFor(player);
                    }
                }
            }
        }
    }


    public void updateDeadBots() {
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

    @Override
    public void activate() {
        resetQueue();
        spawnedBotsFor.clear();
        activatedAt = (int) currentSession.passedTime;
        bots.clear();
        super.activate();
    }

    @Override
    public void deactivate() {
        resetQueue();
        spawnedBotsFor.clear();
        bots.clear();
        killAllBots();
        killAllTriviaSnails();
        super.deactivate();
    }

    public static void reload() {
        resetQueue();
    }

    public static void resetQueue() {
        globalScheduleInitialized = false;
        playerSpawnQueue.clear();
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
