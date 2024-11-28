package net.mat0u5.lifeseries.series.unassigned;

import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class UnassignedSeries extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.UNASSIGNED;
    }

    @Override
    public Blacklist createBlacklist() {
        return new Blacklist() {
            @Override
            public List<Item> getItemBlacklist() {
                return List.of();
            }

            @Override
            public List<Block> getBlockBlacklist() {
                return List.of();
            }

            @Override
            public List<RegistryKey<Enchantment>> getClampedEnchants() {
                return List.of();
            }
        };
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        TaskScheduler.scheduleTask(40, this::broadcastNotice);
    }
    @Override
    public void initialize() {
        super.initialize();
        broadcastNotice();
    }
    public void broadcastNotice() {
        OtherUtils.broadcastMessageToAdmins(Text.literal("[LifeSeries] You must select a series with ").formatted(Formatting.RED)
                .append(Text.literal("'/lifeseries setSeries <series>'").formatted(Formatting.GRAY)));
        OtherUtils.broadcastMessageToAdmins(Text.literal("Optionally, you can change the config file ").formatted(Formatting.RED)
                .append(Text.literal("(./config/lifeseries.properties)").formatted(Formatting.GRAY)));
    }
}
