package net.mat0u5.lifeseries.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.client.ClientHandler;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class PlayerUtils {

    public static void sendTitleWithSubtitle(ServerPlayerEntity player, Text title, Text subtitle, int fadeIn, int stay, int fadeOut) {
        if (server == null) return;
        if (player == null) return;
        if (player.isDead()) {
            TaskScheduler.scheduleTask(5, () -> sendTitleWithSubtitle(server.getPlayerManager().getPlayer(player.getUuid()), title, subtitle, fadeIn, stay, fadeOut));
            return;
        }
        TitleFadeS2CPacket fadePacket = new TitleFadeS2CPacket(fadeIn, stay, fadeOut);
        player.networkHandler.sendPacket(fadePacket);
        TitleS2CPacket titlePacket = new TitleS2CPacket(title);
        player.networkHandler.sendPacket(titlePacket);
        SubtitleS2CPacket subtitlePacket = new SubtitleS2CPacket(subtitle);
        player.networkHandler.sendPacket(subtitlePacket);
    }

    public static void sendTitle(ServerPlayerEntity player, Text title, int fadeIn, int stay, int fadeOut) {
        if (server == null) return;
        if (player == null) return;
        if (player.isDead()) {
            TaskScheduler.scheduleTask(5, () -> sendTitle(server.getPlayerManager().getPlayer(player.getUuid()), title, fadeIn, stay, fadeOut));
            return;
        }
        TitleFadeS2CPacket fadePacket = new TitleFadeS2CPacket(fadeIn, stay, fadeOut);
        player.networkHandler.sendPacket(fadePacket);
        TitleS2CPacket titlePacket = new TitleS2CPacket(title);
        player.networkHandler.sendPacket(titlePacket);
    }

    public static void sendTitleToPlayers(Collection<ServerPlayerEntity> players, Text title, int fadeIn, int stay, int fadeOut) {
        for (ServerPlayerEntity player : players) {
            sendTitle(player, title, fadeIn, stay, fadeOut);
        }
    }

    public static void sendTitleWithSubtitleToPlayers(Collection<ServerPlayerEntity> players, Text title, Text subtitle, int fadeIn, int stay, int fadeOut) {
        for (ServerPlayerEntity player : players) {
            sendTitleWithSubtitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void playSoundToPlayers(Collection<ServerPlayerEntity> players, SoundEvent sound) {
        playSoundToPlayers(players,sound,SoundCategory.MASTER,1,1);
    }
    public static void playSoundToPlayers(Collection<ServerPlayerEntity> players, SoundEvent sound, float volume, float pitch) {
        playSoundToPlayers(players,sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void playSoundToPlayers(Collection<ServerPlayerEntity> players, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        for (ServerPlayerEntity player : players) {
            player.playSoundToPlayer(sound, soundCategory, volume, pitch);
        }
    }

    public static List<ServerPlayerEntity> getAllPlayers() {
        return server.getPlayerManager().getPlayerList();
    }

    public static void applyResorucepack(UUID uuid) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientHandler.applyResourcepack(player);
            return;
        }
        applyServerResourcepack(player);
    }
    public static void applyServerResourcepack(ServerPlayerEntity player) {
        //applySingleResourcepack(player, Series.RESOURCEPACK_MAIN_URL, Series.RESOURCEPACK_MAIN_SHA, "Life Series Main Resourcepack.");
        if (currentSeries instanceof SecretLife) {
            applySingleResourcepack(player, SecretLife.RESOURCEPACK_SECRETLIFE_URL, SecretLife.RESOURCEPACK_SECRETLIFE_SHA, "Secret Life Resourcepack.");
        }
        else {
            removeSingleResourcepack(player, SecretLife.RESOURCEPACK_SECRETLIFE_URL);
        }
    }

    private static void applySingleResourcepack(ServerPlayerEntity player, String link, String sha1, String message) {
        UUID id = UUID.nameUUIDFromBytes(link.getBytes(StandardCharsets.UTF_8));
        ResourcePackSendS2CPacket resourcepackPacket = new ResourcePackSendS2CPacket(
                id,
                link,
                sha1,
                false,
                Optional.of(Text.translatable(message))
        );
        player.networkHandler.sendPacket(resourcepackPacket);
    }

    private static void removeSingleResourcepack(ServerPlayerEntity player, String link) {
        UUID id = UUID.nameUUIDFromBytes(link.getBytes(StandardCharsets.UTF_8));
        ResourcePackRemoveS2CPacket removePackPacket = new ResourcePackRemoveS2CPacket(Optional.of(id));
        player.networkHandler.sendPacket(removePackPacket);
    }

    public static List<ItemStack> getPlayerInventory(ServerPlayerEntity player) {
        List<ItemStack> list = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                list.add(itemStack);
            }
        }
        return list;
    }

    public static void clearItemStack(ServerPlayerEntity player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.equals(itemStack)) {
                inventory.removeStack(i);
            }
        }
    }
}
