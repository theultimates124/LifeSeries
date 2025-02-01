package net.mat0u5.lifeseries.entity.snail.goal;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SnailJumpAttackPlayerGoal extends Goal {

    @NotNull
    private final Snail mob;
    @NotNull
    private Vec3d previousTargetPosition = Vec3d.ZERO;
    private int attackCooldown = Snail.JUMP_COOLDOWN;

    public SnailJumpAttackPlayerGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer == null) {
            return false;
        }

        if (mob.attacking) {
            return true;
        }

        double distanceToTarget = mob.squaredDistanceTo(boundPlayer);
        if (distanceToTarget > Snail.JUMP_RANGE_SQUARED) {
            return false;
        }

        return mob.canSee(boundPlayer);
    }

    @Override
    public boolean shouldContinue() {
        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer == null) {
            return false;
        }

        if (mob.squaredDistanceTo(boundPlayer) > Snail.JUMP_RANGE_SQUARED) {
            return false;
        }

        return mob.canSee(boundPlayer);
    }

    @Override
    public void start() {
        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer != null) {
            this.previousTargetPosition = boundPlayer.getPos();
        }
        this.attackCooldown = Snail.JUMP_COOLDOWN;
        mob.attacking = true;
    }

    @Override
    public void stop() {
        mob.playAttackSound();
        this.attackCooldown = Snail.JUMP_COOLDOWN;
        this.previousTargetPosition = Vec3d.ZERO;
        mob.attacking = false;
    }

    @Override
    public void tick() {
        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (attackCooldown <= 0) {
            jumpAttackPlayer();
        }

        if (boundPlayer != null) {
            this.previousTargetPosition = boundPlayer.getPos();
            mob.lookAtEntity(boundPlayer, 15, 15);
        }
    }

    private void jumpAttackPlayer() {
        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer == null) {
            return;
        }
        this.attackCooldown = Snail.JUMP_COOLDOWN;

        Vec3d mobVelocity = mob.getVelocity();
        Vec3d relativeTargetPos = new Vec3d(
                boundPlayer.getX() - mob.getX(),
                0.0D,
                boundPlayer.getZ() - mob.getZ()
        );

        Vec3d targetVelocity = boundPlayer.getPos().subtract(previousTargetPosition);
        Vec3d expectedTargetPos = relativeTargetPos.add(targetVelocity.multiply(3));

        Vec3d attackVector = mobVelocity;
        if (relativeTargetPos.lengthSquared() > 0.0001) {
            attackVector = expectedTargetPos.normalize().multiply(0.7);
        }

        float velocity = 0.45f;
        mob.setVelocity(attackVector.x, velocity, attackVector.z);
    }
}