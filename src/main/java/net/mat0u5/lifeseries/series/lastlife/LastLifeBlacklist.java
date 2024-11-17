package net.mat0u5.lifeseries.series.lastlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class LastLifeBlacklist extends Blacklist {
    public static final List<Item> BLACKLISTED_ITEMS = List.of(Items.LECTERN, Items.BOOKSHELF, Items.ENCHANTING_TABLE, Items.MACE, Items.END_CRYSTAL
            , Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.GOLDEN_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET);

    public static final List<Block> BLACKLISTED_BLOCKS = List.of(Blocks.LECTERN, Blocks.BOOKSHELF);

    @Override
    public List<Item> getItemBlacklist() {
        return BLACKLISTED_ITEMS;
    }
    @Override
    public List<Block> getBlockBlacklist() {
        return BLACKLISTED_BLOCKS;
    }

    @Override
    public ItemStack furtherItemProcessing(ItemStack itemStack) {
        return clampEnchantments(itemStack);
    }
    public ItemStack clampEnchantments(ItemStack itemStack) {
        return itemStack;
    }

}
