package net.mat0u5.lifeseries.utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.mat0u5.lifeseries.command.*;
import net.mat0u5.lifeseries.events.Events;
import net.mat0u5.lifeseries.series.doublelife.DoubleLifeCommands;
import net.mat0u5.lifeseries.series.lastlife.LastLifeCommands;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLifeCommands;
import net.mat0u5.lifeseries.series.secretlife.SecretLifeCommands;
import net.mat0u5.lifeseries.series.wildlife.WildLifeCommands;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;

import static net.mat0u5.lifeseries.Main.server;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
        TextUtils.setEmotes();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(DoubleLifeCommands::register);
        CommandRegistrationCallback.EVENT.register(LastLifeCommands::register);
        CommandRegistrationCallback.EVENT.register(LimitedLifeCommands::register);
        CommandRegistrationCallback.EVENT.register(SecretLifeCommands::register);
        CommandRegistrationCallback.EVENT.register(WildLifeCommands::register);

        CommandRegistrationCallback.EVENT.register(LivesCommand::register);
        CommandRegistrationCallback.EVENT.register(SessionCommand::register);
        CommandRegistrationCallback.EVENT.register(BoogeymanCommand::register);
        CommandRegistrationCallback.EVENT.register(ClaimKillCommand::register);
        CommandRegistrationCallback.EVENT.register(LifeSeriesCommand::register);
    }

    private static void registerEvents() {
        Events.register();
        TaskScheduler.registerTickHandler();
    }
}
