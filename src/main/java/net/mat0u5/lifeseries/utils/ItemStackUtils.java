package net.mat0u5.lifeseries.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemStackUtils {
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
    public static void setModelData(ItemStack itemStack, int modelData) {
        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(modelData));
    }
}
