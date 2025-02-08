package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HandshakePayload(String modVersionStr, int modVersion) implements CustomPayload {

    public static final CustomPayload.Id<HandshakePayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "handshake"));
    public static final PacketCodec<RegistryByteBuf, HandshakePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, HandshakePayload::modVersionStr,
            PacketCodecs.INTEGER, HandshakePayload::modVersion,
            HandshakePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}