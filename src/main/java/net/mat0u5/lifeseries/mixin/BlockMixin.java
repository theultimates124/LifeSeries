package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.MainClient;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getSlipperiness", at = @At("RETURN"), cancellable = true)
    private void injectSlipperiness(CallbackInfoReturnable<Float> cir) {
        if (MainClient.CURSE_SLIDING) cir.setReturnValue(0.98F);
    }

    @Inject(method = "getVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    private void injectVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        if (MainClient.CURSE_SLIDING) cir.setReturnValue(1F);
    }
}