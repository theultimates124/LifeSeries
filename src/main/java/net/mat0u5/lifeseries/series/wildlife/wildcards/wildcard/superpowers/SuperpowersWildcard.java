package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class SuperpowersWildcard extends Wildcard {
    public static HashMap<UUID, Superpower> playerSuperpowers = new HashMap<>();

    @Override
    public Wildcards getType() {
        return Wildcards.SUPERPOWERS;
    }

    @Override
    public void activate() {
        playerSuperpowers.clear();
        super.activate();
    }

    @Override
    public void deactivate() {
        playerSuperpowers.clear();
        super.deactivate();
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static void resetAllSuperpowers() {
        playerSuperpowers.clear();
    }

    public static void rollRandomSuperpowers() {
        HashMap<UUID, Superpower> newPowers = new HashMap<>();
        List<Superpowers> implemented = Superpowers.getImplemented();
        int pos = 0;
        List<ServerPlayerEntity> allPlayers = currentSeries.getAlivePlayers();
        Collections.shuffle(allPlayers);
        for (ServerPlayerEntity player : allPlayers) {
            Superpowers power = implemented.get(pos%implemented.size());
            Superpower instance = Superpowers.getInstance(player, power);
            newPowers.put(player.getUuid(), instance);
            pos++;
        }
        playerSuperpowers = newPowers;
    }

    public static Superpowers getSuperpower(ServerPlayerEntity player) {
        if (playerSuperpowers.containsKey(player.getUuid())) {
            return playerSuperpowers.get(player.getUuid()).getSuperpower();
        }
        return Superpowers.NONE;
    }

    public static void setSuperpower(ServerPlayerEntity player, Superpowers superpower) {
        playerSuperpowers.put(player.getUuid(), Superpowers.getInstance(player, superpower));
    }

    public static void pressedSuperpowerKey(ServerPlayerEntity player) {
        OtherUtils.log(player.getNameForScoreboard() + " pressed superpower key");
        if (playerSuperpowers.containsKey(player.getUuid())) {
            playerSuperpowers.get(player.getUuid()).onKeyPressed();
        }
    }
}
