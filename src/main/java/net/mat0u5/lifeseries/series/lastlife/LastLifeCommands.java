package net.mat0u5.lifeseries.series.lastlife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
                            context.getSource(), PlayerUtils.getAllPlayers()
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
                    .then(literal("reset")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(context -> resetLives(
                                context.getSource(), EntityArgumentType.getPlayer(context, "player"))
                            )
                        )
                    )
                )
                .then(literal("boogeyman")
                    .then(literal("clear")
                        .executes(context -> boogeyClear(
                            context.getSource()
                        ))
                    )
                    .then(literal("list")
                        .executes(context -> boogeyList(
                            context.getSource()
                        ))
                    )
                    .then(literal("add")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(context -> addBoogey(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                        )
                    )
                    .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(context -> removeBoogey(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                        )
                    )
                    .then(literal("cure")
                            .then(argument("player", EntityArgumentType.player())
                                    .executes(context -> cureBoogey(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                            )
                    )
                    .then(literal("chooseRandom")
                        .executes(context -> boogeyChooseRandom(
                            context.getSource()
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
        dispatcher.register(
            literal("lives")
                .executes(context -> showLives(
                    context.getSource()
                ))
        );
    }
    public static int cureBoogey(ServerCommandSource source, ServerPlayerEntity target) {
        if (!isValidCommand(source)) return -1;

        if (target == null) return -1;

        if (!((LastLife) currentSeries).boogeymanManager.isBoogeyman(target)) {
            source.sendError(Text.of("That player is not a boogeyman!"));
            return -1;
        }
        ((LastLife) currentSeries).boogeymanManager.cure(target);

        OtherUtils.broadcastMessageToAdmins(Text.of(target.getNameForScoreboard()+" is now cured."));

        return 1;
    }
    public static int addBoogey(ServerCommandSource source, ServerPlayerEntity target) {
        if (!isValidCommand(source)) return -1;

        if (target == null) return -1;

        if (((LastLife) currentSeries).boogeymanManager.isBoogeyman(target)) {
            source.sendError(Text.of("That player is already a boogeyman!"));
            return -1;
        }
        ((LastLife) currentSeries).boogeymanManager.addBoogeymanManually(target);

        OtherUtils.broadcastMessageToAdmins(Text.of(target.getNameForScoreboard()+" is now a boogeyman."));

        return 1;
    }
    public static int removeBoogey(ServerCommandSource source, ServerPlayerEntity target) {
        if (!isValidCommand(source)) return -1;

        if (target == null) return -1;

        if (!((LastLife) currentSeries).boogeymanManager.isBoogeyman(target)) {
            source.sendError(Text.of("That player is not a boogeyman!"));
            return -1;
        }
        ((LastLife) currentSeries).boogeymanManager.removeBoogeymanManually(target);

        OtherUtils.broadcastMessageToAdmins(Text.of(target.getNameForScoreboard()+" is no longer a boogeyman."));

        return 1;
    }
    public static int boogeyList(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        List<String> boogeymen = new ArrayList<>();
        for (Boogeyman boogeyman : ((LastLife) currentSeries).boogeymanManager.boogeymen) {
            boogeymen.add(boogeyman.name);
        }
        source.sendMessage(Text.of("Current boogeymen: ["+String.join(", ",boogeymen)+"]"));

        return 1;
    }
    public static int boogeyClear(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        ((LastLife) currentSeries).boogeymanManager.resetBoogeymen();
        OtherUtils.broadcastMessageToAdmins(Text.of("All boogeymen have been cleared"));

        return 1;
    }
    public static int boogeyChooseRandom(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        ((LastLife) currentSeries).boogeymanManager.resetBoogeymen();
        ((LastLife) currentSeries).boogeymanManager.chooseBoogeymen(PlayerUtils.getAllPlayers(), 100);

        return 1;
    }


    public static int resetLives(ServerCommandSource source, ServerPlayerEntity target) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        if (target == null) return -1;

        currentSeries.resetPlayerLife(target);

        source.sendMessage(Text.of("Reset " + target.getNameForScoreboard() + "'s lives."));
        return 1;
    }
    public static int showLives(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        final ServerPlayerEntity self = source.getPlayer();

        if (self == null) return -1;
        if (!currentSeries.hasAssignedLives(self)) {
            self.sendMessage(Text.of("You have not been assigned any lives yet."));
            return 1;
        }

        Integer playerLives = currentSeries.getPlayerLives(self);
        self.sendMessage(Text.literal("You have ").append(currentSeries.getFormattedLives(playerLives)).append(Text.of((playerLives==1?" life.":" lives."))));
        if (playerLives <= 0) {
            self.sendMessage(Text.of("Womp womp."));
        }

        return 1;
    }
    public static int assignRandomLives(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        ((LastLife) currentSeries).livesManager.assignRandomLives(players);
        return 1;
    }
    public static int reloadLives(ServerCommandSource source) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        currentSeries.reloadAllPlayerTeams();
        return 1;
    }
    public static int lifeManager(ServerCommandSource source, ServerPlayerEntity target, int amount, boolean setNotGive) {
        if (!isValidCommand(source)) return -1;

        MinecraftServer server = source.getServer();
        if (target == null) return -1;

        if (setNotGive) {
            currentSeries.setPlayerLives(target,amount);
        }
        else {
            currentSeries.addToPlayerLives(target,amount);
        }
        String pt1 = amount >= 0 ? "Added" : "Removed";
        String pt2 = " "+Math.abs(amount)+" ";
        String pt3 = Math.abs(amount)==1?"life":"lives";
        String pt4 = amount >= 0 ? " to " : " from ";
        String pt5 = target.getNameForScoreboard() + ".";

        source.sendMessage(Text.of(pt1+pt2+pt3+pt4+pt5));
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
