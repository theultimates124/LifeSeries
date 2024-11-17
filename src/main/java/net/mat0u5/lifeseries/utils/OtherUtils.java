package net.mat0u5.lifeseries.utils;


import net.mat0u5.lifeseries.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OtherUtils {
    public static void broadcastMessage(MinecraftServer server, Text message) {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.sendMessage(message, false);
        }
    }
    public static void broadcastMessage(Text message) {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.sendMessage(message, false);
        }
    }
    public static void broadcastMessageToAdmins(Text message) {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (!PermissionManager.isAdmin(player)) continue;
            player.sendMessage(message, false);
        }
    }

    public static String formatTime(int totalTicks) {
        int hours = totalTicks / 72000;
        int minutes = (totalTicks % 72000) / 1200;
        int seconds = (totalTicks % 1200) / 20;

        return hours+":"+ formatTimeNumber(minutes)+":"+ formatTimeNumber(seconds);
    }
    public static String formatTimeNumber(int time) {
        String value = String.valueOf(time);
        while (value.length() < 2) value = "0" + value;
        return value;
    }
    public static int minutesToTicks(int mins) {
        return mins*60*20;
    }
    public static int secondsToTicks(int secs) {
        return secs*20;
    }
}
