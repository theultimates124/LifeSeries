package net.mat0u5.lifeseries.mixin.client;

import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.client.ClientKeybinds;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class, priority = 1)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        ClientKeybinds.tick();
        if (MainClient.clientCurrentSeries != SeriesList.WILD_LIFE) return;
        if (!MainClient.clientActiveWildcards.contains(Wildcards.SIZE_SHIFTING)) return;
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        //? if <= 1.21 {
        if (player.input.jumping) {
            NetworkHandlerClient.sendHoldingJumpPacket();
        }
        //?} else {
        /*if (player.input.playerInput.jump()) {
            NetworkHandlerClient.sendHoldingJumpPacket();
        }
        *///?}
    }
}
