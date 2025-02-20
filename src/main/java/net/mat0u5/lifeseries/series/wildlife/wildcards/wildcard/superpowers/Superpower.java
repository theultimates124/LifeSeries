package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public abstract class Superpower {
    private final UUID playerUUID;
    public Superpower(ServerPlayerEntity player) {
        playerUUID = player.getUuid();
    }

    @Nullable
    public ServerPlayerEntity getPlayer() {
        if (server == null) return null;
        if (playerUUID == null) return null;
        return server.getPlayerManager().getPlayer(playerUUID);
    }

    public abstract Superpowers getSuperpower();
    public abstract void onKeyPressed();
}
