package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = ServerPlayerEntity.class, priority = 1)
public class ServerPlayerEntityMixin {
    @Inject(method = "getRespawnTarget", at = @At("HEAD"), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        if (!Main.isLogicalSide()) return;
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        UUID uuid = player.getUuid();
        TaskScheduler.scheduleTask(1, () -> {
            currentSeries.onPlayerRespawn(Objects.requireNonNull(Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayer(uuid)));
        });
    }

    //? if >= 1.21.2 {
    /*@Inject(method = "jump", at = @At("TAIL"))
    public void onJump(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (currentSeries instanceof WildLife wildLife) {
            WildcardManager.onJump(player);
        }
    }
     *///?}

    @Inject(method = "openHandledScreen", at = @At("HEAD"))
    private void onInventoryOpen(@Nullable NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
        if (!Main.isLogicalSide()) return;
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (blacklist != null) {
            TaskScheduler.scheduleTask(1, () -> player.currentScreenHandler.getStacks().forEach((itemStack) -> blacklist.processItemStack(player, itemStack)));
        }
    }
}
