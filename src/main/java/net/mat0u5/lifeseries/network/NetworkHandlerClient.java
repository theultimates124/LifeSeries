package net.mat0u5.lifeseries.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.network.packets.HandshakePayload;
import net.mat0u5.lifeseries.network.packets.NumberPayload;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TimeDilation;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.client.MinecraftClient;

public class NetworkHandlerClient {
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(NumberPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                handleNumberPacket(payload.name(),payload.number());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(NetworkHandlerClient::respondHandshake);
        });
    }

    public static void handleNumberPacket(String name, double number) {
        int intNumber = (int) number;
        if (name.equalsIgnoreCase("hunger_version")) {
            Main.LOGGER.info("[PACKET_CLIENT] Updated hunger shuffle version to "+ intNumber);
            Hunger.shuffleVersion = intNumber;
        }
        if (name.equalsIgnoreCase("player_min_mspt")) {
            Main.LOGGER.info("[PACKET_CLIENT] Updated min. player MSPT to "+ number);
            TimeDilation.MIN_PLAYER_MSPT = (float) number;
        }
    }

    public static void respondHandshake() {
        String modVersionStr = Main.MOD_VERSION;
        int modVersion = OtherUtils.getModVersionInt(modVersionStr);
        HandshakePayload payload = new HandshakePayload(modVersionStr, modVersion);
        ClientPlayNetworking.send(payload);
        Main.LOGGER.info("[PACKET_CLIENT] Sent handshake: {"+modVersionStr+", "+modVersion+"}");
    }
}
