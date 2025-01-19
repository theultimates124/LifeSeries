package net.mat0u5.lifeseries.series.wildlife.wildcards;

import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.SizeShifting;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class WildcardManager {
    public static HashMap<Wildcards, Wildcard> activeWildcards = new HashMap<>();
    public static Random rnd = new Random();
    public static SessionAction wildcardNotice = new SessionAction(OtherUtils.secondsToTicks(30)) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("A Wildcard will be activated in 3 minutes!").formatted(Formatting.GRAY));
        }
    };
    public static SessionAction startWildcards = new SessionAction(OtherUtils.secondsToTicks(210),"§7Activate Wildcard §f[00:03:30]") {
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
        activeWildcards.put(Wildcards.HUNGER, new Hunger());
    }

    public static void resetWildcardsOnPlayerJoin(ServerPlayerEntity player) {
        if (!activeWildcards.containsKey(Wildcards.SIZE_SHIFTING)) {
            if (SizeShifting.getPlayerSize(player) != 1) SizeShifting.setPlayerSize(player, 1);
        }
    }

    public static void activateWildcards() {
        showDots();
        TaskScheduler.scheduleTask(90, () -> {
            showCryptTitle("A wildcard is active!");
            if (activeWildcards.isEmpty()) {
                chooseRandomWildcard();
            }
            for (Wildcard wildcard : activeWildcards.values()) {
                wildcard.activate();
            }
        });
    }

    public static void fadedWildcard() {
        OtherUtils.broadcastMessage(Text.of("§7A Wildcard has faded..."));
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.BLOCK_BEACON_DEACTIVATE);
    }

    public static void showDots() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllPlayers();
        PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.4f, 1);
        PlayerUtils.sendTitleToPlayers(players, Text.literal("§a."),0,40,0);
        TaskScheduler.scheduleTask(30, () -> {
            PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.4f, 1);
            PlayerUtils.sendTitleToPlayers(players, Text.literal("§a. §e."),0,40,0);
        });
        TaskScheduler.scheduleTask(60, () -> {
            PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.4f, 1);
            PlayerUtils.sendTitleToPlayers(players, Text.literal("§a. §e. §c."),0,40,0);
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

        int pos = 0;
        for (int i = 0; i < text.length(); i++) {
            pos += 4;
            if (!cryptedText.contains("<")) return;
            String[] split = cryptedText.split("<");
            int timesRemaining = split.length;
            int random = rnd.nextInt(1, timesRemaining);
            split[random] = ">"+split[random];
            cryptedText = String.join("<", split).replaceAll("<>", colorNormal);

            String finalCryptedText = cryptedText.replaceAll("<",colorCrypt);
            TaskScheduler.scheduleTask(pos, () -> {
                PlayerUtils.sendTitleToPlayers(PlayerUtils.getAllPlayers(), Text.literal(finalCryptedText),0,30,20);
            });
        }
    }

    public static void tick() {
        for (Wildcard wildcard : activeWildcards.values()) {
            wildcard.tick();
        }
        if (!activeWildcards.containsKey(Wildcards.SIZE_SHIFTING)) {
            SizeShifting.resetSizesTick();
        }
    }

    public static void tickSessionOn() {
        for (Wildcard wildcard : activeWildcards.values()) {
            wildcard.tick();
        }
    }

    public static void onSessionEnd() {
        if (!activeWildcards.isEmpty()) {
            fadedWildcard();
        }
        for (Wildcard wildcard : activeWildcards.values()) {
            wildcard.deactivate();
        }
        activeWildcards.clear();
    }

    public static void onJump(ServerPlayerEntity player) {
        if (!activeWildcards.containsKey(Wildcards.SIZE_SHIFTING)) return;
        if (activeWildcards.get(Wildcards.SIZE_SHIFTING) instanceof SizeShifting sizeShifting) {
            sizeShifting.onJump(player);
        }
    }
}
