package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract boolean isDead();

    @Shadow
    public abstract float getMaxHealth();

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (currentSeries.getSeries() != SeriesList.DOUBLE_LIFE) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            ((DoubleLife)currentSeries).onPlayerDamage((ServerPlayerEntity) player, source, amount);
        }
    }
    @Inject(method = "heal", at = @At("HEAD"))
    private void onHeal(float amount, CallbackInfo info) {
        if (currentSeries.getSeries() != SeriesList.DOUBLE_LIFE) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            ((DoubleLife)currentSeries).onPlayerHeal((ServerPlayerEntity) player, amount);
        }
    }
}
