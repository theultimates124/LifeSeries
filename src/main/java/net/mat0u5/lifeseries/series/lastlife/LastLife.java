package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TitleCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class LastLife extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.LAST_LIFE;
    }
    public void receiveLifeFromOtherPlayer(MinecraftServer server, Text playerName, ServerPlayerEntity target) {
        target.playSoundToPlayer(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 10, 1);
        target.sendMessage(Text.literal("You received a life from ").append(playerName));
        PlayerUtils.sendTitle(target, Text.of("You received a life"));
        AnimationUtils.createSpiral(server, target);
        currentSeries.addPlayerLife(server,target);
    }
}
