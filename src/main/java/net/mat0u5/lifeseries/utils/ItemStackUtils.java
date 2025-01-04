package net.mat0u5.lifeseries.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
//? if <=1.21
import net.minecraft.item.EnchantedBookItem;


import java.util.ArrayList;
import java.util.List;

import static net.mat0u5.lifeseries.Main.server;

public class ItemStackUtils {
    public static void clearItemLore(ItemStack itemStack) {
        itemStack.remove(DataComponentTypes.LORE);
    }

    public static void addLoreToItemStack(ItemStack itemStack, List<Text> lines) {
        List<Text> loreLines = getLore(itemStack);
        if (lines != null && !lines.isEmpty()) loreLines.addAll(lines);
        LoreComponent lore = new LoreComponent(loreLines);
        itemStack.set(DataComponentTypes.LORE, lore);
    }

    public static List<Text> getLore(ItemStack itemStack) {
        LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
        List<Text> lines = lore.lines();
        if (lines == null) return new ArrayList<>();
        if (lines.isEmpty()) return new ArrayList<>();
        return lines;
    }

    public static ItemStack getHoldingItem(PlayerEntity player) {
        ItemStack mainHandItem = player.getMainHandStack();
        if (mainHandItem != null) {
            if (!mainHandItem.isEmpty()) return mainHandItem;
        }
        ItemStack offHandItem = player.getOffHandStack();
        return offHandItem;
    }

    public static void setCustomComponentInt(ItemStack itemStack, String componentKey, int value) {
        if (itemStack == null) return;
        NbtComponent currentNbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbtComp = currentNbt == null ? new NbtCompound() : currentNbt.copyNbt();
        nbtComp.putInt(componentKey,value);
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtComp));
    }

    public static void setCustomComponentByte(ItemStack itemStack, String componentKey, byte value) {
        if (itemStack == null) return;
        NbtComponent currentNbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbtComp = currentNbt == null ? new NbtCompound() : currentNbt.copyNbt();
        nbtComp.putByte(componentKey,value);
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtComp));
    }

    public static void setCustomComponentBoolean(ItemStack itemStack, String componentKey, boolean value) {
        if (itemStack == null) return;
        NbtComponent currentNbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbtComp = currentNbt == null ? new NbtCompound() : currentNbt.copyNbt();
        nbtComp.putBoolean(componentKey, value);
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtComp));
    }

    public static void setCustomComponentString(ItemStack itemStack, String componentKey, String value) {
        if (itemStack == null) return;
        NbtComponent currentNbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbtComp = currentNbt == null ? new NbtCompound() : currentNbt.copyNbt();
        nbtComp.putString(componentKey,value);
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtComp));
    }

    public static String getCustomComponentString(ItemStack itemStack, String componentKey) {
        if (itemStack == null) return null;
        NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return null;
        NbtCompound nbtComp = nbtComponent.copyNbt();
        if (!nbtComp.contains(componentKey)) return null;
        return nbtComp.getString(componentKey);
    }

    public static Integer getCustomComponentInt(ItemStack itemStack, String componentKey) {
        if (itemStack == null) return null;
        NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return null;
        NbtCompound nbtComp = nbtComponent.copyNbt();
        if (!nbtComp.contains(componentKey)) return null;
        return nbtComp.getInt(componentKey);
    }

    public static Byte getCustomComponentByte(ItemStack itemStack, String componentKey) {
        if (itemStack == null) return null;
        NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return null;
        NbtCompound nbtComp = nbtComponent.copyNbt();
        if (!nbtComp.contains(componentKey)) return null;
        return nbtComp.getByte(componentKey);
    }

    public static Boolean getCustomComponentBoolean(ItemStack itemStack, String componentKey) {
        if (itemStack == null) return null;
        NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return null;
        NbtCompound nbtComp = nbtComponent.copyNbt();
        if (!nbtComp.contains(componentKey)) return null;
        return nbtComp.getBoolean(componentKey);
    }

    public static boolean hasCustomComponentEntry(ItemStack itemStack, String componentEntry) {
        NbtComponent nbt = itemStack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;
        return nbt.contains(componentEntry);
    }

    public static void spawnItem(ServerWorld world, Vec3d position, ItemStack stack) {
        spawnItemForPlayer(world, position, stack, null);
    }

    public static void spawnItemForPlayer(ServerWorld world, Vec3d position, ItemStack stack, PlayerEntity player) {
        if (world == null || stack.isEmpty()) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity(world, position.x, position.y, position.z, stack);
        itemEntity.setPickupDelay(20);
        itemEntity.setVelocity(itemEntity.getVelocity().getX()/4, 0.2, itemEntity.getVelocity().getZ()/4);
        if (player != null) itemEntity.setOwner(player.getUuid());

        world.spawnEntity(itemEntity);
    }

    public static ItemStack createEnchantedBook(RegistryKey<Enchantment> enchantment, int level) {
        //? if <=1.21 {
        RegistryEntry<Enchantment> entry = server.getRegistryManager()
                .getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                .getOrThrow(enchantment);
        ItemStack enchantedBook = EnchantedBookItem.forEnchantment(
                new EnchantmentLevelEntry(entry, level)
        );
        return enchantedBook;
        //?} else {
        
        /*RegistryEntry<Enchantment> entry = server.getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT)
                .getOrThrow(enchantment);
        ItemStack enchantedBook = EnchantmentHelper.getEnchantedBookWith(
                new EnchantmentLevelEntry(entry, level)
        );
        return enchantedBook;
        *///?}

    }
}
