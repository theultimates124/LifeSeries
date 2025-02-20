package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LongPayload(String name, long number) implements CustomPayload {
    public static final CustomPayload.Id<LongPayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "long"));
    public static final PacketCodec<RegistryByteBuf, LongPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, LongPayload::name,
            PacketCodecs.VAR_LONG, LongPayload::number,
            LongPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}