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
    @Nullable
    private ServerPlayerEntity boundPlayer;
    @NotNull
    private Vec3d previousTargetPosition = Vec3d.ZERO;
    private int attackCooldown = Snail.JUMP_COOLDOWN;

    public SnailJumpAttackPlayerGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        this.boundPlayer = mob.getBoundPlayer();
        if (this.boundPlayer == null) {
            return false;
        }

        if (mob.attacking) {
            return true;
        }

        double distanceToTarget = mob.squaredDistanceTo(boundPlayer);
        if (distanceToTarget >= Snail.JUMP_RANGE_SQUARED) {
            return false;
        }

        return mob.canSee(boundPlayer) && mob.isOnGround();
    }

    @Override
    public boolean shouldContinue() {
        if (this.boundPlayer == null) {
            return false;
        }

        return mob.isOnGround() && mob.squaredDistanceTo(boundPlayer) <= Snail.JUMP_RANGE_SQUARED;
    }

    @Override
    public void start() {
        OtherUtils.broadcastMessage(Text.of("test_SnailJumpAttackPlayerGoal"));
        if (this.boundPlayer != null) {
            this.previousTargetPosition = this.boundPlayer.getPos();
        }
        this.attackCooldown = Snail.JUMP_COOLDOWN;
        mob.attacking = true;
    }

    @Override
    public void stop() {
        mob.playAttackSound();
        this.boundPlayer = null;
        this.attackCooldown = Snail.JUMP_COOLDOWN;
        this.previousTargetPosition = Vec3d.ZERO;
        mob.attacking = false;
    }

    @Override
    public void tick() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (attackCooldown <= 0) {
            jumpAttackPlayer();
        }

        if (this.boundPlayer != null) {
            this.previousTargetPosition = this.boundPlayer.getPos();
            mob.lookAtEntity(this.boundPlayer, 15, 15);
        }
    }

    private void jumpAttackPlayer() {
        if (this.boundPlayer == null) {
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
            attackVector = expectedTargetPos.normalize().multiply(0.5);
        }

        float velocity = 0.35f;
        mob.setVelocity(attackVector.x, velocity, attackVector.z);
    }
}