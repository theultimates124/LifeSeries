package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.utils.morph.MorphComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.MORPH_COMPONENT;
import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = PlayerEntity.class, priority = 1)
public abstract class PlayerEntityMixin {

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    //? if <=1.21 {
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo ci) {
     //?} else
    /*private void onApplyDamage(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {*/
        if (!Main.isLogicalSide()) return;
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            currentSeries.onPlayerDamage(serverPlayer, source, amount, ci);
        }
    }

    @Inject(method = "canFoodHeal", at = @At("HEAD"), cancellable = true)
    private void canFoodHeal(CallbackInfoReturnable<Boolean> cir) {
        if (!Main.isLogicalSide()) return;
        if (currentSeries instanceof DoubleLife doubleLife)  {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (player instanceof ServerPlayerEntity serverPlayer) {
                doubleLife.canFoodHeal(serverPlayer, cir);
            }
        }
    }


    @Inject(method = "getBaseDimensions", at = @At("HEAD"), cancellable = true)
    public void getBaseDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if(MORPH_COMPONENT.isProvidedBy(player)) {
            MorphComponent morphComponent = MORPH_COMPONENT.get(player);
            LivingEntity dummy = morphComponent.getDummy();
            if (morphComponent.isMorphed() && dummy != null){
                cir.setReturnValue(dummy.getDimensions(EntityPose.STANDING));
            }
        }
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void updateHitbox(CallbackInfo ci) {
        ((PlayerEntity) (Object) this).calculateDimensions();
    }
}
