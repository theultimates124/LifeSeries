package net.mat0u5.lifeseries.series.wildlife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WildLifeCommands {

    public static boolean isAllowed() {
        return currentSeries.getSeries() == SeriesList.WILD_LIFE;
    }

    public static boolean checkBanned(ServerCommandSource source) {
        if (isAllowed()) return false;
        source.sendError(Text.of("This command is only available when playing Wild Life."));
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("wildcard")
                .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                .then(literal("list")
                    .executes(context -> listWildcards(
                        context.getSource())
                    )
                )
                .then(literal("listActive")
                    .executes(context -> listActiveWildcards(
                        context.getSource())
                    )
                )
                .then(literal("activate")
                    .then(argument("wildcard", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(Wildcards.getWildcards(), builder))
                        .executes(context -> activateWildcard(
                            context.getSource(), StringArgumentType.getString(context, "wildcard"))
                        )
                    )
                )
                .then(literal("deactivate")
                    .then(argument("wildcard", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(Wildcards.getActiveWildcards(), builder))
                        .executes(context -> deactivateWildcard(
                            context.getSource(), StringArgumentType.getString(context, "wildcard"))
                        )
                    )
                )
        );
    }
    public static int deactivateWildcard(ServerCommandSource source, String wildcardName) {
        if (checkBanned(source)) return -1;
        Wildcards wildcard = Wildcards.getFromString(wildcardName);
        if (wildcard == Wildcards.NULL) {
            source.sendError(Text.of("That Wildcard doesn't exist."));
            return -1;
        }
        if (!WildcardManager.activeWildcards.containsKey(wildcard)) {
            source.sendError(Text.of("That Wildcard is not active."));
            return -1;
        }
        Wildcard wildcardInstance = WildcardManager.activeWildcards.get(wildcard);
        wildcardInstance.deactivate();
        WildcardManager.activeWildcards.remove(wildcard);

        source.sendMessage(Text.of("Deactivated " + wildcardName + "."));
        WildcardManager.fadedWildcard();
        return 1;
    }

    public static int activateWildcard(ServerCommandSource source, String wildcardName) {
        if (checkBanned(source)) return -1;
        Wildcards wildcard = Wildcards.getFromString(wildcardName);
        if (wildcard == Wildcards.NULL) {
            source.sendError(Text.of("That Wildcard doesn't exist."));
            return -1;
        }
        if (WildcardManager.activeWildcards.containsKey(wildcard)) {
            source.sendError(Text.of("That Wildcard is already active."));
            return -1;
        }
        Wildcard actualWildcard = Wildcards.getInstance(wildcard);
        if (actualWildcard == null) {
            source.sendError(Text.of("That Wildcard has not been implemented yet."));
            return -1;
        }
        WildcardManager.activeWildcards.put(wildcard, actualWildcard);
        WildcardManager.activateWildcards();
        source.sendMessage(Text.of("Activated " + wildcardName + "."));
        return 1;
    }

    public static int listWildcards(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        source.sendMessage(Text.of("Available Wildcards: " + String.join(", ", Wildcards.getWildcards())));
        return 1;
    }

    public static int listActiveWildcards(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        if (Wildcards.getActiveWildcards().isEmpty()) {
            source.sendMessage(Text.of("There are no active Wildcards right now. \nA Wildcard will be randomly selected when the session starts, or you can use §'/wildcard activate <wildcard>' to activate a specific wildcard."));
            return 1;
        }
        source.sendMessage(Text.of("Activated Wildcards: " + String.join(", ", Wildcards.getActiveWildcards())));
        return 1;
    }
}
