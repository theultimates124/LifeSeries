package net.mat0u5.lifeseries.mixin.superpowers;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(AdvancedExplosionBehavior.class)
public class AdvancedExplosionBehaviorMixin {
    @Inject(method = "getKnockbackModifier", at = @At("RETURN"), cancellable = true)
    public void getKnockbackModifier(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (!Main.isLogicalSide()) return;
        if (entity instanceof ServerPlayerEntity player) {
            if (currentSeries.getSeries() == SeriesList.WILD_LIFE) {
                if (!player.getAbilities().flying) {
                    if (SuperpowersWildcard.hasActivatedPower(player, Superpowers.WIND_CHARGE)) {
                        cir.setReturnValue(3f); // Default is 1.22f
                    }
                }
            }
        }
    }
}
