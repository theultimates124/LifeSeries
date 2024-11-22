package net.mat0u5.lifeseries.series.doublelife;

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

public class DoubleLifeCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        if (currentSeries.getSeries() != SeriesList.DOUBLE_LIFE) return;

        dispatcher.register(
            literal("doublelife")
                .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                .then(literal("soulmate")
                    .then(literal("get")
                    )
                    .then(literal("set")
                    )
                    .then(literal("list")
                    )
                    .then(literal("reset")
                    )
                    .then(literal("chooseRandom")
                    )
                )
        );
    }
}
