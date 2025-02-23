package net.mat0u5.lifeseries.series.wildlife.wildcards;

import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.*;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.TriviaWildcard;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.*;

public class WildcardManager {
    public static HashMap<Wildcards, Wildcard> activeWildcards = new HashMap<>();
    public static Random rnd = new Random();
    public static SessionAction wildcardNotice = new SessionAction(OtherUtils.secondsToTicks(30)) {
        @Override
        public void trigger() {
            if (activeWildcards.isEmpty()) {
                OtherUtils.broadcastMessage(Text.literal("A Wildcard will be activated in 2 minutes!").formatted(Formatting.GRAY));
            }
        }
    };
    public static SessionAction startWildcards = new SessionAction(OtherUtils.secondsToTicks(150),"§7Activate Wildcard §f[00:02:30]", "Activate Wildcard") {
        @Override
        public void trigger() {
            if (activeWildcards.isEmpty()) {
                activateWildcards();
            }
        }
    };

    public static WildLife getSeries() {
        if (currentSeries instanceof WildLife wildLife) return wildLife;
        return null;
    }

    public static void chooseRandomWildcard() {
        //TODO
        //activeWildcards.put(Wildcards.SIZE_SHIFTING, new SizeShifting());
        //activeWildcards.put(Wildcards.HUNGER, new Hunger());
        //activeWildcards.put(Wildcards.TIME_DILATION, new TimeDilation());
        //activeWildcards.put(Wildcards.SNAILS, new Snails());
        //activeWildcards.put(Wildcards.MOB_SWAP, new MobSwap());
        activeWildcards.put(Wildcards.TRIVIA, new TriviaWildcard());
    }

    public static void resetWildcardsOnPlayerJoin(ServerPlayerEntity player) {
        if (!isActiveWildcard(Wildcards.SIZE_SHIFTING)) {
            if (SizeShifting.getPlayerSize(player) != 1 && !TriviaBot.cursedGigantificationPlayers.contains(player.getUuid())) SizeShifting.setPlayerSize(player, 1);
        }
        if (!isActiveWildcard(Wildcards.HUNGER)) {
            player.removeStatusEffect(StatusEffects.HUNGER);
        }
        if (!isActiveWildcard(Wildcards.TRIVIA)) {
            TriviaWildcard.resetPlayerOnBotSpawn(player);
        }
        TaskScheduler.scheduleTask(20, () -> {
            Hunger.updateInventory(player);
        });
        MORPH_COMPONENT.maybeGet(player).ifPresent(morphComponent -> morphComponent.setMorph(null));
    }

    public static void activateWildcards() {
        showDots();
        TaskScheduler.scheduleTask(90, () -> {
            if (activeWildcards.isEmpty()) {
                chooseRandomWildcard();
            }
            for (Wildcard wildcard : activeWildcards.values()) {
                if (wildcard.active) continue;
                wildcard.activate();
            }
            showCryptTitle("A wildcard is active!");
        });
        TaskScheduler.scheduleTask(92, NetworkHandlerServer::sendUpdatePackets);
    }

    public static void fadedWildcard() {
        if (activeWildcards.containsKey(Wildcards.TIME_DILATION)) {
            TaskScheduler.scheduleTask(5, () -> {
                OtherUtils.broadcastMessage(Text.of("§7A Wildcard has faded..."));
                PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.BLOCK_BEACON_DEACTIVATE);
            });
            return;
        }
        OtherUtils.broadcastMessage(Text.of("§7A Wildcard has faded..."));
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.BLOCK_BEACON_DEACTIVATE);
    }

    public static void showDots() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllPlayers();
        PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.4f, 1);
        //PlayerUtils.sendTitleToPlayers(players, Text.literal("§a."),0,40,0);
        PlayerUtils.sendTitleToPlayers(players, Text.literal("§a§l,"),0,40,0);
        TaskScheduler.scheduleTask(30, () -> {
            PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.4f, 1);
            //PlayerUtils.sendTitleToPlayers(players, Text.literal("§a. §e."),0,40,0);
            PlayerUtils.sendTitleToPlayers(players, Text.literal("§a§l, §e§l,"),0,40,0);
        });
        TaskScheduler.scheduleTask(60, () -> {
            PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.4f, 1);
            //PlayerUtils.sendTitleToPlayers(players, Text.literal("§a. §e. §c."),0,40,0);
            PlayerUtils.sendTitleToPlayers(players, Text.literal("§a§l, §e§l, §c§l,"),0,40,0);
        });
    }

    public static void showCryptTitle(String text) {
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.2f, 1);
        String colorCrypt = "§r§6§l§k";
        String colorNormal = "§r§6§l";
        String cryptedText = "";
        for (Character character : text.toCharArray()) {
            cryptedText += "<"+character;
        }

        float pos = 0;
        for (int i = 0; i < text.length(); i++) {
            pos += 4;
            if (!cryptedText.contains("<")) return;
            String[] split = cryptedText.split("<");
            int timesRemaining = split.length;
            int random = rnd.nextInt(1, timesRemaining);
            split[random] = ">"+split[random];
            cryptedText = String.join("<", split).replaceAll("<>", colorNormal);

            String finalCryptedText = cryptedText.replaceAll("<",colorCrypt);
            TaskScheduler.scheduleTask((int) pos, () -> PlayerUtils.sendTitleToPlayers(PlayerUtils.getAllPlayers(), Text.literal(finalCryptedText),0,30,20));
        }
    }

    public static void tick() {
        for (Wildcard wildcard : activeWildcards.values()) {
            wildcard.softTick();
            if (!wildcard.active) continue;
            wildcard.tick();
        }
        SizeShifting.resetSizesTick(isActiveWildcard(Wildcards.SIZE_SHIFTING));
        if (!isActiveWildcard(Wildcards.MOB_SWAP) && server != null && server.getTicks() % 200 == 0) {
            MobSwap.killMobSwapMobs();
        }
    }

    public static void tickSessionOn() {
        for (Wildcard wildcard : activeWildcards.values()) {
            if (!wildcard.active) continue;
            wildcard.tickSessionOn();
        }
    }

    public static void onSessionStart() {

    }

    public static void onSessionEnd() {
        if (!activeWildcards.isEmpty()) {
            fadedWildcard();
        }
        for (Wildcard wildcard : activeWildcards.values()) {
            wildcard.deactivate();
        }
        activeWildcards.clear();
        NetworkHandlerServer.sendUpdatePackets();
    }

    public static boolean isActiveWildcard(Wildcards wildcard) {
        return activeWildcards.containsKey(wildcard);
    }

    public static void onUseItem(ServerPlayerEntity player) {
        Hunger.onUseItem(player);
    }
}
