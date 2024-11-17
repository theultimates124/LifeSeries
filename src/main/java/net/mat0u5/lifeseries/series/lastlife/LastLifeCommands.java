package net.mat0u5.lifeseries.series.lastlife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Random;

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
                .then(literal("lives")
                    .then(literal("setToRandom")
                        .executes(context -> LastLifeCommands.assignRandomLives(
                            context.getSource(), context.getSource().getServer().getPlayerManager().getPlayerList()
                        ))
                        .then(argument("players",EntityArgumentType.players())
                            .executes(context -> LastLifeCommands.assignRandomLives(
                                context.getSource(), EntityArgumentType.getPlayers(context, "players")
                            ))
                        )
                    )
                    .then(literal("reload")
                        .executes(context -> LastLifeCommands.reloadLives(
                            context.getSource())
                        )
                    )
                    .then(literal("add")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(context -> lifeManager(
                                context.getSource(), EntityArgumentType.getPlayer(context, "player"), 1, false)
                            )
                            .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> lifeManager(
                                    context.getSource(), EntityArgumentType.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), false)
                                )
                            )
                        )
                    )
                    .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(context -> lifeManager(
                                context.getSource(), EntityArgumentType.getPlayer(context, "player"), -1, false)
                            )
                            .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> lifeManager(
                                    context.getSource(), EntityArgumentType.getPlayer(context, "player"), -IntegerArgumentType.getInteger(context, "amount"), false)
                                )
                            )
                        )
                    )
                    .then(literal("set")
                        .then(argument("player", EntityArgumentType.player())
                            .then(argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> lifeManager(
                                    context.getSource(), EntityArgumentType.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), true)
                                )
                            )
                        )
                    )
                )
        );
        dispatcher.register(
            literal("givelife")
                .then(argument("player", EntityArgumentType.player())
                    .executes(context -> giftLife(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
        dispatcher.register(
            literal("lives")
                .executes(context -> showLives(
                    context.getSource()
                ))
        );
    }
    public static int showLives(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        final ServerPlayerEntity self = source.getPlayer();

        if (self == null) return -1;
        Integer playerLives = currentSeries.getPlayerLives(self);
        if (playerLives == null) {
            self.sendMessage(Text.of("You have not been assigned any lives yet."));
            return 1;
        }

        self.sendMessage(Text.literal("You have ").append(currentSeries.getFormattedLives(playerLives)).append(Text.of((playerLives==1?" life.":" lives."))));
        if (playerLives <= 0) {
            self.sendMessage(Text.of("Womp womp."));
        }

        return 1;
    }
    public static int assignRandomLives(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        ((LastLife) currentSeries).assignRandomLives(server, players);
        return 1;
    }
    public static int reloadLives(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        currentSeries.reloadAllPlayerTeams(server);
        return 1;
    }
    public static int lifeManager(ServerCommandSource source, ServerPlayerEntity target, int amount, boolean setNotGive) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        if (target == null) return -1;

        if (setNotGive) {
            currentSeries.setPlayerLives(server,target,amount);
        }
        else {
            currentSeries.addToPlayerLives(server,target,amount);
        }
        source.sendMessage(Text.of(
        (amount >= 0 ? "Added" : "Removed")+" "+Math.abs(amount)+" "+
              (Math.abs(amount)==1?"life":"lives")+(amount >= 0 ? " to " : " from ") + target.getNameForScoreboard() + ".")
        );
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
        currentSeries.removePlayerLife(server,self);
        AnimationUtils.playTotemAnimation(self);
        TaskScheduler.scheduleTask(40, () -> {
            ((LastLife)currentSeries).receiveLifeFromOtherPlayer(server,currentPlayerName,target);
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
