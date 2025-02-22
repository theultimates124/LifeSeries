package net.mat0u5.lifeseries.mixin.superpowers.client;

import net.mat0u5.lifeseries.MainClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = EntityRenderer.class, priority = 1)
public class EntityRendererMixin<T extends Entity> {

    //? if <= 1.21 {
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V"),
            index = 1
    )
    public Text render(Text text) {
    //?} else {
    /*@ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
            index = 1
    )
    public Text render(Text text) {
    *///?}
        if (text != null) {
            if (MainClient.playerDisguiseNames.containsKey(text.getString())) {
                String name = MainClient.playerDisguiseNames.get(text.getString());
                if (MinecraftClient.getInstance().getNetworkHandler() == null) {
                    return text;
                }
                for (PlayerListEntry entry : MinecraftClient.getInstance().getNetworkHandler().getPlayerList()) {
                    if (entry.getProfile().getName().equalsIgnoreCase(name)) {
                        if (entry.getDisplayName() != null) {
                            return entry.getDisplayName();
                        }
                        return Text.literal(name).setStyle(text.getStyle());
                    }
                }
            }
        }
        return text;
    }
}
