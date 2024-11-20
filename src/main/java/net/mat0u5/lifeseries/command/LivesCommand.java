package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.*;


public class LivesCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("lives")
            .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
            .then(literal("reload")
                .executes(context -> reloadLives(
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
            .then(literal("get")
                .then(argument("player", EntityArgumentType.player())
                    .executes(context -> getLivesFor(
                        context.getSource(), EntityArgumentType.getPlayer(context, "player"))
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
        );
    }
    public static int getLivesFor(ServerCommandSource source, ServerPlayerEntity target) {
        if (target == null) return -1;

        MinecraftServer server = source.getServer();
        if (!currentSeries.hasAssignedLives(target)) {
            source.sendError(Text.of(target.getNameForScoreboard()+" has not been assigned any lives."));
            return -1;
        }
        Integer lives = currentSeries.getPlayerLives(target);
        MutableText pt1 = Text.literal(target.getNameForScoreboard() + " has ");
        Text pt2 = currentSeries.getFormattedLives(lives);
        Text pt3 = Text.of(" "+(Math.abs(lives)==1?"life":"lives")+".");
        source.sendMessage(pt1.append(pt2).append(pt3));
        return 1;
    }
    public static int reloadLives(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        currentSeries.reloadAllPlayerTeams();
        return 1;
    }
    public static int lifeManager(ServerCommandSource source, ServerPlayerEntity target, int amount, boolean setNotGive) {
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
    public static int resetLives(ServerCommandSource source, ServerPlayerEntity target) {
        MinecraftServer server = source.getServer();
        if (target == null) return -1;

        currentSeries.resetPlayerLife(target);

        source.sendMessage(Text.of("Reset " + target.getNameForScoreboard() + "'s lives."));
        return 1;
    }
}
