package net.mat0u5.lifeseries.series.thirdlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;

import java.util.ArrayList;
import java.util.List;

public class ThirdLifeBlacklist extends Blacklist {
    public static final List<Item> BLACKLISTED_ITEMS = List.of(
            Items.LEATHER_HELMET,
            Items.CHAINMAIL_HELMET,
            Items.GOLDEN_HELMET,
            Items.IRON_HELMET,
            Items.DIAMOND_HELMET,
            Items.NETHERITE_HELMET,
            Items.TURTLE_HELMET
    );
    @Override
    public List<Item> getItemBlacklist() {
        return BLACKLISTED_ITEMS;
    }

    @Override
    public List<Block> getBlockBlacklist() {
        return new ArrayList<>();
    }

    @Override
    public List<RegistryKey<Enchantment>> getClampedEnchants() {
        return new ArrayList<>();
    }
}
