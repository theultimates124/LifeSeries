package net.mat0u5.lifeseries.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Environment(EnvType.CLIENT)
public class ClientHandler {
    public static void applyResourcepack(ServerPlayerEntity player) {
        if (MinecraftClient.getInstance() != null) {
            if (MinecraftClient.getInstance().player != null) {
                if (MinecraftClient.getInstance().player.getUuid().equals(player.getUuid())) {
                    if (currentSeries instanceof SecretLife) {
                        enableClientResourcePack("lifeseries:secretlife");
                    }
                    else {
                        disableClientResourcePack("lifeseries:secretlife");
                    }
                }
                else {
                    PlayerUtils.applyServerResourcepack(player);
                }
            }
        }
    }

    // Enable a resource pack
    public static void enableClientResourcePack(String id) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getResourcePackManager() != null) {
            for (ResourcePackProfile profile : client.getResourcePackManager().getProfiles()) {
                if (profile.getId().equals(id)) {
                    client.getResourcePackManager().enable(id);
                    client.reloadResources();
                    return;
                }
            }
        }
    }

    // Disable a resource pack
    public static void disableClientResourcePack(String id) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getResourcePackManager() != null) {
            for (ResourcePackProfile profile : client.getResourcePackManager().getProfiles()) {
                if (profile.getId().equals(id)) {
                    client.getResourcePackManager().disable(id);
                    client.reloadResources();
                    return;
                }
            }
        }
    }
}
