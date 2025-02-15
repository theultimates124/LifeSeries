package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

import javax.print.attribute.standard.MediaSize;
import java.util.Iterator;

import static net.mat0u5.lifeseries.Main.*;

public class TimeDilation extends Wildcard {
    public static float MIN_TICK_RATE = 1;
    public static float NORMAL_TICK_RATE = 20;
    public static float MAX_TICK_RATE = 100;
    public static float MIN_PLAYER_MSPT = 25.0F;
    public static int updateRate = 100;
    public static int lastDiv = -1;
    public static int activatedAt = -1;
    public static float weatherTicksBacklog = 0;

    @Override
    public Wildcards getType() {
        return Wildcards.TIME_DILATION;
    }

    @Override
    public void tick() {
        if (server == null) return;
        ServerTickManager serverTickManager = server.getTickManager();
        float rate = serverTickManager.getTickRate();
        if (rate > 20 && server != null) {
            if (rate > 30) {
                adjustCreeperFuseTimes();
            }
            weatherTicksBacklog += (rate-20) / 2.0f;
            int weatherTicks = (int) weatherTicksBacklog;
            if (weatherTicks >= 1) {
                weatherTicksBacklog -= weatherTicks;
                for (ServerWorld serverWorld : server.getWorlds()) {
                    long newTicks = serverWorld.getTimeOfDay() + weatherTicks;
                    serverWorld.setTimeOfDay(newTicks);
                    for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                        player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(serverWorld.getTime(), serverWorld.getTimeOfDay(), serverWorld.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
                    }
                }
            }
        }
    }

    @Override
    public void tickSessionOn() {
        if (!active) return;
        float sessionPassedTime = ((float) currentSession.passedTime - activatedAt);
        if (sessionPassedTime < 0) return;
        if (sessionPassedTime > 3600 && sessionPassedTime < 3700) OtherUtils.executeCommand("weather clear");
        int currentDiv = (int) ((currentSession.passedTime) / updateRate);
        if (lastDiv != currentDiv) {
            lastDiv = currentDiv;

            float progress = ((float) currentSession.passedTime - activatedAt) / (currentSession.sessionLength - activatedAt);
            /*
            if (progress < 0.492f) {
                progress = 0.311774f * (float) Math.pow(progress, 0.7);
            }
            else {
                progress = (float) Math.pow(1.8*progress-0.87f, 3) + 0.19f;
            }
            */
            float rate;
            if (progress < 0.5f) {
                rate = MIN_TICK_RATE + (NORMAL_TICK_RATE - MIN_TICK_RATE) * (progress * 2);
            }
            else {
                rate = NORMAL_TICK_RATE + (MAX_TICK_RATE - NORMAL_TICK_RATE) * (progress * 2 - 1);
            }
            rate = Math.min(rate, MAX_TICK_RATE);
            setWorldSpeed(rate);
        }
    }

    @Override
    public void deactivate() {
        setWorldSpeed(NORMAL_TICK_RATE);
        lastDiv = -1;
        OtherUtils.executeCommand("/execute as @e[type=minecraft:creeper] run data modify entity @s Fuse set value 30s");
        super.deactivate();
    }

    @Override
    public void activate() {
        TaskScheduler.scheduleTask(50, () -> OtherUtils.executeCommand("weather rain"));
        TaskScheduler.scheduleTask(115, () -> {
            activatedAt = (int) currentSession.passedTime + 400;
            lastDiv = -1;
            slowlySetWorldSpeed(MIN_TICK_RATE, 18);
            TaskScheduler.scheduleTask(19, super::activate);
        });
    }

    public static void slowlySetWorldSpeed(float rate, int ticks) {
        if (server == null) return;
        ServerTickManager serverTickManager = server.getTickManager();
        float currentRate = serverTickManager.getTickRate();
        float step = (rate - currentRate) / ((float) ticks);
        for (int i = 0; i < ticks; i++) {
            int finalI = i;
            TaskScheduler.scheduleTask(i, () -> {
                serverTickManager.setTickRate(currentRate + (step * finalI));
            });
        }
    }

    public static void setWorldSpeed(float rate) {
        if (server == null) return;
        ServerTickManager serverTickManager = server.getTickManager();
        serverTickManager.setTickRate(rate);
    }

    private static void adjustCreeperFuseTimes() {
        if (server == null) return;
        ServerTickManager serverTickManager = server.getTickManager();
        float tickRate = serverTickManager.getTickRate();
        short fuseTime = (short) (20 * (tickRate / 20.0f));
        OtherUtils.executeCommand("/execute as @e[type=minecraft:creeper] run data modify entity @s Fuse set value "+fuseTime+"s");
    }
}
