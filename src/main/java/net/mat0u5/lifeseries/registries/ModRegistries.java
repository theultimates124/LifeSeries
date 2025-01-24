package net.mat0u5.lifeseries.registries;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.mat0u5.lifeseries.command.*;
import net.mat0u5.lifeseries.events.Events;
import net.mat0u5.lifeseries.series.doublelife.DoubleLifeCommands;
import net.mat0u5.lifeseries.series.lastlife.LastLifeCommands;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLifeCommands;
import net.mat0u5.lifeseries.series.secretlife.SecretLifeCommands;
import net.mat0u5.lifeseries.series.wildlife.WildLifeCommands;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.mat0u5.lifeseries.utils.TextUtils;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
        TextUtils.setEmotes();

        SoundRegistry.registerSounds();
        MobRegistry.registerMobs();
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
