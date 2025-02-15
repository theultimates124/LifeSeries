package net.mat0u5.lifeseries.mixin;

import net.minecraft.component.*;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = ItemStack.class, priority = 1)
public class ItemStackMixin {
    @Inject(method = "areItemsAndComponentsEqual", at = @At("HEAD"), cancellable = true)
    private static void areItemsAndComponentsEqual(ItemStack stack, ItemStack otherStack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isOf(otherStack.getItem())) {
        } else {
            if (stack.isEmpty() && otherStack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }
            //? if <= 1.21 {
            ComponentMapImpl comp1 = new ComponentMapImpl(stack.getComponents());
            ComponentMapImpl comp2 = new ComponentMapImpl(otherStack.getComponents());
             //?} else {
            /*MergedComponentMap comp1 = new MergedComponentMap(stack.getComponents());
            MergedComponentMap comp2 = new MergedComponentMap(otherStack.getComponents());
            *///?}

            comp1.set(DataComponentTypes.FOOD, stack.getDefaultComponents().get(DataComponentTypes.FOOD));
            comp2.set(DataComponentTypes.FOOD, stack.getDefaultComponents().get(DataComponentTypes.FOOD));
            //? if >= 1.21.2 {
            /*comp1.set(DataComponentTypes.CONSUMABLE, stack.getDefaultComponents().get(DataComponentTypes.CONSUMABLE));
            comp2.set(DataComponentTypes.CONSUMABLE, stack.getDefaultComponents().get(DataComponentTypes.CONSUMABLE));
             *///?}

            cir.setReturnValue(Objects.equals(comp1, comp2));
        }
    }
}
