package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.client.trivia.Trivia;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (!Trivia.isDoingTrivia()) return;

        if (MinecraftClient.getInstance().player != null) {
            //? if <= 1.21 {
            MinecraftClient.getInstance().player.sendMessage(Text.of("§cYou can't talk in chat while answering a Trivia Bot!"));
            //?} else {
            /*MinecraftClient.getInstance().player.sendMessage(Text.of("§cYou can't talk in chat while answering a Trivia Bot!"), false);
            *///?}
        }
        ci.cancel();
    }

    @Unique
    private static final List<String> notAllowedCommand = List.of("msg", "tell", "whisper", "w", "me");
    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void onSendChatCommand(String command, CallbackInfo ci) {
        if (!Trivia.isDoingTrivia()) return;
        if (MinecraftClient.getInstance().player == null) return;
        for (String s : notAllowedCommand) {
            if (command.startsWith(s+" ")) {
                //? if <= 1.21 {
                MinecraftClient.getInstance().player.sendMessage(Text.of("§cYou can't talk in chat while answering a Trivia Bot!"));
                //?} else {
                /*MinecraftClient.getInstance().player.sendMessage(Text.of("§cYou can't talk in chat while answering a Trivia Bot!"), false);
                 *///?}
                ci.cancel();
                return;
            }
        }
    }
}
