package net.mat0u5.lifeseries.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.mat0u5.lifeseries.Main.blacklist;

@Mixin(net.minecraft.entity.player.PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "markDirty", at = @At("HEAD"))
    private void onInventoryUpdated(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        PlayerEntity player = inventory.player;
        blacklist.onInventoryUpdated((ServerPlayerEntity) player,inventory,ci);
    }
}
