package net.mat0u5.lifeseries.series.unassigned;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.mat0u5.lifeseries.Main.server;

public class UnassignedSeries extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.UNASSIGNED;
    }
    @Override
    public ConfigManager getConfig() {
        return new ConfigManager(null, null) {
            @Override
            public void defaultProperties() {}
        };
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        TaskScheduler.scheduleTask(100, this::broadcastNotice);
    }

    @Override
    public void initialize() {
        super.initialize();
        broadcastNotice();
    }

    public void broadcastNotice() {
        OtherUtils.broadcastMessage(Text.literal("[LifeSeries] You must select a series with ").formatted(Formatting.RED)
                .append(Text.literal("'/lifeseries setSeries <series>'").formatted(Formatting.GRAY)));
        OtherUtils.broadcastMessage(Text.literal("You must be an admin to use this command.").formatted(Formatting.RED));
        Text worldSavesText = Text.literal("§7Additionally, if you want to play on the exact same worlds as Grian did, click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.WHITE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.dropbox.com/scl/fo/jk9fhqx0jjbgeo2qa6v5i/AOZZxMx6S7MlS9HrIRJkkX4?rlkey=2khwcnf2zhgi6s4ik01e3z9d0&st=ghw1d8k6&dl=0"))
                                .withUnderline(true)
                        )).append(Text.of("§7 to open a dropbox where you can download the pre-made worlds."));
        OtherUtils.broadcastMessage(worldSavesText);
        if (!server.isDedicated()) {
            OtherUtils.broadcastMessage(Text.literal("The Life Series is designed and tested for multiplayer servers. Some features might not work as intended when playing in singleplayer or on LAN.").formatted(Formatting.RED));
        }
    }
}
