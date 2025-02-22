package net.mat0u5.lifeseries.entity.fakeplayer;

import net.minecraft.entity.player.*;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Set;

/*
    Used code from https://github.com/gnembon/fabric-carpet
 */
public class FakePlayerNetworkHandler extends ServerPlayNetworkHandler {
    public FakePlayerNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        super(server, connection, player, clientData);
    }

    @Override
    public void sendPacket(final Packet<?> packetIn) {}

    @Override
    public void disconnect(Text message) {}

    @Override
    //? if <= 1.21 {
    public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags) {
        super.requestTeleport(x, y, z, yaw, pitch, flags);
    //?} else {
    /*public void requestTeleport(PlayerPosition pos, Set<PositionFlag> flags) {
        super.requestTeleport(pos, flags);
    *///?}
        if (player.getServerWorld().getPlayerByUuid(player.getUuid()) != null) {
            syncWithPlayerPosition();
            player.getServerWorld().getChunkManager().updatePosition(player);
        }
    }

}



