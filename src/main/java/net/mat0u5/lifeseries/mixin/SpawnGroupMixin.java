package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.MobSwap;
import net.minecraft.entity.SpawnGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;


@Mixin(value = SpawnGroup.class, priority = 1)
public class SpawnGroupMixin {
    @Shadow
    @Final
    private int capacity;

    @Inject(method = "getCapacity", at = @At("HEAD"), cancellable = true)
    private void getCapacity(CallbackInfoReturnable<Integer> cir) {
        SpawnGroup group = (SpawnGroup)(Object)this;
        if (group.getName().equalsIgnoreCase("monster") || group.getName().equalsIgnoreCase("creature")) {
            if (currentSeries instanceof WildLife) {
                if (!WildcardManager.isActiveWildcard(Wildcards.MOB_SWAP)) return;
                MobSwap.getSpawnCapacity(group, capacity, cir);
            }
        }
    }


    @Inject(method = "isRare", at = @At("HEAD"), cancellable = true)
    private void isRare(CallbackInfoReturnable<Boolean> cir) {
        SpawnGroup group = (SpawnGroup)(Object)this;
        if (group.getName().equalsIgnoreCase("creature")) {
            if (currentSeries instanceof WildLife) {
                if (!WildcardManager.isActiveWildcard(Wildcards.MOB_SWAP)) return;
                MobSwap.isRare(group, cir);
            }
        }
    }
}