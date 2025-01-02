package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.seriesConfig;

public class LastLifeLivesManager {
    public SessionAction actionChooseLives = new SessionAction(
            OtherUtils.minutesToTicks(1),"§7Assign lives if necessary §f[00:01:00]"
    ) {
        @Override
        public void trigger() {
            assignRandomLivesToUnassignedPlayers();
        }
    };
    Random rnd = new Random();

    public void receiveLifeFromOtherPlayer(Text playerName, ServerPlayerEntity target) {
        target.playSoundToPlayer(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 10, 1);
        target.sendMessage(Text.literal("You received a life from ").append(playerName));
        PlayerUtils.sendTitle(target, Text.of("You received a life"), 10, 30, 10);
        AnimationUtils.createSpiral(target, 175);
        currentSeries.addPlayerLife(target);
    }

    public void assignRandomLivesToUnassignedPlayers() {
        List<ServerPlayerEntity> assignTo = new ArrayList<>();
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (currentSeries.hasAssignedLives(player)) continue;
            assignTo.add(player);
        }
        if (assignTo.isEmpty()) return;
        assignRandomLives(assignTo);
    }

    public void assignRandomLives(Collection<ServerPlayerEntity> players) {
        HashMap<ServerPlayerEntity, Integer> lives = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            if (lives.containsKey(player)) continue;
            lives.put(player,-1);
        }
        PlayerUtils.sendTitleToPlayers(players, Text.literal("You will have...").formatted(Formatting.GRAY), 10, 40, 10);
        int delay = 60;
        TaskScheduler.scheduleTask(delay, ()->{
            lifeRoll( 0, -1, lives);
        });
    }

    public void lifeRoll(int currentStep, int lastNum,  HashMap<ServerPlayerEntity, Integer> lives) {
        int delay = 1;
        if (currentStep >= 30) delay = 2;
        if (currentStep >= 50) delay = 4;
        if (currentStep >= 65) delay = 8;
        if (currentStep >= 75) delay = 20;
        if (currentStep == 80) {
            //Choose the amount of lives a player will have

            int totalSize = lives.size();
            int chosenNotRandomly = 1;
            for (ServerPlayerEntity player : lives.keySet()) {
                Integer currentLives = currentSeries.getPlayerLives(player);
                if (currentLives != null) {
                    lives.put(player, currentLives);
                    continue;
                }
                if (chosenNotRandomly < 6 && totalSize > 6) {
                    chosenNotRandomly++;
                    lives.put(player, chosenNotRandomly);
                    continue;
                }

                int minLives = seriesConfig.getOrCreateInt("default_lives_min", 2);
                int maxLives = seriesConfig.getOrCreateInt("default_lives_max", 6);
                int randomLives = rnd.nextInt(maxLives-1)+minLives;// Random number, default 2->6
                lives.put(player, randomLives);
            }


            //Show the actual amount of lives for one cycle
            for (Map.Entry<ServerPlayerEntity, Integer> playerEntry : lives.entrySet()) {
                Integer livesNum = playerEntry.getValue();
                ServerPlayerEntity player = playerEntry.getKey();
                Text textLives = currentSeries.getFormattedLives(livesNum);
                PlayerUtils.sendTitle(player, textLives, 0, 25, 0);
            }
            PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
            TaskScheduler.scheduleTask(delay, ()->{
                lifeRoll( currentStep+1, -1, lives);
            });
            return;
        }
        if (currentStep == 81) {
            //Show "x lives." screen
            for (Map.Entry<ServerPlayerEntity, Integer> playerEntry : lives.entrySet()) {
                Integer livesNum = playerEntry.getValue();
                ServerPlayerEntity player = playerEntry.getKey();
                MutableText textLives = currentSeries.getFormattedLives(livesNum).copy();
                Text finalText = textLives.append(Text.literal(" lives.").formatted(Formatting.GREEN));
                PlayerUtils.sendTitle(player, finalText, 0, 60, 20);
                if (currentSeries.hasAssignedLives(player)) continue;
                currentSeries.setPlayerLives(player, livesNum);
            }
            PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.BLOCK_END_PORTAL_SPAWN);
            currentSeries. reloadAllPlayerTeams();
            return;
        }
        int minLives = seriesConfig.getOrCreateInt("default_lives_min", 2);
        int maxLives = seriesConfig.getOrCreateInt("default_lives_max", 6);
        int displayLives = rnd.nextInt(maxLives-1)+minLives;// Random number, default 2->6
        while (displayLives == lastNum) {
            // Just so that the random cycle can't have two of the same number in a row
            displayLives = rnd.nextInt(maxLives-1)+minLives;
        }
        int finalDisplayLives = displayLives;
        PlayerUtils.sendTitleToPlayers(lives.keySet(), currentSeries.getFormattedLives(finalDisplayLives), 0, 25, 0);
        PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
        TaskScheduler.scheduleTask(delay, ()->{
            lifeRoll( currentStep+1, finalDisplayLives, lives);
        });
    }
}
