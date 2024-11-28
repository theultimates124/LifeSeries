package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.SessionStatus;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.currentSession;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SessionCommand {


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        if (currentSeries.getSeries() == SeriesList.UNASSIGNED) return;
        dispatcher.register(
            literal("session")
                .then(literal("start")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .executes(context -> SessionCommand.startSession(
                        context.getSource()
                    ))
                )
                .then(literal("stop")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .executes(context -> SessionCommand.stopSession(
                        context.getSource()
                    ))
                )
                .then(literal("pause")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .executes(context -> SessionCommand.pauseSession(
                        context.getSource()
                    ))
                )
                .then(literal("timer")
                    .then(literal("set")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("time", StringArgumentType.string())
                            .executes(context -> SessionCommand.setTime(
                                context.getSource(), StringArgumentType.getString(context, "time")
                            ))
                        )
                    )
                    .then(literal("add")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("time", StringArgumentType.string())
                            .executes(context -> SessionCommand.addTime(
                                context.getSource(), StringArgumentType.getString(context, "time")
                            ))
                        )
                    )
                    .then(literal("fastforward")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("time", StringArgumentType.string())
                            .executes(context -> SessionCommand.skipTime(
                                context.getSource(), StringArgumentType.getString(context, "time")
                            ))
                        )
                    )
                    .then(literal("remove")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                            .then(argument("time", StringArgumentType.string())
                                    .executes(context -> SessionCommand.removeTime(
                                            context.getSource(), StringArgumentType.getString(context, "time")
                                    ))
                            )
                    )
                    .then(literal("remaining")
                        .executes(context -> SessionCommand.getTime(
                            context.getSource()
                        ))
                    )
                    .then(literal("showDisplay")
                        .executes(context -> SessionCommand.displayTimer(
                            context.getSource()
                        ))
                    )
                )

        );
    }
    public static int getTime(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        final ServerPlayerEntity self = source.getPlayer();

        if (!currentSession.validTime()) {
            source.sendError(Text.of("The session time has not been set yet."));
            return -1;
        }

        source.sendMessage(Text.of("The session ends in " + currentSession.getRemainingLength()));
        return 1;
    }
    public static int displayTimer(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        final ServerPlayerEntity self = source.getPlayer();

        if (self == null) return -1;

        boolean isInDisplayTimer = currentSession.isInDisplayTimer(self);
        if (isInDisplayTimer) currentSession.removeFromDisplayTimer(self);
        else currentSession.addToDisplayTimer(self);
        return 1;
    }
    public static int startSession(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        if (!currentSession.validTime()) {
            source.sendError(Text.of("The session time is not set!"));
            return -1;
        }
        if (currentSession.status == SessionStatus.STARTED) {
            source.sendError(Text.of("The session has already started!"));
            return -1;
        }
        if (currentSession.status == SessionStatus.PAUSED) {
            currentSession.sessionPause();
            return 1;
        }
        currentSession.sessionStart();

        return 1;
    }
    public static int stopSession(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        if (currentSession.status == SessionStatus.NOT_STARTED || currentSession.status == SessionStatus.FINISHED) {
            source.sendError(Text.of("The session has not yet started!"));
            return -1;
        }

        currentSession.sessionEnd();

        return 1;
    }
    public static int pauseSession(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        if (currentSession.status == SessionStatus.NOT_STARTED || currentSession.status == SessionStatus.FINISHED) {
            source.sendError(Text.of("The session has not yet started!"));
            return -1;
        }

        currentSession.sessionPause();

        return 1;
    }
    public static int skipTime(ServerCommandSource source, String timeArgument) {

        int totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == -1) {
            source.sendError(Text.literal("Invalid time format. Use h, m, s for hours, minutes, and seconds."));
            return -1;
        }
        currentSession.passedTime+=totalTicks;

        return 1;
    }
    public static int setTime(ServerCommandSource source, String timeArgument) {

        int totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == -1) {
            source.sendError(Text.literal("Invalid time format. Use h, m, s for hours, minutes, and seconds."));
            return -1;
        }
        currentSession.setSessionLength(totalTicks);
        source.sendMessage(Text.of("The session length has been set to "+ OtherUtils.formatTime(totalTicks)));

        return 1;
    }
    public static int addTime(ServerCommandSource source, String timeArgument) {

        int totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == -1) {
            source.sendError(Text.literal("Invalid time format. Use h, m, s for hours, minutes, and seconds."));
            return -1;
        }
        currentSession.addSessionLength(totalTicks);
        source.sendMessage(Text.of("Added "+OtherUtils.formatTime(totalTicks) + " to the session length."));

        return 1;
    }
    public static int removeTime(ServerCommandSource source, String timeArgument) {

        int totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == -1) {
            source.sendError(Text.literal("Invalid time format. Use h, m, s for hours, minutes, and seconds."));
            return -1;
        }
        currentSession.removeSessionLength(totalTicks);
        source.sendMessage(Text.of("Removed "+OtherUtils.formatTime(totalTicks) + " from the session length."));

        return 1;
    }
}

