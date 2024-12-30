package net.mat0u5.lifeseries.mixin;

import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At("HEAD"), cancellable = true)
    private static void getPossibleEntries(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (currentSeries.CUSTOM_ENCHANTMENT_TABLE_ALGORITHM) {
            customEnchantmentTableAlgorithm(level, stack, possibleEnchantments, cir);
        }
        if (currentSeries.BLACKLIST_ENCHANTMENT_TABLE) {
            blacklistEnchantments(level, stack, possibleEnchantments, cir);
        }
    }
    private static void blacklistEnchantments(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<EnchantmentLevelEntry> list = Lists.<EnchantmentLevelEntry>newArrayList();
        boolean bl = stack.isOf(Items.BOOK);
        possibleEnchantments.filter(enchantment -> ((Enchantment)enchantment.value()).isPrimaryItem(stack) || bl).forEach(enchantmentx -> {
            Enchantment enchantment = (Enchantment)enchantmentx.value();
            Optional<RegistryKey<Enchantment>> enchantRegistryKey = enchantmentx.getKey();
            boolean isRegistryPresent = enchantRegistryKey.isPresent();
            for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); j--) {
                if (level >= enchantment.getMinPower(j) && level <= enchantment.getMaxPower(j)) {
                    if (isRegistryPresent && blacklist.getClampedEnchants().contains(enchantRegistryKey.get())) {
                        list.add(new EnchantmentLevelEntry(enchantmentx, 1));
                    }
                    else {
                        list.add(new EnchantmentLevelEntry(enchantmentx, j));
                    }
                    break;
                }
            }
        });
        cir.setReturnValue(list);
    }

    private static void customEnchantmentTableAlgorithm(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<EnchantmentLevelEntry> list = new ArrayList<>();
        boolean bl = stack.isOf(Items.BOOK);
        possibleEnchantments.filter(enchantment -> ((Enchantment)enchantment.value()).isPrimaryItem(stack) || bl).forEach(enchantmentx -> {
            Enchantment enchantment = (Enchantment)enchantmentx.value();
            Optional<RegistryKey<Enchantment>> enchantRegistryKey = enchantmentx.getKey();
            if (enchantRegistryKey.isPresent()) {
                if (blacklist.getClampedEnchants().contains(enchantRegistryKey.get())) {
                    list.add(new EnchantmentLevelEntry(enchantmentx, 1));
                }
                else {
                    for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); j--) {
                        if (j == 1) {
                            if (enchantment.getMaxLevel() <= 3 || level < 4) {
                                list.add(new EnchantmentLevelEntry(enchantmentx, j));
                            }
                        }
                        else if (j == 2 && level > 4 && enchantment.getMaxLevel() > 3) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                        else if (j == 2 && level > 6 && enchantment.getMaxLevel() >= 3) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                        else if (j == 3 && level > 6 && enchantment.getMaxLevel() > 3) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                    }
                }
            }
        });
        cir.setReturnValue(list);
    }
}
