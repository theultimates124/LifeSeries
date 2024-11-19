package net.mat0u5.lifeseries.series.thirdlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ThirdLife extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.THIRD_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return new ThirdLifeBlacklist();
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        if (!hasAssignedLives(player)) {
            setPlayerLives(player,3);
        }
        reloadPlayerTeam(player);
    }
    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        if (isOnLastLife(killer)) return;
        if (killer.getPrimeAdversary() == victim && isOnLastLife(victim)) return;
        OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "
                +killer.getNameForScoreboard() + ", who is not §cred name§f."));
    }
}
