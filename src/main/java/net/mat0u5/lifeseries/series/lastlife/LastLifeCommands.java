package net.mat0u5.lifeseries.series.lastlife;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LastLifeCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        if (currentSeries.getSeries() != SeriesList.LAST_LIFE) return;
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
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        ((LastLife) currentSeries).livesManager.assignRandomLives(players);
        return 1;
    }

    public static int giftLife(ServerCommandSource source, ServerPlayerEntity target) {
        if (!isValidCommand(source)) return -1;

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
        Text currentPlayerName = self.getStyledDisplayName();
        currentSeries.removePlayerLife(self);
        AnimationUtils.playTotemAnimation(self);
        TaskScheduler.scheduleTask(40, () -> {
            ((LastLife)currentSeries).livesManager.receiveLifeFromOtherPlayer(currentPlayerName,target);
        });

        return 1;
    }

    public static boolean isValidCommand(ServerCommandSource source) {
        boolean isValid = currentSeries.getSeries() == SeriesList.LAST_LIFE;
        if (!isValid) {
            source.sendError(Text.of("This command is only available when playing Last Life."));
        }
        return isValid;
    }
}
