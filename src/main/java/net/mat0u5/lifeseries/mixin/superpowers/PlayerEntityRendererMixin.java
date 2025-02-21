package net.mat0u5.lifeseries.mixin.superpowers;

import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
//? if >= 1.21.2
/*import net.minecraft.client.render.entity.state.PlayerEntityRenderState;*/

@Mixin(value = PlayerEntityRenderer.class, priority = 1)
public class PlayerEntityRendererMixin {
    //? if <= 1.21 {
    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfoReturnable<Identifier> cir) {
        UUID uuid = abstractClientPlayerEntity.getUuid();
        if (uuid == null) return;
        if (MainClient.playerDisguise.containsKey(uuid.toString())) {
            SkinTextures textures = DefaultSkinHelper.getSkinTextures(MainClient.playerDisguise.get(uuid.toString()));
            cir.setReturnValue(textures.texture());
        }
    }
    //?} else {
    /*@Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void getTexture(PlayerEntityRenderState playerEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
        if (playerEntityRenderState.playerName == null) return;
        if (MainClient.playerDisguise.containsKey(playerEntityRenderState.playerName.getString())) {
            SkinTextures textures = DefaultSkinHelper.getSkinTextures(MainClient.playerDisguise.get(playerEntityRenderState.playerName.getString()));
            cir.setReturnValue(textures.texture());
        }
    }
    *///?}
}
