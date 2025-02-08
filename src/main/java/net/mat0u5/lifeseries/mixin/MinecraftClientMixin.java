package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TimeDilation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClient.class, priority = 1)
public abstract class MinecraftClientMixin {
    @Inject(method = "getTargetMillisPerTick", at = @At("HEAD"), cancellable = true)
    private void getTargetMillisPerTick(float millis, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            TickManager tickManager = client.world.getTickManager();
            if (tickManager.shouldTick()) {
                float mspt = Math.max(TimeDilation.MIN_PLAYER_MSPT, tickManager.getMillisPerTick());
                cir.setReturnValue(mspt);
                return;
            }
        }
        cir.setReturnValue(millis);
    }
}
