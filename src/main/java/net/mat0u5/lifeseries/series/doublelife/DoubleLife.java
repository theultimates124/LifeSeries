package net.mat0u5.lifeseries.series.doublelife;

import net.mat0u5.lifeseries.config.DatabaseManager;
import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public class DoubleLife extends Series {

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
}
