package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class Boogeyman {
    public UUID uuid;
    public String name;
    public boolean cured = false;
    public boolean died = false;

    public Boogeyman(ServerPlayerEntity player) {
        uuid = player.getUuid();
        name = player.getNameForScoreboard();
    }

    public ServerPlayerEntity getPlayer() {
        return PlayerUtils.getPlayer(uuid);
    }
}
