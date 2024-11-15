package net.mat0u5.lifeseries.utils;


import net.mat0u5.lifeseries.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OtherUtils {
    public static void broadcastMessage(MinecraftServer server, Text message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message, false);
        }
    }
    public static void broadcastMessage(Text message) {
        for (ServerPlayerEntity player : Main.server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message, false);
        }
    }
}
