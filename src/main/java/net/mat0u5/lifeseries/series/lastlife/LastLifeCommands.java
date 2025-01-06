package net.mat0u5.lifeseries.series.lastlife;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Collection;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LastLifeCommands {


    public static boolean isAllowed() {
        return currentSeries.getSeries() == SeriesList.LAST_LIFE;
    }

    public static boolean checkBanned(ServerCommandSource source) {
        if (isAllowed()) return false;
        source.sendError(Text.of("This command is only available when playing Last Life."));
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("lastlife")
                .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                .then(literal("rollLives")
                    .executes(context -> LastLifeCommands.assignRandomLives(
                        context.getSource(), PlayerUtils.getAllPlayers()
                    ))
                    .then(argument("players", EntityArgumentType.players())
                        .executes(context -> LastLifeCommands.assignRandomLives(
                            context.getSource(), EntityArgumentType.getPlayers(context, "players")
                        ))
                    )
                )
        );
        dispatcher.register(
            literal("givelife")
                .then(argument("player", EntityArgumentType.player())
                    .executes(context -> giftLife(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    public static int assignRandomLives(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        if (checkBanned(source)) return -1;

        MinecraftServer server = source.getServer();
        ((LastLife) currentSeries).livesManager.assignRandomLives(players);
        return 1;
    }

    public static int giftLife(ServerCommandSource source, ServerPlayerEntity target) {
        if (checkBanned(source)) return -1;

        MinecraftServer server = source.getServer();
        final ServerPlayerEntity self = source.getPlayer();
        if (self == null) return -1;;
        if (target == null) return -1;
        if (!currentSeries.isAlive(self)) {
            source.sendError(Text.of("You do not have any lives to give."));
            return -1;
        }
        if (!currentSeries.isAlive(target)) {
            source.sendError(Text.of("That player is not alive."));
            return -1;
        }
        if (target == self) {
            source.sendError(Text.of("You cannot give a life to yourself."));
            return -1;
        }
        Integer currentLives = currentSeries.getPlayerLives(self);
        if (currentLives <= 1) {
            source.sendError(Text.of("You cannot give away your last life."));
            return -1;
        }
        Integer targetLives = currentSeries.getPlayerLives(target);
        if (targetLives >= LastLife.GIVELIFE_MAX_LIVES) {
            source.sendError(Text.of("That player cannot receive any more lives."));
            return -1;
        }

        Text currentPlayerName = self.getStyledDisplayName();
        currentSeries.removePlayerLife(self);
        ((LastLife) currentSeries).addToLifeNoUpdate(target);
        AnimationUtils.playTotemAnimation(self);
        TaskScheduler.scheduleTask(40, () -> {
            ((LastLife)currentSeries).livesManager.receiveLifeFromOtherPlayer(currentPlayerName,target);
        });

        return 1;
    }
}
