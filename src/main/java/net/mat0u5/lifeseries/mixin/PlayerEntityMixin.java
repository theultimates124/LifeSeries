package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = PlayerEntity.class, priority = 1)
public abstract class PlayerEntityMixin {

    @Inject(method = "applyDamage", at = @At("TAIL"))
    //? if <=1.21 {
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo info) {
     //?} else
    /*private void onApplyDamage(ServerWorld world, DamageSource source, float amount, CallbackInfo info) {*/
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof  ServerPlayerEntity serverPlayer) {
            currentSeries.onPlayerDamage(serverPlayer, source, amount);
        }
    }

    @Inject(method = "canFoodHeal", at = @At("HEAD"), cancellable = true)
    private void canFoodHeal(CallbackInfoReturnable<Boolean> cir) {
        if (currentSeries instanceof DoubleLife doubleLife)  {
            PlayerEntity player = (PlayerEntity) (Object) this;
            doubleLife.canFoodHeal(player, cir);
        }
    }

    //? if <=1.21 {
    
    @Inject(method = "jump", at = @At("TAIL"))
    public void onJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (currentSeries instanceof WildLife wildLife) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                WildcardManager.onJump(serverPlayer);
            }
        }
    }
     //?}
}
