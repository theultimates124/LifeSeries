package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getRespawnTarget", at = @At("HEAD"), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        currentSeries.getRespawnTarget(player, postDimensionTransition, cir);
        currentSeries.onPlayerRespawn(player);
    }

    //? if >= 1.21.2 {
    /*@Inject(method = "jump", at = @At("TAIL"))
    public void onJump(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (currentSeries instanceof WildLife wildLife) {
            wildLife.onJump(player);
        }
    }
     *///?}
}
