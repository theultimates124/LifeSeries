package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.server.ServerTickManager;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.*;

public class TimeDilation extends Wildcard {
    public static float MIN_TICK_RATE = 1;
    public static float NORMAL_TICK_RATE = 20;
    public static float MAX_TICK_RATE = 100;
    public static float MIN_PLAYER_MSPT = 25.0F;
    public static int updateRate = 100;
    public static int lastDiv = -1;
    public static int activatedAt = -1;

    @Override
    public Wildcards getType() {
        return Wildcards.TIME_DILATION;
    }

    @Override
    public void tickSessionOn() {
        int currentDiv = (int) ((currentSession.passedTime) / updateRate);
        if (lastDiv != currentDiv) {
            lastDiv = currentDiv;

            float progress = ((float) currentSession.passedTime - activatedAt) / (currentSession.sessionLength - activatedAt);
            if (progress < 0.492f) {
                progress = 0.311774f * (float) Math.pow(progress, 0.7);
            }
            else {
                progress = (float) Math.pow(1.8*progress-0.87f, 3) + 0.19f;
            }
            float rate = MIN_TICK_RATE + (MAX_TICK_RATE - MIN_TICK_RATE) * progress;
            rate = Math.min(rate, MAX_TICK_RATE);
            setWorldSpeed(rate);
        }
    }

    @Override
    public void deactivate() {
        setWorldSpeed(NORMAL_TICK_RATE);
        lastDiv = -1;
        super.deactivate();
    }

    @Override
    public void activate() {
        activatedAt = (int) currentSession.passedTime;
        lastDiv = -1;
        setWorldSpeed(MIN_TICK_RATE);
        super.activate();
    }

    public static void setWorldSpeed(float rate) {
        if (server == null) return;
        ServerTickManager serverTickManager = server.getTickManager();
        serverTickManager.setTickRate(rate);
    }
}
