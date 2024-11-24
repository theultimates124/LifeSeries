package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.*;
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
                .then(literal("series")
                    .executes(context -> getSeries(context.getSource()))
                )
                .then(literal("version")
                    .executes(context -> getVersion(context.getSource()))
                )
                .then(literal("credits")
                    .executes(context -> getCredits(context.getSource()))
                )
        );
    }


    public static int getSeries(ServerCommandSource source) {
        source.sendMessage(Text.of("Current series: "+ SeriesList.getStringNameFromSeries(currentSeries.getSeries())));
        return 1;
    }
    public static int getVersion(ServerCommandSource source) {
        source.sendMessage(Text.of("Mod version: "+ Main.MOD_VERSION));
        return 1;
    }
    public static int getCredits(ServerCommandSource source) {
        source.sendMessage(Text.of("The Life Series was originally created by " + Credits_IdeaCreator +
                ", and this mod aims to implement every season of the Life Series. "));
        source.sendMessage(Text.of("This mod was created by " + Credits_ModCreator + "."));
        return 1;
    }
}
