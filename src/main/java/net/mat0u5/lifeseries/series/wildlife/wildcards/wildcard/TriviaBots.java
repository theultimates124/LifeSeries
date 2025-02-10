package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
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
    @Override
    public Wildcards getType() {
        return Wildcards.TRIVIA_BOT;
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
        super.deactivate();
    }

    public static void spawnBotFor(ServerPlayerEntity player) {
        TriviaBot bot = MobRegistry.TRIVIA_BOT.spawn(player.getServerWorld(), player.getBlockPos().add(0,20,0), SpawnReason.COMMAND);
        if (bot != null) {
            bot.setBoundPlayer(player);
            bots.put(player.getUuid(), bot);
            bot.teleportNearPlayer(20);
        }
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
}
