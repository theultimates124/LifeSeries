package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TimeDilation;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpower;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;

public class TimeControl extends Superpower {
    //TODO sounds
    public TimeControl(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.TIME_CONTROL;
    }

    @Override
    public int getCooldownSeconds() {
        return 300;
    }

    @Override
    public void activate() {
        super.activate();
        float previousSpeed = TimeDilation.getWorldSpeed();
        if (previousSpeed <= 4) return;
        TimeDilation.slowlySetWorldSpeed(4, 20);
        TaskScheduler.scheduleTask(70, () -> TimeDilation.slowlySetWorldSpeed(previousSpeed, 20));
    }
}