package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.WorldUitls;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;

import java.util.*;

public class Session {
    public Map<UUID, Integer> playerNaturalDeathLog = new HashMap<>();
    public List<SessionAction> activeActions = new ArrayList<>();
    public List<UUID> displayTimer = new ArrayList<>();
    public static final int NATURAL_DEATH_LOG_MAX = 2400;
    public static final int DISPLAY_TIMER_INTERVAL = 20;
    public int currentTimer = 20;

    public Integer sessionLength = null;
    public int passedTime;
    public SessionStatus status = SessionStatus.NOT_STARTED;

    SessionAction defaultWarningAction = new SessionAction(OtherUtils.minutesToTicks(-5)) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("Session ends in 5 minutes!").formatted(Formatting.GOLD));
        }
    };

    public void sessionStart() {
        if (!canStartSession()) return;
        status = SessionStatus.STARTED;
        passedTime = 0;
        MutableText sessionStartedText = Text.literal("Session started!").formatted(Formatting.GOLD);
        Text firstLine = sessionStartedText.append(Text.literal(" ["+OtherUtils.formatTime(sessionLength)+"]").formatted(Formatting.GRAY));
        MutableText infoText1 = Text.literal("§f/session timer remaining§7 - sends remaining time in chat.");
        MutableText infoText2 = Text.literal("§f/session timer showDisplay§7 - shows a permanent session countdown.");
        OtherUtils.broadcastMessage(firstLine);
        OtherUtils.broadcastMessage(infoText1);
        OtherUtils.broadcastMessage(infoText2);
        activeActions.clear();
        activeActions.add(defaultWarningAction);
    }
    public void sessionEnd() {
        status = SessionStatus.FINISHED;
        OtherUtils.broadcastMessage(Text.literal("The session has ended!").formatted(Formatting.GOLD));
    }
    public void sessionPause() {
        if (status == SessionStatus.PAUSED) {
            OtherUtils.broadcastMessage(Text.literal("Session unpaused!").formatted(Formatting.GOLD));
            status = SessionStatus.STARTED;
        }
        else {
            OtherUtils.broadcastMessage(Text.literal("Session paused!").formatted(Formatting.GOLD));
            status = SessionStatus.PAUSED;
        }
    }
    public boolean canStartSession() {
        if (!validTime()) return false;
        if (status == SessionStatus.STARTED) return false;
        if (status == SessionStatus.PAUSED) return false;
        return true;
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

        if (playerNaturalDeathLog != null && !playerNaturalDeathLog.isEmpty()) {
            int currentTime = server.getTicks();
            List<UUID> removeQueue = new ArrayList<>();
            for (Map.Entry<UUID, Integer> entry : playerNaturalDeathLog.entrySet()) {
                int tickDiff = currentTime - entry.getValue();
                if (tickDiff >= NATURAL_DEATH_LOG_MAX) {
                    removeQueue.add(entry.getKey());
                }
            }
            if (!removeQueue.isEmpty()) {
                for (UUID uuid : removeQueue) {
                    playerNaturalDeathLog.remove(uuid);
                }
            }
        }
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (player.isSpectator()) continue;
            checkPlayerPosition(player);
        }

        if (!validTime()) return;
        if (status != SessionStatus.STARTED) return;
        tickSessionOn();
    }
    public void tickSessionOn() {
        passedTime++;
        if (passedTime >= sessionLength) {
            sessionEnd();
        }

        //Actions
        if (activeActions == null) return;
        if (activeActions.isEmpty()) return;
        List<SessionAction> remaining = new ArrayList<>();
        for (SessionAction action : activeActions) {
            boolean triggered = action.tick(passedTime, sessionLength);
            if (!triggered) {
                remaining.add(action);
            }
        }
        activeActions = remaining;
    }
    public void checkPlayerPosition(ServerPlayerEntity player) {
        WorldBorder border = player.getWorld().getWorldBorder();
        double playerSize = player.getBoundingBox().getLengthX()/2;
        double minX = border.getBoundWest() + playerSize;
        double maxX = border.getBoundEast() - playerSize;
        double minZ = border.getBoundNorth() + playerSize;
        double maxZ = border.getBoundSouth() - playerSize;

        double playerX = player.getX();
        double playerZ = player.getZ();

        if (playerX < minX || playerX > maxX || playerZ < minZ || playerZ > maxZ) {
            // Clamp player position inside the border
            double clampedX = Math.max(minX, Math.min(maxX, playerX));
            double clampedZ = Math.max(minZ, Math.min(maxZ, playerZ));
            double safeY = WorldUitls.findSafeY(player.getWorld(), new Vec3d(clampedX, player.getY(), clampedZ));

            // Teleport player inside the world border
            player.teleport(player.getServerWorld(),clampedX, safeY, clampedZ, player.getYaw(), player.getPitch());
        }
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
}
