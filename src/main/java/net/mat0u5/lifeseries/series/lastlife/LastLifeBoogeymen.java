package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class LastLifeBoogeymen {

    public SessionAction actionBoogeymanWarn1 = new SessionAction(OtherUtils.minutesToTicks(2)) {
        @Override
        public void trigger() {
            if (boogeymanChosen) return;
            OtherUtils.broadcastMessage(Text.literal("The boogeyman is being chosen in 3 minutes.").formatted(Formatting.RED));
        }
    };
    public SessionAction actionBoogeymanWarn2 = new SessionAction(OtherUtils.minutesToTicks(4)) {
        @Override
        public void trigger() {
            if (boogeymanChosen) return;
            OtherUtils.broadcastMessage(Text.literal("The boogeyman is being chosen in 1 minute.").formatted(Formatting.RED));
        }
    };
    public SessionAction actionBoogeymanChoose = new SessionAction(OtherUtils.minutesToTicks(5)) {
        @Override
        public void trigger() {
            if (boogeymanChosen) return;
            OtherUtils.broadcastMessage(Text.literal("The boogeyman is about to be chosen.").formatted(Formatting.RED));
            TaskScheduler.scheduleTask(100, () -> {
                chooseBoogeymen();
            });
        }
    };
    public List<Boogeyman> boogeymen = new ArrayList<>();
    public boolean boogeymanChosen = false;
    public boolean isBoogeyman(ServerPlayerEntity player) {
        if (player == null) return false;
        for (Boogeyman boogeyman : boogeymen) {
            if (boogeyman.uuid.equals(player.getUuid())) {
                return true;
            }
        }
        return false;
    }
    public Boogeyman getBoogeyman(ServerPlayerEntity player) {
        if (player == null) return null;
        for (Boogeyman boogeyman : boogeymen) {
            if (boogeyman.uuid.equals(player.getUuid())) {
                return boogeyman;
            }
        }
        return null;
    }
    public void addBoogeyman(ServerPlayerEntity player) {
        Boogeyman newBoogeyman = new Boogeyman(player);
        boogeymen.add(newBoogeyman);
        boogeymanChosen = true;
    }
    public void addBoogeymanManually(ServerPlayerEntity player) {
        addBoogeyman(player);
        player.sendMessage(Text.of("§c [NOTICE] You are now a boogeyman!"));
    }
    public void removeBoogeymanManually(ServerPlayerEntity player) {
        Boogeyman boogeyman = getBoogeyman(player);
        if (boogeyman == null) return;
        boogeymen.remove(boogeyman);
        if (boogeymen.isEmpty()) boogeymanChosen = false;
        player.sendMessage(Text.of("§c [NOTICE] You are no longer a boogeyman!"));
    }
    public void resetBoogeymen() {
        for (Boogeyman boogeyman : boogeymen) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(boogeyman.uuid);
            if (player == null) continue;
            player.sendMessage(Text.of("§c [NOTICE] You are no longer a boogeyman!"));
        }
        boogeymen = new ArrayList<>();
        boogeymanChosen = false;
    }
    public void cure(ServerPlayerEntity player) {
        Boogeyman boogeyman = getBoogeyman(player);
        if (boogeymen == null) return;
        boogeyman.cured = true;
        PlayerUtils.sendTitle(player,Text.of("§aYou are cured!"), 20, 30, 20);
    }
    public void chooseBoogeymen() {
        resetBoogeymen();
        PlayerUtils.playSoundToPlayers(((LastLife)currentSeries).getNonRedPlayers(), SoundEvents.UI_BUTTON_CLICK.value());
        PlayerUtils.sendTitleToPlayers(((LastLife)currentSeries).getNonRedPlayers(), Text.literal("3").formatted(Formatting.GREEN),0,35,0);

        TaskScheduler.scheduleTask(30, () -> {
            PlayerUtils.playSoundToPlayers(((LastLife)currentSeries).getNonRedPlayers(), SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(((LastLife)currentSeries).getNonRedPlayers(), Text.literal("2").formatted(Formatting.YELLOW),0,35,0);
        });
        TaskScheduler.scheduleTask(60, () -> {
            PlayerUtils.playSoundToPlayers(((LastLife)currentSeries).getNonRedPlayers(), SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(((LastLife)currentSeries).getNonRedPlayers(), Text.literal("1").formatted(Formatting.RED),0,35,0);
        });
        TaskScheduler.scheduleTask(90, () -> {
            PlayerUtils.sendTitleToPlayers(((LastLife)currentSeries).getNonRedPlayers(), Text.literal("You are...").formatted(Formatting.YELLOW),10,50,20);
        });
        TaskScheduler.scheduleTask(180, this::boogeymenChooseRandom);
    }
    public void boogeymenChooseRandom() {
        List<ServerPlayerEntity> nonRedPlayers = ((LastLife)currentSeries).getNonRedPlayers();
        Collections.shuffle(nonRedPlayers);
        if (nonRedPlayers.isEmpty()) return;

        List<ServerPlayerEntity> normalPlayers = new ArrayList<>();
        List<ServerPlayerEntity> boogeyPlayers = new ArrayList<>();
        double currentChance = 100;
        for (ServerPlayerEntity player : nonRedPlayers) {
            double currentRoll = Math.random()*100;
            if (currentChance >= currentRoll && currentChance != 0) {
                boogeyPlayers.add(player);
            }
            else {
                normalPlayers.add(player);
                currentChance = 0;
            }

            if (currentChance != 0) {
                currentChance/=2;
            }
        }
        PlayerUtils.sendTitleToPlayers(normalPlayers, Text.literal("NOT the Boogeyman").formatted(Formatting.GREEN),10,50,20);
        PlayerUtils.sendTitleToPlayers(boogeyPlayers, Text.literal("The Boogeyman").formatted(Formatting.RED),10,50,20);
        for (ServerPlayerEntity boogey : boogeyPlayers) {
            addBoogeyman(boogey);
            boogey.sendMessage(Text.of("You are the boogeyman. You must by any means necessary kill a §2dark green§7, §agreen§7 or §eyellow§7 name by direct action to be cured of the curse. " +
                    "If you fail, next session you will become a §cred name§7. All loyalties and friendships are removed while you are the boogeyman."));
        }
    }
    public void sessionEnd() {
        for (Boogeyman boogeyman : boogeymen) {
            if (boogeyman.died) return;

            if (!boogeyman.cured) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(boogeyman.uuid);
                if (player == null) {
                    OtherUtils.broadcastMessageToAdmins(Text.of("§c[LastLife] The boogeyman ("+boogeyman.name+") has failed to kill a person, and is offline at session end. " +
                            "That means their lives have not been set to 1. You must do this manually once they are online again."));
                    return;
                }
                currentSeries.setPlayerLives(player, 1);
                player.sendMessage(Text.of("§7You failed to kill a green or yellow name last session as the boogeyman. As punishment, you have dropped to your §cLast Life§7. " +
                        "All alliances are severed and you are now hostile to all players. You may team with others on their Last Life if you wish."));
            }
        }
    }
    public void playerLostAllLives(ServerPlayerEntity player) {
        Boogeyman boogeyman = getBoogeyman(player);
        if (boogeyman == null) return;
        boogeyman.died = true;
    }
}
