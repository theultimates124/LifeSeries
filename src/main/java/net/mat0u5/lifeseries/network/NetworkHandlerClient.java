package net.mat0u5.lifeseries.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.client.ClientHandler;
import net.mat0u5.lifeseries.client.trivia.Trivia;
import net.mat0u5.lifeseries.network.packets.HandshakePayload;
import net.mat0u5.lifeseries.network.packets.NumberPayload;
import net.mat0u5.lifeseries.network.packets.StringPayload;
import net.mat0u5.lifeseries.network.packets.TriviaQuestionPayload;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TimeDilation;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class NetworkHandlerClient {
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(NumberPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                handleNumberPacket(payload.name(),payload.number());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(StringPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                handleStringPacket(payload.name(),payload.value());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(NetworkHandlerClient::respondHandshake);
        });
        ClientPlayNetworking.registerGlobalReceiver(TriviaQuestionPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                Trivia.receiveTrivia(payload);
            });
        });
    }
    
    public static void handleStringPacket(String name, String value) {
        if (name.equalsIgnoreCase("currentSeries")) {
            Main.LOGGER.info("[PACKET_CLIENT] Updated current series to "+ value);
            MainClient.clientCurrentSeries = SeriesList.getSeriesFromStringName(value);
            if (Main.isClient()) {
                ClientHandler.checkSecretLifeClient();
            }
        }
        if (name.equalsIgnoreCase("activeWildcards")) {
            List<Wildcards> newList = new ArrayList<>();
            for (String wildcardStr : value.split("__")) {
                newList.add(Wildcards.getFromString(wildcardStr));
            }
            Main.LOGGER.info("[PACKET_CLIENT] Updated current wildcards to "+ newList);

            if (!MainClient.clientActiveWildcards.contains(Wildcards.TIME_DILATION) && newList.contains(Wildcards.TIME_DILATION)) {
                MainClient.TIME_DILATION_TIMESTAMP = System.currentTimeMillis();
            }
            MainClient.clientActiveWildcards = newList;
        }
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

    /*
        Sending
     */

    public static void sendTriviaAnswer(int answer) {
        Main.LOGGER.info("[PACKET_CLIENT] Sending trivia answer: "+ answer);
        ClientPlayNetworking.send(new NumberPayload("trivia_answer", answer));
    }

    public static void respondHandshake() {
        String modVersionStr = Main.MOD_VERSION;
        int modVersion = OtherUtils.getModVersionInt(modVersionStr);
        HandshakePayload payload = new HandshakePayload(modVersionStr, modVersion);
        ClientPlayNetworking.send(payload);
        Main.LOGGER.info("[PACKET_CLIENT] Sent handshake: {"+modVersionStr+", "+modVersion+"}");
    }
}
