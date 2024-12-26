package net.mat0u5.lifeseries.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getRespawnTarget", at = @At("HEAD"), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        currentSeries.getRespawnTarget(player, postDimensionTransition, cir);
    }
}
