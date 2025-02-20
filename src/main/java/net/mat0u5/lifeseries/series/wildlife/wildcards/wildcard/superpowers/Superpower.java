package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public abstract class Superpower {
    public boolean active = true;
    public long cooldown = 0;
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

    public int getCooldownSeconds() {
        return 1;
    }

    public void tick() {
    }

    public void onKeyPressed() {
        if (System.currentTimeMillis() < cooldown) {
            cooldownFailed();
            return;
        }
        activate();
    }

    public void cooldownFailed() {
        NetworkHandlerServer.sendLongPacket(getPlayer(), "superpower_cooldown", cooldown);
    }

    public void activate() {
        cooldown = System.currentTimeMillis()+ (getCooldownSeconds()* 1000L);
        active = true;
        NetworkHandlerServer.sendLongPacket(getPlayer(), "superpower_cooldown", cooldown);
    }

    public void deactivate() {
        active = false;
    }

    public void turnOff() {
        //Fully deactivate superpower.
    }
}
