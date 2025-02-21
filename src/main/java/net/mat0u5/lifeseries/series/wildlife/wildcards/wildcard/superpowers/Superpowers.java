package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower.Creaking;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower.TimeControl;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum Superpowers {
    NONE,

    TIME_CONTROL,
    CREAKING,
    WIND_CHARGE,
    ASTRAL_PROJECTION,
    SUPER_PUNCH,
    MIMICRY,
    TELEPORTATION,
    //LISTENING,
    SHADOW_PLAY,
    FLIGHT,
    PLAYER_DISGUISE,
    ANIMAL_DISGUISE,
    TRIPLE_JUMP,
    INVISIBILITY,
    SUPERSPEED,
    NECROMANCY;

    public static List<Superpowers> getImplemented() {
        return List.of(TIME_CONTROL, CREAKING);
    }

    public static List<String> getImplementedStr() {
        List<String> result = new ArrayList<>();
        for (Superpowers superpower : getImplemented()) {
            result.add(getString(superpower));
        }
        return result;
    }
    @Nullable
    public static Superpower getInstance(ServerPlayerEntity player, Superpowers superpower) {
        if (superpower == TIME_CONTROL) return new TimeControl(player);
        if (superpower == CREAKING) return new Creaking(player);
        return null;
    }

    public static String getString(Superpowers superpower) {
        return superpower.toString().toLowerCase();
    }

    public static Superpowers fromString(String superpower) {
        return Superpowers.valueOf(Superpowers.class, superpower.toUpperCase());
    }
}
