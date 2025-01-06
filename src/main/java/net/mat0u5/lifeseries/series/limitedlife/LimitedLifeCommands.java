package net.mat0u5.lifeseries.series.limitedlife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LimitedLifeCommands {

    public static boolean isAllowed() {
        return currentSeries.getSeries() == SeriesList.LIMITED_LIFE;
    }

    public static boolean checkBanned(ServerCommandSource source) {
        if (isAllowed()) return false;
        source.sendError(Text.of("This command is only available when playing Limited Life."));
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("limitedlife")
            .then(literal("time")
                .executes(context -> showLives(context.getSource()))
                .then(literal("reload")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .executes(context -> reloadLives(
                                context.getSource())
                        )
                )
                .then(literal("add")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("time", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(List.of("30m", "1h"), builder))
                                        .executes(context -> lifeManager(
                                                context.getSource(), EntityArgumentType.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "time"), false, false)
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("time", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(List.of("30m", "1h"), builder))
                                        .executes(context -> lifeManager(
                                                context.getSource(), EntityArgumentType.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "time"), false, true)
                                        )
                                )
                        )
                )
                .then(literal("set")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("time", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(List.of("8h", "16h", "24h"), builder))
                                        .executes(context -> lifeManager(
                                                context.getSource(), EntityArgumentType.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "time"), true, false)
                                        )
                                )
                        )
                )
                .then(literal("get")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> getLivesFor(
                                        context.getSource(), EntityArgumentType.getPlayer(context, "player"))
                                )
                        )
                )
                .then(literal("reset")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> resetLives(
                                        context.getSource(), EntityArgumentType.getPlayer(context, "player"))
                                )
                        )
                )
                .then(literal("resetAll")
                        .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                        .executes(context -> resetAllLives(
                                context.getSource())
                        )
                )
            )
        );
    }

    public static int showLives(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        MinecraftServer server = source.getServer();
        final ServerPlayerEntity self = source.getPlayer();

        if (self == null) return -1;
        if (!currentSeries.hasAssignedLives(self)) {
            self.sendMessage(Text.of("You have not been assigned any lives yet."));
            return 1;
        }

        Integer playerLives = currentSeries.getPlayerLives(self);
        self.sendMessage(Text.literal("You have ").append(currentSeries.getFormattedLives(playerLives)).append(Text.of(" left.")));
        if (playerLives <= 0) {
            self.sendMessage(Text.of("Womp womp."));
        }

        return 1;
    }

    public static int getLivesFor(ServerCommandSource source, ServerPlayerEntity target) {
        if (checkBanned(source)) return -1;
        if (target == null) return -1;

        MinecraftServer server = source.getServer();
        if (!currentSeries.hasAssignedLives(target)) {
            source.sendError(Text.of(target.getNameForScoreboard()+" has not been assigned any lives."));
            return -1;
        }
        Integer lives = currentSeries.getPlayerLives(target);
        MutableText pt1 = Text.literal("").append(target.getStyledDisplayName()).append(Text.literal(" has "));
        Text pt2 = currentSeries.getFormattedLives(lives);
        Text pt3 = Text.of(" left.");
        source.sendMessage(pt1.append(pt2).append(pt3));
        return 1;
    }

    public static int reloadLives(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        MinecraftServer server = source.getServer();
        currentSeries.reloadAllPlayerTeams();
        return 1;
    }

    public static int lifeManager(ServerCommandSource source, ServerPlayerEntity target, String timeArgument, boolean setNotGive, boolean reverse) {
        if (checkBanned(source)) return -1;
        MinecraftServer server = source.getServer();
        if (target == null) return -1;

        int amount = OtherUtils.parseTimeSecondsFromArgument(timeArgument);
        if (reverse) amount *= -1;

        if (setNotGive) {
            currentSeries.setPlayerLives(target,amount);
            source.sendMessage(Text.literal("Set ").append(target.getStyledDisplayName()).append(Text.of("'s time to ")).append(currentSeries.getFormattedLives(target)));
        }
        else {
            currentSeries.addToPlayerLives(target,amount);
            String pt1 = amount >= 0 ? "Added" : "Removed";
            String pt2 = " "+OtherUtils.formatTime(Math.abs(amount)*20);
            String pt4 = amount >= 0 ? " to " : " from ";
            source.sendMessage(Text.of(pt1+pt2+pt4).copy().append(target.getStyledDisplayName()).append("."));
        }
        return 1;
    }

    public static int resetLives(ServerCommandSource source, ServerPlayerEntity target) {
        if (checkBanned(source)) return -1;
        MinecraftServer server = source.getServer();
        if (target == null) return -1;

        currentSeries.resetPlayerLife(target);

        source.sendMessage(Text.literal("Reset ").append(target.getStyledDisplayName()).append(Text.of("'s lives.")));
        return 1;
    }

    public static int resetAllLives(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        MinecraftServer server = source.getServer();

        currentSeries.resetAllPlayerLives();

        source.sendMessage(Text.literal("Reset everyone's lives."));
        return 1;
    }
}
