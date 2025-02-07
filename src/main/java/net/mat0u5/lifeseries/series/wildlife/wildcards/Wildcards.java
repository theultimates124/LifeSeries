package net.mat0u5.lifeseries.series.wildlife.wildcards;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.*;

import java.util.ArrayList;
import java.util.List;

public enum Wildcards {
    NULL,
    SIZE_SHIFTING,
    HUNGER,
    SNAILS,
    TIME_DILATION,
    TRIVIA_BOT,
    MOB_SWAP,
    SUPERPOWERS,
    CALLBACK;

    public static Wildcards getFromString(String wildcard) {
        if (wildcard.equalsIgnoreCase("size_shifting")) return SIZE_SHIFTING;
        if (wildcard.equalsIgnoreCase("hunger")) return HUNGER;
        if (wildcard.equalsIgnoreCase("snails")) return SNAILS;
        if (wildcard.equalsIgnoreCase("time_dilation")) return TIME_DILATION;
        if (wildcard.equalsIgnoreCase("trivia_bot")) return TRIVIA_BOT;
        if (wildcard.equalsIgnoreCase("mob_swap")) return MOB_SWAP;
        if (wildcard.equalsIgnoreCase("superpowers")) return SUPERPOWERS;
        if (wildcard.equalsIgnoreCase("callback")) return CALLBACK;
        return NULL;
    }

    public static Wildcard getInstance(Wildcards wildcard) {
        if (wildcard == Wildcards.SIZE_SHIFTING) return new SizeShifting();
        if (wildcard == Wildcards.HUNGER) return new Hunger();
        if (wildcard == Wildcards.SNAILS) return new Snails();
        if (wildcard == Wildcards.TIME_DILATION) return new TimeDilation();
        if (wildcard == Wildcards.TRIVIA_BOT) return null;
        if (wildcard == Wildcards.MOB_SWAP) return new MobSwap();
        if (wildcard == Wildcards.SUPERPOWERS) return null;
        if (wildcard == Wildcards.CALLBACK) return null;
        return null;
    }

    public static String getStringName(Wildcards wildcard) {
        if (wildcard == Wildcards.SIZE_SHIFTING) return "size_shifting";
        if (wildcard == Wildcards.HUNGER) return "hunger";
        if (wildcard == Wildcards.SNAILS) return "snails";
        if (wildcard == Wildcards.TIME_DILATION) return "time_dilation";
        if (wildcard == Wildcards.TRIVIA_BOT) return "trivia_bot";
        if (wildcard == Wildcards.MOB_SWAP) return "mob_swap";
        if (wildcard == Wildcards.SUPERPOWERS) return "superpowers";
        if (wildcard == Wildcards.CALLBACK) return "callback";
        return "null";
    }

    public static List<String> getWildcards() {
        return List.of("size_shifting","hunger","snails","time_dilation","trivia_bot","mob_swap","superpowers","callback");
    }

    public static List<String> getNonActiveWildcards() {
        List<String> result = new ArrayList<>(getWildcards());
        for (Wildcards wildcard : WildcardManager.activeWildcards.keySet()) {
            String name = getStringName(wildcard);
            result.remove(name);
        }
        return result;
    }

    public static List<String> getActiveWildcards() {
        List<String> result = new ArrayList<>();
        for (Wildcards wildcard : WildcardManager.activeWildcards.keySet()) {
            String name = getStringName(wildcard);
            result.add(name);
        }
        return result;
    }
}
