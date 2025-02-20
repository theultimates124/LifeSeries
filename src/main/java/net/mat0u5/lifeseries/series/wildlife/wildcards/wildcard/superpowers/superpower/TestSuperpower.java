package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpower;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.server.network.ServerPlayerEntity;

public class TestSuperpower extends Superpower {

    public TestSuperpower(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.TEST;
    }

    @Override
    public void onKeyPressed() {
        OtherUtils.log("Test superpower key pressed.");
    }
}
