package net.mat0u5.lifeseries.utils;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.mat0u5.lifeseries.command.Command;
import net.mat0u5.lifeseries.events.Events;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
        TextUtils.setEmotes();
    }
    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(Command::register);
    }
    private static void registerEvents() {
        Events.register();
    }
}
