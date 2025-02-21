package net.mat0u5.lifeseries.mixin.superpowers;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = SleepManager.class, priority = 1)
public abstract class SleepManagerMixin {
    @Inject(method = "canResetTime", at = @At("RETURN"), cancellable = true)
    public void canResetTime(int percentage, List<ServerPlayerEntity> players, CallbackInfoReturnable<Boolean> cir) {
        if (!Main.isLogicalSide()) return;
        if (currentSeries.getSeries() != SeriesList.WILD_LIFE) return;
        for (ServerPlayerEntity player : players) {
            if (player.canResetTimeBySleeping()) {
                if (SuperpowersWildcard.hasActivePower(player, Superpowers.TIME_CONTROL)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
    @Inject(method = "canSkipNight", at = @At("RETURN"), cancellable = true)
    public void canSkipNight(int percentage, CallbackInfoReturnable<Boolean> cir) {
        if (!Main.isLogicalSide()) return;
        if (currentSeries.getSeries() != SeriesList.WILD_LIFE) return;
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (player.isSleeping()) {
                if (SuperpowersWildcard.hasActivePower(player, Superpowers.TIME_CONTROL)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
