package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.secretlife.Task;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.currentSession;

public class Stats {

    public static List<String> messages = new ArrayList<>();

    public static void deactivateWildcard(Wildcards type) {
        addMessageWithTime("Deactivated Wildcard: " + type);
    }

    public static void activateWildcard(Wildcards type) {
        addMessageWithTime("Activated Wildcard: " + type);
    }

    public static void logPlayers() {
        List<String> names = new ArrayList<>();
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            names.add(player.getNameForScoreboard());
        }
        addMessageWithTime("Players online: " + String.join(", ", names));
    }

    public static void rerollTask(ServerPlayerEntity player) {
        addMessageWithTime(player.getNameForScoreboard() + " has rerolled their task.");
    }

    public static void successTask(ServerPlayerEntity player) {
        addMessageWithTime(player.getNameForScoreboard() + " has passed their task.");
    }

    public static void failTask(ServerPlayerEntity player) {
        addMessageWithTime(player.getNameForScoreboard() + " has failed their task.");
    }

    public static void assignTask(ServerPlayerEntity player, Task task, List<String> linesStr) {
        addMessageWithTime(player.getNameForScoreboard() + " has been given a " + task.type.name() + " task: " + String.join(" ", linesStr));
    }

    public static void claimKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        addMessageWithTime(killer.getNameForScoreboard() + "'s kill claim of " + victim.getNameForScoreboard() + " has been accepted.");
    }

    public static void soulmate(ServerPlayerEntity player, ServerPlayerEntity soulmate) {
        addMessageWithTime(player.getNameForScoreboard() + "'s soulmate has been chosen to be " + soulmate.getNameForScoreboard());
    }

    public static void assignRandomLives(ServerPlayerEntity player, int amount) {
        addMessageWithTime(player.getNameForScoreboard() + " has been randomly assigned " + amount + " lives");
    }

    public static void givelife(Text playerName, ServerPlayerEntity target) {
        addMessageWithTime("<@","> ",playerName.getString()+" gave a life to " + target.getNameForScoreboard());
    }

    public static void triggerSessionAction(String message) {
        if (message == null || message.isEmpty()) return;
        addMessageWithTime("TRIGGERED_SESSION_ACTION: " + message);
    }

    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        addMessageWithTime("<@","> ",source.getDeathMessage(player).getString());
    }

    public static void onPlayerLostAllLives(ServerPlayerEntity player) {
        addMessageWithTime(player.getNameForScoreboard() + " lost all lives.");
    }

    public static void boogeymenChosen(List<ServerPlayerEntity> players) {
        List<String> names = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            names.add(player.getNameForScoreboard());
        }
        addMessageWithTime("Boogeymen chosen: " + String.join(", ", names));
    }

    public static void sessionStart() {
        addMessageWithTime("Session started!");
    }

    public static void sessionEnd() {
        addMessageWithTime("The session has ended!");
    }
    public static void addMessageWithTime(String message) {
        addMessageWithTime("[@","] ", message);
    }

    private static void addMessageWithTime(String start, String end,String message) {
        String time = currentSession.getPassedTime();
        String finalMessage = start+time+end+message;
        messages.add(finalMessage);
    }

    public static void resetStats() {
        messages.clear();
        messages.add("----- "+currentSeries.getSeries().name()+" (" + OtherUtils.getTimeAndDate() + ") -----");
    }

    public static String getStats() {
        return String.join("\n", messages);
    }

    public static void sendTranscriptToAdmins() {
        Text sessionTranscript = getTranscriptMessage();
        OtherUtils.broadcastMessageToAdmins(sessionTranscript);
    }

    public static Text getTranscriptMessage() {
        return Text.literal("§7Click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.BLUE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Stats.getStats()))
                                .withUnderline(true)
                        )).append(Text.of("§7 to copy the session transcript."));
    }
}
