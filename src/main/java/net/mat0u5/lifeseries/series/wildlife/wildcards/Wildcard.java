package net.mat0u5.lifeseries.series.wildlife.wildcards;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.wildlife.WildLife;

import static net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager.getSeries;

public abstract class Wildcard {

    public boolean active = false;

    public abstract Wildcards getType();

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

    public void tickSessionOn() {}
    public void tick() {}

}
