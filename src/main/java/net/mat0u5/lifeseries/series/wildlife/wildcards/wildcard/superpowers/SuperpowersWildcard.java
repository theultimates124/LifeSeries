package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

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
        resetAllSuperpowers();
        super.activate();
    }

    @Override
    public void deactivate() {
        resetAllSuperpowers();
        super.deactivate();
    }

    @Override
    public void softTick() {
        playerSuperpowers.values().forEach(Superpower::tick);
        super.softTick();
    }

    public static void resetAllSuperpowers() {
        playerSuperpowers.values().forEach(Superpower::turnOff);
        playerSuperpowers.clear();
    }

    public static void rollRandomSuperpowers() {
        resetAllSuperpowers();
        List<Superpowers> implemented = Superpowers.getImplemented();
        int pos = 0;
        List<ServerPlayerEntity> allPlayers = currentSeries.getAlivePlayers();
        Collections.shuffle(allPlayers);
        for (ServerPlayerEntity player : allPlayers) {
            Superpowers power = implemented.get(pos%implemented.size());
            Superpower instance = Superpowers.getInstance(player, power);
            playerSuperpowers.put(player.getUuid(), instance);
            pos++;
        }
        PlayerUtils.playSoundToPlayers(allPlayers, SoundEvent.of(Identifier.of("minecraft","wildlife_superpowers")));
    }

    public static Superpowers getSuperpower(ServerPlayerEntity player) {
        if (playerSuperpowers.containsKey(player.getUuid())) {
            return playerSuperpowers.get(player.getUuid()).getSuperpower();
        }
        return Superpowers.NONE;
    }

    public static void setSuperpower(ServerPlayerEntity player, Superpowers superpower) {
        if (playerSuperpowers.containsKey(player.getUuid())) {
            playerSuperpowers.get(player.getUuid()).turnOff();
        }
        playerSuperpowers.put(player.getUuid(), Superpowers.getInstance(player, superpower));
        PlayerUtils.playSoundToPlayers(List.of(player), SoundEvent.of(Identifier.of("minecraft","wildlife_superpowers")));
    }

    public static void pressedSuperpowerKey(ServerPlayerEntity player) {
        if (playerSuperpowers.containsKey(player.getUuid())) {
            playerSuperpowers.get(player.getUuid()).onKeyPressed();
        }
    }

    public static boolean hasActivePower(ServerPlayerEntity player, Superpowers superpower) {
        if (!playerSuperpowers.containsKey(player.getUuid())) return false;
        return playerSuperpowers.get(player.getUuid()).getSuperpower() == superpower;
    }
}
