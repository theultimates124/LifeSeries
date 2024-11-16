package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TitleCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class LastLife extends Series {
    Random rnd = new Random();
    @Override
    public SeriesList getSeries() {
        return SeriesList.LAST_LIFE;
    }
    public void receiveLifeFromOtherPlayer(MinecraftServer server, Text playerName, ServerPlayerEntity target) {
        target.playSoundToPlayer(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 10, 1);
        target.sendMessage(Text.literal("You received a life from ").append(playerName));
        PlayerUtils.sendTitle(target, Text.of("You received a life"), 10, 30, 10);
        AnimationUtils.createSpiral(server, target);
        currentSeries.addPlayerLife(server,target);
    }
    public void assignRandomLives(MinecraftServer server, Collection<ServerPlayerEntity> players) {
        HashMap<ServerPlayerEntity, Integer> lives = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            int playerLives = rnd.nextInt(5)+2;// Random number from 2->6
            lives.put(player,playerLives);
        }
        PlayerUtils.sendTitleToPlayers(players, Text.literal("You will have...").formatted(Formatting.GRAY), 10, 40, 10);
        int delay = 60;
        TaskScheduler.scheduleTask(delay, ()->{
            lifeRoll(server, 0, -1, lives);
        });
    }
    public void lifeRoll(MinecraftServer server, int currentStep, int lastNum,  HashMap<ServerPlayerEntity, Integer> lives) {
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
                Text textLives = Text.literal(String.valueOf(livesNum)).formatted(getColorForLivesNum(livesNum));
                PlayerUtils.sendTitle(player, textLives, 0, 25, 0);
            }
            PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
            TaskScheduler.scheduleTask(delay, ()->{
                lifeRoll(server, currentStep+1, -1, lives);
            });
            return;
        }
        if (currentStep == 81) {
            //Show "x lives." screen
            for (Map.Entry<ServerPlayerEntity, Integer> playerEntry : lives.entrySet()) {
                Integer livesNum = playerEntry.getValue();
                ServerPlayerEntity player = playerEntry.getKey();
                MutableText textLives = Text.literal(String.valueOf(livesNum)).formatted(getColorForLivesNum(livesNum));
                Text finalText = textLives.append(Text.literal(" lives.").formatted(Formatting.GREEN));
                PlayerUtils.sendTitle(player, finalText, 0, 60, 20);
                setPlayerLives(server,player, livesNum);
            }
            PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.BLOCK_END_PORTAL_SPAWN);
            return;
        }
        int displayLives = rnd.nextInt(5)+2;// Random number from 2->6
        while (displayLives == lastNum) {
            // Just so that the random cycle can't have two of the same number in a row
            displayLives = rnd.nextInt(5)+2;
        }
        int finalDisplayLives = displayLives;
        PlayerUtils.sendTitleToPlayers(lives.keySet(), Text.literal(String.valueOf(finalDisplayLives)).formatted(getColorForLivesNum(finalDisplayLives)), 0, 25, 0);
        PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
        TaskScheduler.scheduleTask(delay, ()->{
            lifeRoll(server, currentStep+1, finalDisplayLives, lives);
        });
    }
    public Formatting getColorForLivesNum(int lives) {
        if (lives == 1) return Formatting.DARK_RED;
        if (lives == 2) return Formatting.YELLOW;
        if (lives == 3) return Formatting.GREEN;
        if (lives >= 4) return Formatting.DARK_GREEN;
        return Formatting.GRAY;
    }
}
