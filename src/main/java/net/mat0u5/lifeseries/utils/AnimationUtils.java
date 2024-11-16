package net.mat0u5.lifeseries.utils;

import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

public class AnimationUtils {
    public static void playTotemAnimation(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte) 35));
    }

    public static void createSpiral(MinecraftServer server, ServerPlayerEntity player) {
        TaskScheduler.scheduleTask(1, () -> startSpiral(player, server));
    }

    // Method that starts the spiral task for the player
    private static void startSpiral(ServerPlayerEntity player, MinecraftServer server) {
        TaskScheduler.scheduleTask(1, () -> runSpiralStep(player, 0, server));
    }

    // Method that runs each step of the spiral
    private static void runSpiralStep(ServerPlayerEntity player, int step, MinecraftServer server) {
        if (player == null) return;

        processSpiral(player, step);
        processSpiral(player, step+1);
        processSpiral(player, step+2);
        processSpiral(player, step+3);

        if (step <= 175) {
            TaskScheduler.scheduleTask(1, () -> runSpiralStep(player, step + 4, server));
        }
    }

    private static void processSpiral(ServerPlayerEntity player, int step) {
        ServerWorld world = player.getServerWorld();
        double x = player.getX();
        double z = player.getZ();
        double yStart = player.getY();
        double height = 1.0;
        double radius = 0.8;
        int pointsPerCircle = 40;

        double angle = 2 * Math.PI * (step % pointsPerCircle) / pointsPerCircle + step / 4.0;
        double y = yStart + height * Math.sin(Math.PI * (step - 20) / 20) + 1;

        double offsetX = radius * Math.cos((float) angle);
        double offsetZ = radius * Math.sin((float) angle);

        world.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER,
                x + offsetX, y, z + offsetZ,
                1, 0, 0, 0, 0
        );
    }
}
