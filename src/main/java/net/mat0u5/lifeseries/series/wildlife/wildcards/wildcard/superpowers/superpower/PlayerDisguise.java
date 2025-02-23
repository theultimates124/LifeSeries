package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.ToggleableSuperpower;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TextUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class PlayerDisguise extends ToggleableSuperpower {

    private String copiedPlayerName = "";
    private String copiedPlayerUUID = "";

    public PlayerDisguise(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.PLAYER_DISGUISE;
    }

    @Override
    public int deactivateCooldown() {
        return 20;
    }

    @Override
    public void activate() {
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        Entity lookingAt = PlayerUtils.getEntityLookingAt(player, 50);
        if (lookingAt != null)  {
            if (lookingAt instanceof PlayerEntity lookingAtPlayer) {
                if (!PlayerUtils.isFakePlayer(lookingAtPlayer)) {
                    copiedPlayerUUID = lookingAtPlayer.getUuidAsString();
                    copiedPlayerName = TextUtils.textToLegacyString(lookingAtPlayer.getStyledDisplayName());
                    player.playSoundToPlayer(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.MASTER, 0.3f, 1);
                    PlayerUtils.displayMessageToPlayer(player, Text.literal("Copied DNA of ").append(lookingAtPlayer.getStyledDisplayName()).append(Text.of(" — Press again to disguise")), 65);
                    return;
                }
            }
        }

        if (copiedPlayerName.isEmpty() || copiedPlayerUUID.isEmpty()) {
            PlayerUtils.displayMessageToPlayer(player, Text.of("You are not looking at a player."), 65);
            return;
        }

        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, SoundCategory.MASTER, 1, 1);
        player.getServerWorld().spawnParticles(
                ParticleTypes.EXPLOSION,
                player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(),
                2, 0, 0, 0, 0
        );
        NetworkHandlerServer.sendPlayerDisguise("player_disguise", player.getUuid().toString(), player.getName().getString(), copiedPlayerUUID, copiedPlayerName);

        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.MASTER, 1, 1);
        player.getServerWorld().spawnParticles(
                ParticleTypes.EXPLOSION,
                player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(),
                2, 0, 0, 0, 0
        );
        NetworkHandlerServer.sendPlayerDisguise("player_disguise", player.getUuid().toString(), player.getName().getString(), "", "");
    }

    public void onTakeDamage() {
        deactivate();
    }
}
