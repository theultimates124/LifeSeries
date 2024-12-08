package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class SecretLife extends Series {

    public static final double MAX_HEALTH = 60.0d;
    @Override
    public SeriesList getSeries() {
        return SeriesList.SECRET_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return new SecretLifeBlacklist();
    }
    @Override
    public void initialize() {
        super.initialize();
        CUSTOM_ENCHANTMENT_TABLE_ALGORITHM = true;
        NO_HEALING = true;
    }
    @Override
    public String getResourcepackURL() {
        return "https://github.com/Mat0u5/LifeSeries-Resources/releases/download/release-secretlife-9cee1e5a515c44bb9d97b8344f4955db8640bf88/RP.zip";
    }
    @Override
    public String getResourcepackSHA1() {
        return "1ad0f33082f86b0fd0c98acd49b4d5d2fdb9855d";
    }
    @Override
    public void onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        syncPlayerHealth(player);
    }
    @Override
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        super.onPlayerDeath(player, source);
        setPlayerHealth(player, MAX_HEALTH);
    }
    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        super.onPlayerJoin(player);
        if (!hasAssignedLives(player)) {
            setPlayerLives(player,3);
            setPlayerHealth(player, MAX_HEALTH);
            player.setHealth((float) MAX_HEALTH);
        }
    }
    public void setPlayerHealth(ServerPlayerEntity player, double newHealth) {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(newHealth);
    }
    public void syncPlayerHealth(ServerPlayerEntity player) {
        setPlayerHealth(player, player.getHealth());
    }
}
