package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NumberPayload(String name, double number) implements CustomPayload {

    public static final CustomPayload.Id<NumberPayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "number"));
    public static final PacketCodec<RegistryByteBuf, NumberPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, NumberPayload::name,
            PacketCodecs.DOUBLE, NumberPayload::number,
            NumberPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}