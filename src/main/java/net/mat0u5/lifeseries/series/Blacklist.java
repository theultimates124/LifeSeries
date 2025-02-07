package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.mat0u5.lifeseries.Main.seriesConfig;
import static net.mat0u5.lifeseries.Main.server;

public class Blacklist {
    public List<Identifier> loadedListItemIdentifier;
    private List<Item> loadedListItem;
    private List<Block> loadedListBlock;
    private List<RegistryKey<Enchantment>> loadedListEnchants;

    public List<String> loadItemBlacklist() {
        if (seriesConfig == null) return new ArrayList<>();
        String raw = seriesConfig.getOrCreateProperty("blacklist_items", "[]");
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<String> loadBlockBlacklist() {
        if (seriesConfig == null) return new ArrayList<>();
        String raw = seriesConfig.getOrCreateProperty("blacklist_blocks", "[]");
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<String> loadClampedEnchants() {
        if (seriesConfig == null) return new ArrayList<>();
        String raw = seriesConfig.getOrCreateProperty("blacklist_clamped_enchants", "[]");
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<Item> getItemBlacklist() {
        if (loadedListItem != null) return loadedListItem;
        List<Item> newList = new ArrayList<>();
        List<Identifier> newListIdentifier = new ArrayList<>();

        if (seriesConfig != null) {
            if (!seriesConfig.getOrCreateBoolean("spawner_recipe", false)) {
                newListIdentifier.add(Identifier.of("lifeseries", "spawner_recipe"));
            }
        }

        for (String itemId : loadItemBlacklist()) {
            if (!itemId.startsWith("minecraft:")) itemId = "minecraft:" + itemId;

            try {
                Identifier id = Identifier.of(itemId);
                RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);

                // Check if the block exists in the registry
                Item item = Registries.ITEM.get(key);
                if (item != null) {
                    newListIdentifier.add(id);
                    newList.add(item);
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid item: " + itemId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing item ID: " + itemId);
            }
        }

        loadedListItem = newList;
        loadedListItemIdentifier = newListIdentifier;
        return newList;
    }

    public List<Block> getBlockBlacklist() {
        if (loadedListBlock != null) return loadedListBlock;
        List<Block> newList = new ArrayList<>();

        for (String blockId : loadBlockBlacklist()) {
            if (!blockId.startsWith("minecraft:")) blockId = "minecraft:" + blockId;

            try {
                Identifier id = Identifier.of(blockId);
                RegistryKey<Block> key = RegistryKey.of(Registries.BLOCK.getKey(), id);

                // Check if the block exists in the registry
                Block block = Registries.BLOCK.get(key);
                if (block != null) {
                    newList.add(block);
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid block: " + blockId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing block ID: " + blockId);
            }
        }

        loadedListBlock = newList;
        return newList;
    }

    public List<RegistryKey<Enchantment>> getClampedEnchants() {
        if (server == null) return new ArrayList<>();

        if (loadedListEnchants != null) return loadedListEnchants;
        List<RegistryKey<Enchantment>> newList = new ArrayList<>();

        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager()

                //? if <=1.21 {
                .get(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));
                 //?} else
                /*.getOrThrow(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));*/


        for (String enchantmentId : loadClampedEnchants()) {
            if (!enchantmentId.startsWith("minecraft:")) enchantmentId = "minecraft:" + enchantmentId;

            try {
                Identifier id = Identifier.of(enchantmentId);
                Enchantment enchantment = enchantmentRegistry.get(id);

                if (enchantment != null) {
                    newList.add(enchantmentRegistry.getKey(enchantment).orElseThrow());
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid enchantment: " + enchantmentId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing enchantment ID: " + enchantmentId);
            }
        }

        loadedListEnchants = newList;
        return newList;
    }

    public void reloadBlacklist() {
        if (Main.server == null) return;
        loadedListItem = null;
        loadedListBlock = null;
        loadedListEnchants = null;
        getItemBlacklist();
        getBlockBlacklist();
        getClampedEnchants();
    }


    public ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isCreative() && seriesConfig.getOrCreateBoolean("creative_ignore_blacklist", true)) return ActionResult.PASS;
        processItemStack(player, player.getStackInHand(hand));
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState block = world.getBlockState(blockPos);
        if (block.isAir()) return ActionResult.PASS;
        if (getBlockBlacklist().contains(block.getBlock())) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (player.isCreative() && seriesConfig.getOrCreateBoolean("creative_ignore_blacklist", true)) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.PASS;
        BlockState block = world.getBlockState(pos);
        if (block.isAir()) return ActionResult.PASS;
        if (getBlockBlacklist().contains(block.getBlock())) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public void onCollision(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        processItemStack(player, stack);
    }

    public void onInventoryUpdated(PlayerEntity player, PlayerInventory inventory, CallbackInfo ci) {
        if (Main.server == null) return;
        if (player.isCreative() && seriesConfig.getOrCreateBoolean("creative_ignore_blacklist", true)) return;
        for (int i = 0; i < inventory.size(); i++) {
            processItemStack(player, inventory.getStack(i));
        }
        player.currentScreenHandler.sendContentUpdates();
        player.playerScreenHandler.onContentChanged(player.getInventory());
    }

    public boolean isBlacklistedItemSimple(ItemStack itemStack) {
        return getItemBlacklist().contains(itemStack.getItem());
    }

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

    public void processItemStack(PlayerEntity player, ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        if (itemStack.getItem() == Items.AIR) return;
        if (isBlacklistedItem(itemStack) && !ItemStackUtils.hasCustomComponentEntry(itemStack, "IgnoreBlacklist")) {
            itemStack.setCount(0);
            player.getInventory().updateItems();
            return;
        }
        ItemEnchantmentsComponent enchants = itemStack.getComponents().get(DataComponentTypes.ENCHANTMENTS);
        ItemEnchantmentsComponent enchantsStored = itemStack.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (enchants != null) clampEnchantments(enchants);
        if (enchantsStored != null) clampEnchantments(enchantsStored);
    }

    public void clampEnchantments(ItemEnchantmentsComponent enchants) {
        List<RegistryKey<Enchantment>> clamp = getClampedEnchants();
        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchants.getEnchantmentEntries()) {
            Optional<RegistryKey<Enchantment>> enchantRegistry = enchant.getKey().getKey();
            if (enchantRegistry.isEmpty()) continue;
            if (clamp.contains(enchantRegistry.get())) {
                enchant.setValue(1);
            }
        }
    }

    public List<EnchantmentLevelEntry> clampEnchantmentList(List<EnchantmentLevelEntry> list) {
        List<RegistryKey<Enchantment>> clamp = getClampedEnchants();
        List<EnchantmentLevelEntry> result = new ArrayList<>();
        for (EnchantmentLevelEntry enchantment : list) {
            RegistryEntry<Enchantment> enchRegistryEntry = enchantment.enchantment;
            Optional<RegistryKey<Enchantment>> enchantRegistryKey = enchRegistryEntry.getKey();
            if (enchantRegistryKey.isEmpty()) continue;
            if (clamp.contains(enchantRegistryKey.get())) {
                result.add(new EnchantmentLevelEntry(enchRegistryEntry, 1));
            }
        }
        return result;
    }
}