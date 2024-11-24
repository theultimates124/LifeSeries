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
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class LastLifeLives {
    public SessionAction actionChooseLives = new SessionAction(OtherUtils.minutesToTicks(1)) {
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
        AnimationUtils.createSpiral(target);
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
        if (players.size() > 5) {
            List<ServerPlayerEntity> playersCopy = new ArrayList<>(players.stream().toList());
            Collections.shuffle(playersCopy);
            lives.put(playersCopy.get(0), 2);
            lives.put(playersCopy.get(1), 3);
            lives.put(playersCopy.get(2), 4);
            lives.put(playersCopy.get(3), 5);
            lives.put(playersCopy.get(4), 6);
        }
        for (ServerPlayerEntity player : players) {
            if (lives.containsKey(player)) continue;
            int playerLives = rnd.nextInt(5)+2;// Random number from 2->6
            lives.put(player,playerLives);
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
        int displayLives = rnd.nextInt(5)+2;// Random number from 2->6
        while (displayLives == lastNum) {
            // Just so that the random cycle can't have two of the same number in a row
            displayLives = rnd.nextInt(5)+2;
        }
        int finalDisplayLives = displayLives;
        PlayerUtils.sendTitleToPlayers(lives.keySet(), currentSeries.getFormattedLives(finalDisplayLives), 0, 25, 0);
        PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
        TaskScheduler.scheduleTask(delay, ()->{
            lifeRoll( currentStep+1, finalDisplayLives, lives);
        });
    }
}
