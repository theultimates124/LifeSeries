package net.mat0u5.lifeseries.utils;

import de.tomalbrc.bil.api.AnimatedHolder;
import de.tomalbrc.bil.api.Animator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ParticleCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

import java.awt.*;

public class AnimationUtils {
    public static int SPIRAL_DURATION = 175;
    public static void playTotemAnimation(ServerPlayerEntity player) {
        //The animation lasts about 40 ticks.
        player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte) 35));
    }

    public static void createSpiral(ServerPlayerEntity player, int duration) {
        SPIRAL_DURATION = duration;
        TaskScheduler.scheduleTask(1, () -> startSpiral(player));
    }

    private static void startSpiral(ServerPlayerEntity player) {
        TaskScheduler.scheduleTask(1, () -> runSpiralStep(player, 0));
    }

    private static void runSpiralStep(ServerPlayerEntity player, int step) {
        if (player == null) return;

        processSpiral(player, step);
        processSpiral(player, step+1);
        processSpiral(player, step+2);
        processSpiral(player, step+3);

        if (step <= SPIRAL_DURATION) {
            TaskScheduler.scheduleTask(1, () -> runSpiralStep(player, step + 4));
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

    public static void createGlyphAnimation(ServerWorld world, Vec3d target, int duration) {
        if (world == null || target == null || duration <= 0) return;

        int steps = duration; // Animation duration in ticks
        double radius = 7.5; // Radius of the glyph starting positions

        for (int step = 0; step < steps; step++) {
            int currentStep = step;
            TaskScheduler.scheduleTask(step, () -> spawnGlyphParticles(world, target, radius, currentStep, steps));
        }
    }

    private static void spawnGlyphParticles(ServerWorld world, Vec3d target, double radius, int step, int totalSteps) {
        int particlesPerTick = 50; // Number of glyphs spawned per tick
        Random random = world.getRandom();

        for (int i = 0; i < particlesPerTick; i++) {
            // Randomize starting position around the target block
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = radius * (random.nextDouble() * 0.5); // Random distance within radius

            double startX = target.getX() + distance * Math.cos(angle);
            double startY = target.getY() + random.nextDouble()*2+1; // Random height variation
            double startZ = target.getZ() + distance * Math.sin(angle);

            // Compute the velocity vector toward the target
            double targetX = target.getX();
            double targetY = target.getY();
            double targetZ = target.getZ();

            double dx = targetX - startX;
            double dy = targetY - startY;
            double dz = targetZ - startZ;

            // Normalize velocity to control particle speed
            double velocityScale = -50; // Adjust speed (lower values for slower movement)
            double vx = dx * velocityScale;
            double vy = dy * velocityScale;
            double vz = dz * velocityScale;

            // Spawn the particle with velocity
            world.spawnParticles(
                    ParticleTypes.ENCHANT, // Glyph particle
                    startX, startY, startZ, // Starting position
                    0, // Number of particles to display as a burst (keep 0 for velocity to work)
                    vx, vy, vz, // Velocity components
                    0.2 // Spread (keep non-zero to activate velocity)
            );
        }
    }

    public static void spawnFireworkBall(ServerWorld world, Vec3d position, int duration, double radius, Vector3f color) {
        if (world == null || position == null || duration <= 0 || radius <= 0) return;

        Random random = world.getRandom();

        for (int step = 0; step < duration; step++) {
            int currentStep = step;
            TaskScheduler.scheduleTask(currentStep, () -> {
                // Spawn particles in a spherical pattern for the current step
                for (int i = 0; i < 50; i++) { // 50 particles per tick
                    double theta = random.nextDouble() * 2 * Math.PI; // Angle in the XY plane
                    double phi = random.nextDouble() * Math.PI; // Angle from the Z-axis
                    double r = radius * (0.8 + 0.2 * random.nextDouble()); // Slight variation in radius

                    // Spherical coordinates to Cartesian
                    double x = r * Math.sin(phi) * Math.cos(theta);
                    double y = r * Math.sin(phi) * Math.sin(theta);
                    double z = r * Math.cos(phi);

                    // Create the particle effect with the generated color and size
                    //? if <= 1.21 {
                    DustParticleEffect particleEffect = new DustParticleEffect(color, 1.0f);
                    //?} else
                    /*DustParticleEffect particleEffect = new DustParticleEffect(new Color(color.x, color.y, color.z).getRGB(), 1.0f);*/

                    // Spawn particle with random offset
                    world.spawnParticles(
                            particleEffect, // Colored particle effect
                            position.getX() + x,
                            position.getY() + y,
                            position.getZ() + z,
                            1, // Particle count
                            0, 0, 0, // No velocity
                            0 // No spread
                    );
                }
            });
        }
    }

    public static void spawnTeleportParticles(ServerWorld world, Vec3d pos) {
        world.spawnParticles(
                ParticleTypes.PORTAL,
                pos.x, pos.y, pos.z,
                30,
                0, 0, 0,
                0.35
        );
    }
}
