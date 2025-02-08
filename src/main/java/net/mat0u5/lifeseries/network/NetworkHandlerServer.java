package net.mat0u5.lifeseries.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.network.packets.HandshakePayload;
import net.mat0u5.lifeseries.network.packets.NumberPayload;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TimeDilation;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KickCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class NetworkHandlerServer {
    public static HashMap<UUID, Integer> awaitingHandshake = new HashMap<>();

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(NumberPayload.ID, NumberPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(NumberPayload.ID, NumberPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);
    }
    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = context.server();
            server.execute(() -> {
                handleHandshakeResponse(player, payload.modVersionStr(), payload.modVersion());
            });
        });
    }

    public static void handleHandshakeResponse(ServerPlayerEntity player, String modVersionStr, int modVersion) {
        Main.LOGGER.info("[PACKET_SERVER] Received handshake (from "+player.getNameForScoreboard()+"): {"+modVersionStr+", "+modVersion+"}");
        awaitingHandshake.remove(player.getUuid());
    }

    public static void sendHandshake(ServerPlayerEntity player) {
        String modVersionStr = Main.MOD_VERSION;
        int modVersion = OtherUtils.getModVersionInt(modVersionStr);
        HandshakePayload payload = new HandshakePayload(modVersionStr, modVersion);
        ServerPlayNetworking.send(player, payload);
        awaitingHandshake.put(player.getUuid(), 290);
        Main.LOGGER.info("[PACKET_SERVER] Sending handshake: {"+modVersionStr+", "+modVersion+"}");
    }

    public static void sendNumberPacket(ServerPlayerEntity player, String name, double number) {
        NumberPayload payload = new NumberPayload(name, number);
        Main.LOGGER.info("[PACKET_SERVER] Sending number packet to client: {"+name+": "+number+"}");
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendUpdatePacketTo(ServerPlayerEntity player) {
        if (currentSeries instanceof WildLife) {
            sendNumberPacket(player, "hunger_version", Hunger.shuffleVersion);
            sendNumberPacket(player, "player_min_mspt", TimeDilation.MIN_PLAYER_MSPT);
        }
    }

    public static void sendUpdatePackets() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            sendUpdatePacketTo(player);
        }
    }

    public static void tick() {
        HashMap<UUID, Integer> newAwaiting = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : awaitingHandshake.entrySet()) {
            UUID uuid = entry.getKey();
            int num = entry.getValue();
            num--;
            if (num > 0) {
                newAwaiting.put(uuid, num);
            }
            else {
                kickFailedHandshake(uuid);
            }
        }
        awaitingHandshake = newAwaiting;
    }

    public static void kickFailedHandshake(UUID uuid) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        if (player == null) return;
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