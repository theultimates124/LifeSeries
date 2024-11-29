package net.mat0u5.lifeseries.utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.mat0u5.lifeseries.Main.server;

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
        Main.LOGGER.info(message.getString());
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

    private static final Pattern TIME_PATTERN = Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");
    public static int parseTimeFromArgument(String time) {
        Matcher matcher = TIME_PATTERN.matcher(time);
        if (!matcher.matches()) {
            return -1; // Invalid time format
        }

        int hours = parseInt(matcher.group(1));
        int minutes = parseInt(matcher.group(2));
        int seconds = parseInt(matcher.group(3));

        return (hours * 3600 + minutes * 60 + seconds) * 20;
    }
    public static int parseTimeSecondsFromArgument(String time) {
        Matcher matcher = TIME_PATTERN.matcher(time);
        if (!matcher.matches()) {
            return -1; // Invalid time format
        }

        int hours = parseInt(matcher.group(1));
        int minutes = parseInt(matcher.group(2));
        int seconds = parseInt(matcher.group(3));

        return (hours * 3600 + minutes * 60 + seconds);
    }

    private static int parseInt(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }
    public static void executeCommand(String command) {
        CommandManager manager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource().withSilent();
        manager.executeWithPrefix(commandSource, command);
    }
}
