package net.mat0u5.lifeseries.utils;

import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionManager {

    public static boolean isAdmin(ServerPlayerEntity player) {
        if (player == null) return false;
        return player.hasPermissionLevel(2);
    }
}
