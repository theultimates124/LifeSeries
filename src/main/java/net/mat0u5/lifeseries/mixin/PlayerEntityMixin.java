package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
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

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "applyDamage", at = @At("TAIL"))
    //? if <=1.21 {
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo info) {
     //?} else
    /*private void onApplyDamage(ServerWorld world, DamageSource source, float amount, CallbackInfo info) {*/
        PlayerEntity player = (PlayerEntity) (Object) this;
        currentSeries.onPlayerDamage((ServerPlayerEntity) player, source, amount);
    }

    @Inject(method = "canFoodHeal", at = @At("HEAD"), cancellable = true)
    private void canFoodHeal(CallbackInfoReturnable<Boolean> cir) {
        if (currentSeries instanceof DoubleLife)  {
            PlayerEntity player = (PlayerEntity) (Object) this;
            ((DoubleLife) currentSeries).canFoodHeal(player, cir);
        }
    }
}
