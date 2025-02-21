package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public abstract class Superpower {
    public boolean active = false;
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

    public void tick() {}
    public void playerUseItem() {}

    public void onKeyPressed() {
        if (System.currentTimeMillis() < cooldown) {
            sendCooldownPacket();
            return;
        }
        activate();
    }

    public void activate() {
        active = true;
        cooldown(getCooldownSeconds());
    }

    public void deactivate() {
        active = false;
    }

    public void turnOff() {
        //Fully deactivate superpower.
        deactivate();
    }

    public void cooldown(int seconds) {
        cooldown = System.currentTimeMillis()+ (seconds* 1000L);
        if (System.currentTimeMillis() < (cooldown - 100)) {
            sendCooldownPacket();
        }
    }

    public void sendCooldownPacket() {
        NetworkHandlerServer.sendLongPacket(getPlayer(), "superpower_cooldown", cooldown);
    }
}
