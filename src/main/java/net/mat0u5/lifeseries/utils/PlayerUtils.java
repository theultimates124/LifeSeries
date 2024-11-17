package net.mat0u5.lifeseries.utils;

import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

import static net.mat0u5.lifeseries.Main.server;

public class PlayerUtils {
    public static void sendTitle(ServerPlayerEntity player, Text title, int fadeIn, int stay, int fadeOut) {
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
    public static void playSoundToPlayers(Collection<ServerPlayerEntity> players, SoundEvent sound) {
        for (ServerPlayerEntity player : players) {
            player.playSoundToPlayer(sound, SoundCategory.MASTER, 1, 1);
        }
    }
    public static List<ServerPlayerEntity> getAllPlayers() {
        return server.getPlayerManager().getPlayerList();
    }
}
