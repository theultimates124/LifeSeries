package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = net.minecraft.entity.player.PlayerInventory.class, priority = 1)
public abstract class PlayerInventoryMixin {
    @Inject(method = "markDirty", at = @At("HEAD"))
    private void onInventoryUpdated(CallbackInfo ci) {
        if (!Main.isLogicalSide()) return;
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        PlayerEntity player = inventory.player;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (blacklist != null) {
                blacklist.onInventoryUpdated(serverPlayer,inventory,ci);
            }
            if (currentSeries instanceof WildLife) {
                if (!WildcardManager.isActiveWildcard(Wildcards.HUNGER)) {
                    Hunger.updateInventory(serverPlayer);
                }
            }
        }
    }
}
