package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.secretlife.TaskManager;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.mat0u5.lifeseries.Main.*;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LifeSeriesCommand {
    public static final String Credits_ModCreator = "Mat0u5";
    public static final String Credits_IdeaCreator = "Grian";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("lifeseries")
                .executes(context -> defaultCommand(context.getSource()))
                .then(literal("worlds")
                    .executes(context -> getWorlds(context.getSource()))
                )
                .then(literal("discord")
                    .executes(context -> getDiscord(context.getSource()))
                )
                .then(literal("getSeries")
                    .executes(context -> getSeries(context.getSource()))
                )
                .then(literal("version")
                    .executes(context -> getVersion(context.getSource()))
                )
                .then(literal("reload")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .executes(context -> reload(context.getSource()))
                )
                .then(literal("setSeries")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .then(argument("series", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(ALLOWED_SERIES_NAMES, builder))
                        .executes(context -> setSeries(
                            context.getSource(), StringArgumentType.getString(context, "series"), false)
                        )
                        .then(literal("confirm")
                            .executes(context -> setSeries(
                                context.getSource(), StringArgumentType.getString(context, "series"), true)
                            )
                        )
                    )
                )
        );
    }

    public static int setSeries(ServerCommandSource source, String setTo, boolean confirmed) {
        if (!ALLOWED_SERIES_NAMES.contains(setTo)) {
            source.sendError(Text.of("That is not a valid series!"));
            source.sendError(Text.literal("You must choose one of the following: ").append(Text.literal(String.join(", ", ALLOWED_SERIES_NAMES)).formatted(Formatting.GRAY)));
            return -1;
        }
        if (confirmed) {
            setSeriesFinal(source, setTo);
        }
        else {
            if (currentSeries.getSeries() == SeriesList.UNASSIGNED) {
                setSeriesFinal(source, setTo);
            }
            else {
                source.sendMessage(Text.of("WARNING: you already have a selected series, are you sure you want to change to a different one?"));
                source.sendMessage(Text.literal("If you are sure, use '")
                        .append(Text.literal("/lifeseries setSeries <series>").formatted(Formatting.GRAY))
                        .append(Text.literal(" confirm").formatted(Formatting.GREEN)).append(Text.of("'")));
            }
        }
        return 1;
    }

    public static void setSeriesFinal(ServerCommandSource source, String setTo) {
        currentSeries.resetAllPlayerLives();
        config.setProperty("currentSeries", setTo);
        source.sendMessage(Text.literal("Successfully changed the series to " + setTo + ".").formatted(Formatting.GREEN));
        source.sendMessage(Text.of("---------------"));
        if (source.getServer().isDedicated()) {
            source.sendMessage(Text.literal("--- You must restart the server to fully finish setting up ---").formatted(Formatting.GREEN));
        }
        else {
            source.sendMessage(Text.literal("--- You must restart the client to fully finish setting up ---").formatted(Formatting.GREEN));
        }
        source.sendMessage(Text.of("---------------"));
    }

    public static int getWorlds(ServerCommandSource source) {
        Text worldSavesText = Text.literal("§7Additionally, if you want to play on the exact same worlds as Grian did, click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.WHITE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.dropbox.com/scl/fo/jk9fhqx0jjbgeo2qa6v5i/AOZZxMx6S7MlS9HrIRJkkX4?rlkey=2khwcnf2zhgi6s4ik01e3z9d0&st=ghw1d8k6&dl=0"))
                                .withUnderline(true)
                        )).append(Text.of("§7 to open a dropbox where you can download the pre-made worlds."));
        source.sendMessage(worldSavesText);
        return 1;
    }

    public static int defaultCommand(ServerCommandSource source) {
        getDiscord(source);
        return 1;
    }

    public static int getDiscord(ServerCommandSource source) {
        Text text = Text.literal("§7Click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.WHITE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/QWJxfb4zQZ"))
                                .withUnderline(true)
                        )).append(Text.of("§7 to join the mod development discord if you have any questions, issues, requests, or if you just want to hang out :)"));
        source.sendMessage(text);
        return 1;
    }

    public static int getSeries(ServerCommandSource source) {
        source.sendMessage(Text.of("Current series: "+ SeriesList.getStringNameFromSeries(currentSeries.getSeries())));
        return 1;
    }

    public static int getVersion(ServerCommandSource source) {
        source.sendMessage(Text.of("Mod version: "+ Main.MOD_VERSION));
        return 1;
    }

    public static int reload(ServerCommandSource source) {
        source.sendMessage(Text.of("Reloading!"));
        OtherUtils.executeCommand("reload");
        return 1;
    }

    public static int getCredits(ServerCommandSource source) {
        source.sendMessage(Text.of("The Life Series was originally created by " + Credits_IdeaCreator +
                ", and this mod aims to implement every season of the Life Series. "));
        source.sendMessage(Text.of("This mod was created by " + Credits_ModCreator + "."));
        return 1;
    }
}
