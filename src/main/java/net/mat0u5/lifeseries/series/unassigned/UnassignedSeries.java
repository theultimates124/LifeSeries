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
                .append(Text.literal("'/lifeseries setSeries <series>'").formatted(Formatting.GRAY)), 120);
        OtherUtils.broadcastMessage(Text.literal("You must be an admin to use this command.").formatted(Formatting.RED), 120);
        Text text = Text.literal("§7Click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.BLUE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/QWJxfb4zQZ"))
                                .withUnderline(true)
                        )).append(Text.of("§7 to join the mod development discord if you have any questions, issues, requests, or if you just want to hang out :)"));
        OtherUtils.broadcastMessage(text, 120);
    }
}
