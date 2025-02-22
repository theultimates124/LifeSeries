package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerDisguisePayload(String name, String hiddenUUID, String hiddenName, String shownUUID, String shownName) implements CustomPayload {

    public static final CustomPayload.Id<PlayerDisguisePayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "player_disguise"));
    public static final PacketCodec<RegistryByteBuf, PlayerDisguisePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, PlayerDisguisePayload::name,
            PacketCodecs.STRING, PlayerDisguisePayload::hiddenUUID,
            PacketCodecs.STRING, PlayerDisguisePayload::hiddenName,
            PacketCodecs.STRING, PlayerDisguisePayload::shownUUID,
            PacketCodecs.STRING, PlayerDisguisePayload::shownName,
            PlayerDisguisePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}