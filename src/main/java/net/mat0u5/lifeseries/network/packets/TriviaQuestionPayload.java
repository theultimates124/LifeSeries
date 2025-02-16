package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record TriviaQuestionPayload(String question, int difficulty, long timestamp, int timeToComplete, List<String> answers) implements CustomPayload {

    public static final CustomPayload.Id<TriviaQuestionPayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "triviaquestion"));
    public static final PacketCodec<RegistryByteBuf, TriviaQuestionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TriviaQuestionPayload::question,
            PacketCodecs.INTEGER, TriviaQuestionPayload::difficulty,
            PacketCodecs.VAR_LONG, TriviaQuestionPayload::timestamp,
            PacketCodecs.INTEGER, TriviaQuestionPayload::timeToComplete,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), TriviaQuestionPayload::answers,
            TriviaQuestionPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}