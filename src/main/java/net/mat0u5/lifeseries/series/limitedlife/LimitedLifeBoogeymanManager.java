package net.mat0u5.lifeseries.series.limitedlife;

import net.mat0u5.lifeseries.series.BoogeymanManager;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class LimitedLifeBoogeymanManager extends BoogeymanManager {
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
    }
    @Override
    public void boogeymenChooseRandom(List<ServerPlayerEntity> allowedPlayers, double currentChance) {
        List<ServerPlayerEntity> nonRedPlayers = currentSeries.getNonRedPlayers();
        Collections.shuffle(nonRedPlayers);

        List<ServerPlayerEntity> normalPlayers = new ArrayList<>();
        List<ServerPlayerEntity> boogeyPlayers = new ArrayList<>();
        for (ServerPlayerEntity player : nonRedPlayers) {
            if (!allowedPlayers.contains(player)) continue;
            if (rolledPlayers.contains(player.getUuid())) continue;
            boogeyPlayers.add(player);
            break;
        }
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (rolledPlayers.contains(player.getUuid())) continue;
            rolledPlayers.add(player.getUuid());
            if (!allowedPlayers.contains(player)) continue;
            if (boogeyPlayers.contains(player)) continue;
            normalPlayers.add(player);
        }
        PlayerUtils.playSoundToPlayers(normalPlayers, SoundEvent.of(Identifier.of("minecraft","lastlife_boogeyman_no")));
        PlayerUtils.playSoundToPlayers(boogeyPlayers, SoundEvent.of(Identifier.of("minecraft","lastlife_boogeyman_yes")));
        PlayerUtils.sendTitleToPlayers(normalPlayers, Text.literal("NOT the Boogeyman.").formatted(Formatting.GREEN),10,50,20);
        PlayerUtils.sendTitleToPlayers(boogeyPlayers, Text.literal("The Boogeyman.").formatted(Formatting.RED),10,50,20);
        for (ServerPlayerEntity boogey : boogeyPlayers) {
            addBoogeyman(boogey);
            boogey.sendMessage(Text.of("§7You are the Boogeyman. You must by any means necessary kill a §2dark green§7, §agreen§7 or §eyellow§7 name by direct action to be cured of the curse. " +
                    "If you fail, next session you will become a §cred name§7. All loyalties and friendships are removed while you are the Boogeyman."));
        }
    }
}
