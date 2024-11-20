package net.mat0u5.lifeseries.series.doublelife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.minecraft.server.network.ServerPlayerEntity;

public class DoubleLife extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.DOUBLE_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return new DoubleLifeBlacklist();
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        if (!hasAssignedLives(player)) {
            setPlayerLives(player,4);
        }
        reloadPlayerTeam(player);
    }
}
