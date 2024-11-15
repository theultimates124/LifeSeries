package net.mat0u5.lifeseries.series.lastlife;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.command.Commands;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LastLifeCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {

        dispatcher.register(
            literal("lastLife")
                .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                .executes(context -> LastLifeCommands.testCommand(
                        context.getSource())
                )
                .then(literal("lives")
                    .then(literal("assignRandomLives")
                        .executes(context -> LastLifeCommands.assignRandomLives(
                            context.getSource(), context.getSource().getServer().getPlayerManager().getPlayerList()
                        ))
                        .then(argument("targets",EntityArgumentType.players())
                            .executes(context -> LastLifeCommands.assignRandomLives(
                                context.getSource(), EntityArgumentType.getPlayers(context, "targets")
                            ))
                        )
                    )
                    .then(literal("reload")
                        .executes(context -> LastLifeCommands.reloadLives(
                            context.getSource())
                        )
                    )
                )
        );
    }
    public static int testCommand(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        final PlayerEntity self = source.getPlayer();

        source.sendMessage(Text.of("Command successful"));
        return 1;
    }
    public static int assignRandomLives(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        MinecraftServer server = source.getServer();
        Random rnd = new Random();
        for (ServerPlayerEntity player : players) {
            int lives = rnd.nextInt(5)+2;
            LastLife.setPlayerLives(server, player, lives);
        }
        return 1;
    }
    public static int reloadLives(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        LastLife.reloadAllPlayerTeams(server);
        return 1;
    }
}
