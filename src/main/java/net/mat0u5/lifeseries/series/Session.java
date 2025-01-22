package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.WorldUitls;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;

import java.util.*;

import static net.mat0u5.lifeseries.Main.server;

public class Session {
    public Map<UUID, Integer> playerNaturalDeathLog = new HashMap<>();
    public List<SessionAction> activeActions = new ArrayList<>();
    public List<UUID> displayTimer = new ArrayList<>();
    public static final int NATURAL_DEATH_LOG_MAX = 2400;
    public static final int DISPLAY_TIMER_INTERVAL = 20;
    public int currentTimer = 20;

    public Integer sessionLength = null;
    public double passedTime;
    public SessionStatus status = SessionStatus.NOT_STARTED;

    SessionAction endWarning1 = new SessionAction(OtherUtils.minutesToTicks(-5)) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("Session ends in 5 minutes!").formatted(Formatting.GOLD));
        }
    };
    SessionAction endWarning2 = new SessionAction(OtherUtils.minutesToTicks(-30)) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("Session ends in 30 minutes!").formatted(Formatting.GOLD));
        }
    };
    SessionAction actionInfoAction = new SessionAction(OtherUtils.secondsToTicks(7)) {
        @Override
        public void trigger() {
            showActionInfo();
        }
    };

    public boolean sessionStart() {
        if (!canStartSession()) return false;
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
        activeActions.add(endWarning1);
        activeActions.add(endWarning2);
        activeActions.add(actionInfoAction);
        return true;
    }

    public void sessionEnd() {
        status = SessionStatus.FINISHED;
        OtherUtils.broadcastMessage(Text.literal("The session has ended!").formatted(Formatting.GOLD));
    }

    public void sessionPause() {
        if (statusPaused()) {
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
        if (statusStarted()) return false;
        if (statusPaused()) return false;
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
        return OtherUtils.formatTime(sessionLength - ((int) passedTime));
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
        if (!statusStarted()) return;
        tickSessionOn(server);
    }

    public void tickSessionOn(MinecraftServer server) {
        float tickRate = server.getTickManager().getTickRate();
        if (tickRate == 20) {
            passedTime++;
        }
        else {
            passedTime += (20/tickRate);
        }

        if (passedTime >= sessionLength) {
            sessionEnd();
        }

        //Actions
        if (activeActions == null) return;
        if (activeActions.isEmpty()) return;
        List<SessionAction> remaining = new ArrayList<>();
        for (SessionAction action : activeActions) {
            boolean triggered = action.tick((int) passedTime, sessionLength);
            if (!triggered) {
                remaining.add(action);
            }
        }
        activeActions = remaining;
    }

    public void checkPlayerPosition(ServerPlayerEntity player) {
        WorldBorder border = player.getWorld().getWorldBorder();
        double playerSize = player.getBoundingBox().getLengthX()/2;
        double minX = Math.floor(border.getBoundWest()) + playerSize;
        double maxX = Math.ceil(border.getBoundEast()) - playerSize;
        double minZ = Math.floor(border.getBoundNorth()) + playerSize;
        double maxZ = Math.ceil(border.getBoundSouth()) - playerSize;

        double playerX = player.getX();
        double playerZ = player.getZ();

        if (playerX < minX || playerX > maxX || playerZ < minZ || playerZ > maxZ) {
            // Clamp player position inside the border
            double clampedX = Math.max(minX, Math.min(maxX, playerX));
            double clampedZ = Math.max(minZ, Math.min(maxZ, playerZ));
            double safeY = WorldUitls.findSafeY(player.getWorld(), new Vec3d(clampedX, player.getY(), clampedZ));

            // Teleport player inside the world border
            //? if <=1.21 {
            player.teleport(player.getServerWorld(),clampedX, safeY, clampedZ, player.getYaw(), player.getPitch());
            //?} else {
                /*Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
                player.teleport(player.getServerWorld(),clampedX, safeY, clampedZ, flags, player.getYaw(), player.getPitch(), false);
            *///?}
        }
    }

    public void displayTimers(MinecraftServer server) {
        String message = "";
        if (statusNotStarted()) {
            message = "Session has not started";
        }
        else if (statusStarted()) {
            message = getRemainingLength();
        }
        else if (statusPaused()) {
            message = "Session has been paused";
        }
        else if (statusFinished()) {
            message = "Session has ended";
        }
        for (UUID uuid : displayTimer) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage(Text.literal(message).formatted(Formatting.GRAY), true);
        }
    }

    public void showActionInfo() {
        OtherUtils.broadcastMessageToAdmins(Text.of("§7Queued session actions:"));
        for (SessionAction action : activeActions) {
            String actionMessage = action.sessionMessage;
            if (actionMessage == null) continue;
            if (actionMessage.isEmpty()) continue;
            OtherUtils.broadcastMessageToAdmins(Text.of("§7- "+actionMessage));
        }
    }

    public boolean statusStarted() {
        return status == SessionStatus.STARTED;
    }

    public boolean statusPaused() {
        return status == SessionStatus.PAUSED;
    }

    public boolean statusFinished() {
        return status == SessionStatus.FINISHED;
    }

    public boolean statusNotStarted() {
        return status == SessionStatus.NOT_STARTED;
    }
}
