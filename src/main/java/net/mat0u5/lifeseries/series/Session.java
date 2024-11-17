package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    List<UUID> displayTimer = new ArrayList<>();
    public int DISPLAY_TIMER_INTERVAL = 20;
    public int currentTimer = 20;

    public Integer sessionLength = null;
    public int passedTime;
    public SessionStatus status = SessionStatus.NOT_STARTED;
    public void start() {
        status = SessionStatus.STARTED;
        passedTime = 0;
        MutableText sessionStartedText = Text.literal("Session started!").formatted(Formatting.GOLD);
        Text firstLine = sessionStartedText.append(Text.literal(" ["+OtherUtils.formatTime(sessionLength)+"]").formatted(Formatting.GRAY));
        MutableText infoText1 = Text.literal("§f/session timer remaining§7 - sends remaining time in chat.");
        MutableText infoText2 = Text.literal("§f/session timer showDisplay§7 - shows a permanent session countdown.");
        OtherUtils.broadcastMessage(firstLine);
        OtherUtils.broadcastMessage(infoText1);
        OtherUtils.broadcastMessage(infoText2);
        sessionStart();
    }
    public void stop() {
        status = SessionStatus.FINISHED;
        OtherUtils.broadcastMessage(Text.literal("Session stopped!").formatted(Formatting.GOLD));
        sessionEnd();
    }
    public void pause() {
        if (status == SessionStatus.PAUSED) {
            OtherUtils.broadcastMessage(Text.literal("Session unpaused!").formatted(Formatting.GOLD));
            status = SessionStatus.STARTED;
        }
        else {
            OtherUtils.broadcastMessage(Text.literal("Session paused!").formatted(Formatting.GOLD));
            status = SessionStatus.PAUSED;
        }
    }

    public void setSessionLength(int lengthTicks) {
        sessionLength = lengthTicks;
    }
    public void addSessionLength(int lengthTicks) {
        if (sessionLength == null) sessionLength = 0;
        sessionLength += lengthTicks;
    }
    public void removeSessionLength(int lengthTicks) {
        if (sessionLength == null) sessionLength = 0;
        sessionLength -= lengthTicks;
    }
    public String getSessionLength() {
        if (sessionLength == null) return "";
        return OtherUtils.formatTime(sessionLength);
    }
    public String getRemainingLength() {
        if (sessionLength == null) return "";
        return OtherUtils.formatTime(sessionLength-passedTime);
    }
    public boolean validTime() {
        return sessionLength != null;
    }
    public boolean isInDisplayTimer(ServerPlayerEntity player) {
        return displayTimer.contains(player.getUuid());
    }
    public void addToDisplayTimer(ServerPlayerEntity player) {
        displayTimer.add(player.getUuid());
    }
    public void removeFromDisplayTimer(ServerPlayerEntity player) {
        if (!displayTimer.contains(player.getUuid())) return;
        displayTimer.remove(player.getUuid());
    }

    public void tick(MinecraftServer server) {
        currentTimer--;
        if (currentTimer <=0) {
            currentTimer = DISPLAY_TIMER_INTERVAL;
            displayTimers(server);
        }

        if (!validTime()) return;
        if (status != SessionStatus.STARTED) return;
        passedTime++;
        if (passedTime >= sessionLength) {
            status = SessionStatus.FINISHED;
            sessionEnd();
            OtherUtils.broadcastMessage(Text.literal("The session has ended!").formatted(Formatting.GOLD));
        }
        overrideTick();
    }
    public void displayTimers(MinecraftServer server) {
        String message = "";
        if (status == SessionStatus.NOT_STARTED) {
            message = "Session has not started";
        }
        else if (status == SessionStatus.STARTED) {
            message = getRemainingLength();
        }
        else if (status == SessionStatus.PAUSED) {
            message = "Session has been paused";
        }
        else if (status == SessionStatus.FINISHED) {
            message = "Session has ended";
        }
        for (UUID uuid : displayTimer) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage(Text.literal(message).formatted(Formatting.GRAY), true);
        }
    }

    public void overrideTick() {}
    public void sessionStart() {}
    public void sessionEnd() {
        System.out.println("DefaultSessEnd");
    }
}
