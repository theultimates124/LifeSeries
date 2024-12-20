package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;

import java.util.List;

public class SecretLifeBlacklist extends Blacklist {
    public static final List<Item> BLACKLISTED_ITEMS = List.of(
            Items.LECTERN,
            Items.MACE,
            Items.END_CRYSTAL,
            Items.LEATHER_HELMET,
            Items.CHAINMAIL_HELMET,
            Items.GOLDEN_HELMET,
            Items.IRON_HELMET,
            Items.DIAMOND_HELMET,
            Items.NETHERITE_HELMET,
            Items.TURTLE_HELMET,
            Items.ELYTRA
    );

    public static final List<Block> BLACKLISTED_BLOCKS = List.of(
            Blocks.LECTERN
    );
    public static final List<RegistryKey<Enchantment>> CLAMPED_ENCHANTMENTS = List.of(
            Enchantments.SHARPNESS,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.FIRE_ASPECT,
            Enchantments.KNOCKBACK,
            Enchantments.SWEEPING_EDGE,

            Enchantments.POWER,
            Enchantments.PUNCH,

            Enchantments.THORNS,

            Enchantments.BREACH,
            Enchantments.DENSITY,
            Enchantments.WIND_BURST,

            Enchantments.MULTISHOT,
            Enchantments.PIERCING,
            Enchantments.QUICK_CHARGE
    );

    @Override
    public List<Item> getItemBlacklist() {
        return BLACKLISTED_ITEMS;
    }
    @Override
    public List<Block> getBlockBlacklist() {
        return BLACKLISTED_BLOCKS;
    }

    @Override
    public List<RegistryKey<Enchantment>> getClampedEnchants() {
        return CLAMPED_ENCHANTMENTS;
    }

    @Override
    public boolean isBlacklistedItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (getItemBlacklist().contains(item)) return true;
        if (item != Items.POTION && item != Items.LINGERING_POTION && item != Items.SPLASH_POTION) return false;

        PotionContentsComponent potions = itemStack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
        if (potions == null) return false;
        for (StatusEffectInstance effect : potions.getEffects()) {
            if (effect.equals(StatusEffects.STRENGTH)) return true;
            if (effect.equals(StatusEffects.INSTANT_HEALTH)) return true;
            if (effect.equals(StatusEffects.INSTANT_DAMAGE)) return true;
        }
        return false;
    }
}
