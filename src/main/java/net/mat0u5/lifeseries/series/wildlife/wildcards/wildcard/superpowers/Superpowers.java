package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import java.util.ArrayList;
import java.util.List;

public enum Superpowers {
    NULL,

    TIME_CONTROL,
    CREAKING,
    WIND_CHARGE,
    ASTRAL_PROJECTION,
    SUPER_PUNCH,
    MIMICRY,
    TELEPORTATION,
    LISTENING,
    SHADOW_PLAY,
    FLIGHT,
    PLAYER_DISGUISE,
    ANIMAL_DISGUISE,
    TRIPLE_JUMP,
    INVISIBILITY,
    SUPERSPEED,
    NECROMANCY;

    public static List<Superpowers> getImplemented() {
        return List.of();
    }

    public static List<String> getImplementedStr() {
        List<String> result = new ArrayList<>();
        for (Superpowers superpower : getImplemented()) {
            result.add(getString(superpower));
        }
        return result;
    }

    public static Superpower getInstance(Superpowers superpower) {
        return new Superpower();
    }

    public static String getString(Superpowers superpower) {
        return superpower.toString().toLowerCase();
    }

    public static Superpowers fromString(String superpower) {
        return Superpowers.valueOf(Superpowers.class, superpower.toUpperCase());
    }
}
