package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.minecraft.server.network.ServerPlayerEntity;

public class SuperpowersWildcard extends Wildcard {
    @Override
    public Wildcards getType() {
        return Wildcards.SUPERPOWERS;
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static void resetAllSuperpowers() {

    }

    public static void rollRandomSuperpowers() {

    }

    public static Superpowers getSuperpower(ServerPlayerEntity player) {
        return Superpowers.NULL;
    }

    public static void setSuperpower(ServerPlayerEntity player, Superpower superpower) {

    }
}
