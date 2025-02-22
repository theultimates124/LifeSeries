package net.mat0u5.lifeseries.mixin.superpowers;

import net.mat0u5.lifeseries.entity.fakeplayer.FakePlayer;
import net.mat0u5.lifeseries.entity.fakeplayer.FakePlayerNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PlayerManager.class, priority = 1)
public class PlayerManagerMixin {
    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    public void broadcast(Text message, boolean overlay, CallbackInfo ci) {
        if (message.getString().contains("`")) ci.cancel();
    }



    /*
        Used code from https://github.com/gnembon/fabric-carpet
     */
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    public void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        if (player instanceof FakePlayer fakePlayer) {
            fakePlayer.fixStartingPosition.run();
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)Lnet/minecraft/server/network/ServerPlayNetworkHandler;"))
    public ServerPlayNetworkHandler onPlayerConnect(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        if (player instanceof FakePlayer fake) {
            return new FakePlayerNetworkHandler(this.server, connection, fake, clientData);
        }
        else {
            return new ServerPlayNetworkHandler(this.server, connection, player, clientData);
        }
    }

}
