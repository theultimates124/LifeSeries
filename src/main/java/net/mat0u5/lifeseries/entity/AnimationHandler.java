package net.mat0u5.lifeseries.entity;

import de.tomalbrc.bil.api.AnimatedHolder;
import de.tomalbrc.bil.api.Animator;
import net.minecraft.entity.LivingEntity;

public class AnimationHandler {
    public static void updateWalkAnimation(LivingEntity entity, AnimatedHolder holder) {
        updateWalkAnimation(entity, holder, 0);
    }

    public static void updateWalkAnimation(LivingEntity entity, AnimatedHolder holder, int priority) {
        Animator animator = holder.getAnimator();
        if (entity.limbAnimator.isLimbMoving() && entity.limbAnimator.getSpeed() > 0.02) {
            animator.playAnimation("walk", priority);
            animator.pauseAnimation("idle");
        }
        else {
            animator.pauseAnimation("walk");
            animator.playAnimation("idle", priority, true);
        }
    }

    public static void updateHurtVariant(LivingEntity entity, AnimatedHolder holder) {
        updateHurtColor(entity, holder);
    }

    public static void updateHurtColor(LivingEntity entity, AnimatedHolder holder) {
        if (entity.deathTime > 0 || entity.hurtTime > 0) {
            holder.setColor(0xff7e7e);
        }
        else {
            holder.clearColor();
        }
    }
}
