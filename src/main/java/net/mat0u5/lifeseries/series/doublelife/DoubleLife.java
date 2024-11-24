package net.mat0u5.lifeseries.series.doublelife;

import net.mat0u5.lifeseries.config.DatabaseManager;
import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

import static net.mat0u5.lifeseries.Main.server;

public class DoubleLife extends Series {
    public SessionAction actionChooseSoulmates = new SessionAction(OtherUtils.minutesToTicks(1)) {
        @Override
        public void trigger() {
            rollSoulmates();
        }
    };

    public Map<UUID, UUID> soulmates = new HashMap<>();

    @Override
    public SeriesList getSeries() {
        return SeriesList.DOUBLE_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return new DoubleLifeBlacklist();
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        if (!hasAssignedLives(player)) {
            setPlayerLives(player,4);
        }
        reloadPlayerTeam(player);
    }
    @Override
    public void sessionStart() {
        activeActions = List.of(actionChooseSoulmates);
    }


    public void loadSoulmates() {
        soulmates = DatabaseManager.getAllSoulmates();
    }
    public void saveSoulmates() {
        DatabaseManager.deleteDoubleLifeSoulmates();
        DatabaseManager.setAllSoulmates(soulmates);
    }
    public boolean hasSoulmate(ServerPlayerEntity player) {
        if (player == null) return false;
        UUID playerUUID = player.getUuid();
        if (!soulmates.containsKey(playerUUID)) return false;
        return true;
    }
    public boolean isSoulmateOnline(ServerPlayerEntity player) {
        if (!hasSoulmate(player)) return false;
        UUID soulmateUUID = soulmates.get(player.getUuid());
        return server.getPlayerManager().getPlayer(soulmateUUID) != null;
    }
    public ServerPlayerEntity getSoulmate(ServerPlayerEntity player) {
        if (!isSoulmateOnline(player)) return null;
        UUID soulmateUUID = soulmates.get(player.getUuid());
        return server.getPlayerManager().getPlayer(soulmateUUID);
    }
    public void setSoulmate(ServerPlayerEntity player1, ServerPlayerEntity player2) {
        soulmates.put(player1.getUuid(), player2.getUuid());
        soulmates.put(player2.getUuid(), player1.getUuid());
    }
    public void resetSoulmate(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        Map<UUID, UUID> newSoulmates = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : soulmates.entrySet()) {
            if (entry.getKey().equals(playerUUID)) continue;
            if (entry.getValue().equals(playerUUID)) continue;
            newSoulmates.put(entry.getKey(), entry.getValue());
        }
        soulmates = newSoulmates;
    }
    public void resetAllSoulmates() {
        soulmates = new HashMap<>();
    }

    public void rollSoulmates() {
        List<ServerPlayerEntity> playersToRoll = getNonAssignedPlayers();
        PlayerUtils.playSoundToPlayers(playersToRoll, SoundEvents.UI_BUTTON_CLICK.value());
        PlayerUtils.sendTitleToPlayers(playersToRoll, Text.literal("3").formatted(Formatting.GREEN),5,20,5);
        TaskScheduler.scheduleTask(25, () -> {
            PlayerUtils.playSoundToPlayers(playersToRoll, SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(playersToRoll, Text.literal("2").formatted(Formatting.GREEN),5,20,5);
        });
        TaskScheduler.scheduleTask(50, () -> {
            PlayerUtils.playSoundToPlayers(playersToRoll, SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(playersToRoll, Text.literal("1").formatted(Formatting.GREEN),5,20,5);
        });
        TaskScheduler.scheduleTask(75, () -> {
            PlayerUtils.sendTitleToPlayers(playersToRoll, Text.literal("Your soulmate is...").formatted(Formatting.GREEN),10,50,20);
            PlayerUtils.playSoundToPlayers(playersToRoll, SoundEvent.of(Identifier.of("minecraft","doublelife_soulmate_wait")));
        });
        TaskScheduler.scheduleTask(165, () -> {
            PlayerUtils.sendTitleToPlayers(playersToRoll, Text.literal("????").formatted(Formatting.GREEN),20,60,20);
            PlayerUtils.playSoundToPlayers(playersToRoll, SoundEvent.of(Identifier.of("minecraft","doublelife_soulmate_chosen")));
            chooseRandomSoulmates();
        });
    }
    public List<ServerPlayerEntity> getNonAssignedPlayers() {
        List<ServerPlayerEntity> playersToRoll = new ArrayList<>();
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (hasSoulmate(player)) continue;
            playersToRoll.add(player);
        }
        return playersToRoll;
    }

    public void chooseRandomSoulmates() {
        List<ServerPlayerEntity> playersToRoll = getNonAssignedPlayers();
        Collections.shuffle(playersToRoll);
        if (playersToRoll.size()%2 != 0) {
            ServerPlayerEntity remove = playersToRoll.getFirst();
            playersToRoll.remove(remove);
            OtherUtils.broadcastMessageToAdmins(Text.literal(" [DoubleLife] ").append(remove.getStyledDisplayName()).append(" was not paired with anyone, as there is an odd number of players online."));
        }
        while(!playersToRoll.isEmpty()) {
            ServerPlayerEntity player1 = playersToRoll.get(0);
            ServerPlayerEntity player2 = playersToRoll.get(1);
            setSoulmate(player1,player2);
            playersToRoll.removeFirst();
            playersToRoll.removeFirst();
        }
        saveSoulmates();
    }
}
