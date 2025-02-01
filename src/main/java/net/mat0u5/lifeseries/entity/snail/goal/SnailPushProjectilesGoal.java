package net.mat0u5.lifeseries.entity.snail.goal;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class SnailPushProjectilesGoal extends Goal {

    @NotNull
    private final Snail mob;
    @NotNull
    private List<ProjectileEntity> projectiles  = new ArrayList<>();

    public SnailPushProjectilesGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (mob.getWorld() == null) {
            return false;
        }

        World world = mob.getWorld();
        this.projectiles = world.getEntitiesByClass(
                ProjectileEntity.class,
                mob.getBoundingBox().expand(5.0, 5.0, 5.0),
                projectile -> projectile.squaredDistanceTo(mob) < 16
        );

        return !this.projectiles.isEmpty();
    }

    @Override
    public void start() {
        boolean playSound = false;
        for (ProjectileEntity projectile : projectiles) {
            NbtCompound empty = new NbtCompound();
            NbtCompound nbt = projectile.writeNbt(empty);
            if (nbt.contains("inGround")) {
                if (nbt.getBoolean("inGround")) {
                    continue;
                }
            }
            Entity sender = projectile.getOwner();
            if (sender instanceof LivingEntity target) {
                if (target instanceof Snail) {
                    continue;
                }

                double dx = target.getX() - projectile.getX();
                double dz = target.getZ() - projectile.getZ();
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

                double dy = target.getEyeY() - projectile.getY();

                float speed = 1.6F;

                double time = horizontalDistance / speed;
                double velocityY = dy / time + 0.5 * 0.05 * time;

                double norm = Math.sqrt(dx * dx + dz * dz);
                double velocityX = (dx / norm) * speed;
                double velocityZ = (dz / norm) * speed;

                projectile.setVelocity(velocityX, velocityY, velocityZ, 1.6F, 0.0F);
                projectile.setOwner(mob);

                playSound = true;
            }
        }
        if (playSound) {
            mob.playThrowSound();
        }
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }

    @Override
    public void stop() {
        this.projectiles.clear();
    }
}
