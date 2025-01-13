package net.mat0u5.lifeseries.series.thirdlife;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PermissionManager;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.seriesConfig;

public class ThirdLife extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.THIRD_LIFE;
    }

    @Override
    public ConfigManager getConfig() {
        return new ThirdLifeConfig();
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        super.onPlayerJoin(player);

        if (!hasAssignedLives(player)) {
            int lives = seriesConfig.getOrCreateInt("default_lives", 3);
            setPlayerLives(player, lives);
        }
        TaskScheduler.scheduleTask(99, () -> {
            if (PermissionManager.isAdmin(player)) {
                player.sendMessage(Text.of("§7Third Life commands: §r/lifeseries, /session, /claimkill, /lives"));
            }
            else {
                player.sendMessage(Text.of("§7Third Life non-admin commands: §r/claimkill, /lives"));
            }
        });
    }

    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        if (isAllowedToAttack(killer, victim)) return;
        OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "
                +killer.getNameForScoreboard() + ", who is not §cred name§f."));
    }

}
