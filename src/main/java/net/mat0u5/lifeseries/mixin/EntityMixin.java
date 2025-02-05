package net.mat0u5.lifeseries.mixin;

import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Snails;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getAir", at = @At("HEAD"), cancellable = true)
    public void getAir(CallbackInfoReturnable<Integer> cir) {
        if (Snail.SHOULD_DROWN_PLAYER) {
            if (currentSeries instanceof WildLife) {
                if (!WildcardManager.isActiveWildcard(Wildcards.SNAILS)) return;
                Entity entity = (Entity) (Object) this;
                if (entity instanceof PlayerEntity player) {
                    if (!Snails.snails.containsKey(player.getUuid())) return;
                    Snail snail = Snails.snails.get(player.getUuid());
                    if (snail == null) return;
                    int snailAir = snail.getAir();
                    int initialAir = entity.getDataTracker().get(EntityTrackedData.AIR);
                    if (snailAir < initialAir) {
                        cir.setReturnValue(snailAir);
                    }
                }
            }
        }
    }
}