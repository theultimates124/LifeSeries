package net.mat0u5.lifeseries.series;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

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
        if (server == null) return null;
        if (uuid == null) return null;
        return server.getPlayerManager().getPlayer(uuid);
    }
}
