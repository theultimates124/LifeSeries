package net.mat0u5.lifeseries.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.network.packets.*;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.SizeShifting;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TimeDilation;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.TriviaWildcard;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class NetworkHandlerServer {
    public static List<UUID> handshakeSuccessful = new ArrayList<>();

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(NumberPayload.ID, NumberPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StringPayload.ID, StringPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TriviaQuestionPayload.ID, TriviaQuestionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LongPayload.ID, LongPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerDisguisePayload.ID, PlayerDisguisePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(NumberPayload.ID, NumberPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StringPayload.ID, StringPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TriviaQuestionPayload.ID, TriviaQuestionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LongPayload.ID, LongPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDisguisePayload.ID, PlayerDisguisePayload.CODEC);
    }
    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = context.server();
            server.execute(() -> {
                handleHandshakeResponse(player, payload.modVersionStr(), payload.modVersion());
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(NumberPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = context.server();
            server.execute(() -> {
                handleNumberPacket(player, payload.name(), payload.number());
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(StringPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = context.server();
            server.execute(() -> {
                handleStringPacket(player, payload.name(),payload.value());
            });
        });
    }
    public static void handleNumberPacket(ServerPlayerEntity player, String name, double value) {
        int intValue = (int) value;
        if (name.equalsIgnoreCase("trivia_answer")) {
            Main.LOGGER.info("[PACKET_SERVER] Received trivia answer (from "+player.getNameForScoreboard()+"): "+ intValue);
            TriviaWildcard.handleAnswer(player, intValue);
        }
    }
    public static void handleStringPacket(ServerPlayerEntity player, String name, String value) {
        if (name.equalsIgnoreCase("holding_jump") && currentSeries.getSeries() == SeriesList.WILD_LIFE && WildcardManager.isActiveWildcard(Wildcards.SIZE_SHIFTING)) {
            SizeShifting.onHoldingJump(player);
        }
        if (name.equalsIgnoreCase("superpower_key") && currentSeries.getSeries() == SeriesList.WILD_LIFE) {
            SuperpowersWildcard.pressedSuperpowerKey(player);
        }
    }

    public static void handleHandshakeResponse(ServerPlayerEntity player, String modVersionStr, int modVersion) {
        Main.LOGGER.info("[PACKET_SERVER] Received handshake (from "+player.getNameForScoreboard()+"): {"+modVersionStr+", "+modVersion+"}");
        handshakeSuccessful.add(player.getUuid());
    }

    /*
        Sending
     */
    public static void sendTriviaPacket(ServerPlayerEntity player, String question, int difficulty, long timestamp, int timeToComplete, List<String> answers) {
        TriviaQuestionPayload triviaQuestionPacket = new TriviaQuestionPayload(question, difficulty, timestamp, timeToComplete, answers);
        Main.LOGGER.info("[PACKET_SERVER] Sending trivia question packet to "+player.getNameForScoreboard()+"): {"+question+", " + difficulty+", " + timestamp+", " + timeToComplete + ", " + answers + "}");
        ServerPlayNetworking.send(player, triviaQuestionPacket);
    }

    public static void sendHandshake(ServerPlayerEntity player) {
        String modVersionStr = Main.MOD_VERSION;
        int modVersion = OtherUtils.getModVersionInt(modVersionStr);
        HandshakePayload payload = new HandshakePayload(modVersionStr, modVersion);
        ServerPlayNetworking.send(player, payload);
        handshakeSuccessful.remove(player.getUuid());
        Main.LOGGER.info("[PACKET_SERVER] Sending handshake to "+player.getNameForScoreboard()+": {"+modVersionStr+", "+modVersion+"}");
    }

    public static void sendStringPacket(ServerPlayerEntity player, String name, String value) {
        StringPayload payload = new StringPayload(name, value);
        Main.LOGGER.info("[PACKET_SERVER] Sending string packet to "+player.getNameForScoreboard()+": {"+name+": "+value+"}");
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendNumberPacket(ServerPlayerEntity player, String name, double number) {
        NumberPayload payload = new NumberPayload(name, number);
        Main.LOGGER.info("[PACKET_SERVER] Sending number packet to "+player.getNameForScoreboard()+": {"+name+": "+number+"}");
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendLongPacket(ServerPlayerEntity player, String name, long number) {
        if (player == null) return;
        LongPayload payload = new LongPayload(name, number);
        Main.LOGGER.info("[PACKET_SERVER] Sending long packet to "+player.getNameForScoreboard()+": {"+name+": "+number+"}");
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendUpdatePacketTo(ServerPlayerEntity player) {
        if (currentSeries instanceof WildLife) {
            sendNumberPacket(player, "hunger_version", Hunger.shuffleVersion);
            sendNumberPacket(player, "player_min_mspt", TimeDilation.MIN_PLAYER_MSPT);

            List<String> activeWildcards = new ArrayList<>();
            for (Wildcards wildcard : WildcardManager.activeWildcards.keySet()) {
                activeWildcards.add(Wildcards.getStringName(wildcard));
            }
            sendStringPacket(player, "activeWildcards", String.join("__", activeWildcards));
        }
        sendStringPacket(player, "currentSeries", SeriesList.getStringNameFromSeries(currentSeries.getSeries()));
    }

    public static void sendUpdatePackets() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            sendUpdatePacketTo(player);
        }
    }

    public static void sendPlayerDisguise(String name, String hiddenUUID, String hiddenName, String shownUUID, String shownName) {
        PlayerDisguisePayload payload = new PlayerDisguisePayload(name, hiddenUUID, hiddenName, shownUUID, shownName);
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void tryKickFailedHandshake(ServerPlayerEntity player) {
        if (server == null) return;
        if (currentSeries.getSeries() != SeriesList.WILD_LIFE) return;
        if (handshakeSuccessful.contains(player.getUuid())) return;
        Text disconnectText = Text.literal("You must have the §2Life Series mod\n§l installed on the client§r§r§f to play Wild Life!\n").append(
                Text.literal("Click to download on Modrinth.")
                        .styled(style -> style
                                .withColor(Formatting.BLUE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/life-series"))
                                .withUnderline(true)
                        ));
        player.networkHandler.disconnect(new DisconnectionInfo(disconnectText));
    }

}