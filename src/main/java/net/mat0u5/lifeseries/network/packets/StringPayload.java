package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StringPayload(String name, String value) implements CustomPayload {

    public static final CustomPayload.Id<StringPayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "string"));
    public static final PacketCodec<RegistryByteBuf, StringPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, StringPayload::name,
            PacketCodecs.STRING, StringPayload::value,
            StringPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}