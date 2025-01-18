package net.mat0u5.lifeseries.series.wildlife;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.wildlife.wildcards.SizeShifting;

import static net.mat0u5.lifeseries.Main.currentSeries;

public abstract class Wildcard {
    public boolean active = false;
    public static WildLife getSeries() {
        if (currentSeries instanceof WildLife wildLife) {
            return (WildLife) currentSeries;
        }
        else {
            return null;
        }
    }

    public abstract Wildcards getType();

    public static void chooseWildcards() {
        WildLife series = getSeries();
        if (series == null) return;
        //TODO
        series.activeWildcards.put(Wildcards.SIZE_SHIFTING, new SizeShifting());
    }

    public void activate() {
        WildLife series = getSeries();
        if (series == null) return;
        active = true;
        Main.LOGGER.info("[WildLife] Activated Wildcard: " + getType());
    }

    public void deactivate() {
        WildLife series = getSeries();
        if (series == null) return;
        active = false;
        Main.LOGGER.info("[WildLife] Dectivated Wildcard: " + getType());
    }

    public void onTick() {}
}
